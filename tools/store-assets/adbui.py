"""adb로 에뮬레이터를 조작하는 작은 도우미 (uiautomator 덤프에서 글자로 요소를 찾아 누른다)."""

from __future__ import annotations

import os
import re
import subprocess
import time
from dataclasses import dataclass
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]


def _sdk_dir() -> str:
    env = os.environ.get("ANDROID_SDK") or os.environ.get("ANDROID_HOME")
    if env:
        return env
    props = REPO_ROOT / "local.properties"
    if props.exists():
        for line in props.read_text().splitlines():
            if line.startswith("sdk.dir="):
                return line.split("=", 1)[1].strip()
    return str(Path.home() / "Library/Android/sdk")


ADB = str(Path(_sdk_dir()) / "platform-tools" / "adb")

_NODE = re.compile(r"<node ([^>]*?)/?>")
_ATTR = re.compile(r'([\w-]+)="([^"]*)"')
_BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


@dataclass
class Node:
    text: str
    desc: str
    clickable: bool
    left: int
    top: int
    right: int
    bottom: int

    @property
    def center(self) -> tuple[int, int]:
        return (self.left + self.right) // 2, (self.top + self.bottom) // 2


class Device:
    def __init__(self, serial: str):
        self.serial = serial

    # --- adb ---
    def adb(self, *args: str, check: bool = True, binary: bool = False, stdin: bytes | None = None):
        result = subprocess.run(
            [ADB, "-s", self.serial, *args],
            input=stdin,
            capture_output=True,
            check=False,
        )
        if check and result.returncode != 0:
            raise RuntimeError(f"adb {' '.join(args)} 실패: {result.stderr.decode(errors='replace')[:300]}")
        return result.stdout if binary else result.stdout.decode(errors="replace")

    def shell(self, command: str, check: bool = True) -> str:
        return self.adb("shell", command, check=check)

    def root(self) -> None:
        self.adb("root", check=False)
        time.sleep(2)
        self.adb("wait-for-device")

    # --- 화면 읽기 ---
    def nodes(self) -> list[Node]:
        xml = ""
        for _ in range(5):
            self.shell("uiautomator dump /sdcard/ui.xml >/dev/null 2>&1", check=False)
            xml = self.shell("cat /sdcard/ui.xml", check=False)
            if "<node" in xml:
                break
            time.sleep(0.5)
        found = []
        for match in _NODE.finditer(xml):
            attrs = dict(_ATTR.findall(match.group(1)))
            bounds = _BOUNDS.match(attrs.get("bounds", ""))
            if not bounds:
                continue
            left, top, right, bottom = map(int, bounds.groups())
            found.append(
                Node(
                    text=_unescape(attrs.get("text", "")),
                    desc=_unescape(attrs.get("content-desc", "")),
                    clickable=attrs.get("clickable") == "true",
                    left=left,
                    top=top,
                    right=right,
                    bottom=bottom,
                )
            )
        return found

    def find(self, label: str, exact: bool = True) -> list[Node]:
        def matches(node: Node) -> bool:
            for value in (node.text, node.desc):
                if value and (value == label if exact else label in value):
                    return True
            return False

        return [node for node in self.nodes() if matches(node)]

    def wait_for(self, label: str, timeout: float = 20.0, exact: bool = True) -> Node:
        deadline = time.time() + timeout
        while time.time() < deadline:
            found = self.find(label, exact)
            if found:
                return found[0]
            time.sleep(0.5)
        texts = [n.text or n.desc for n in self.nodes() if n.text or n.desc]
        raise RuntimeError(f"[{self.serial}] '{label}'를 찾지 못함. 화면의 글자: {texts}")

    def has(self, label: str, exact: bool = True) -> bool:
        return bool(self.find(label, exact))

    # --- 입력 ---
    def tap(self, x: int, y: int) -> None:
        self.shell(f"input tap {x} {y}")
        time.sleep(0.8)

    def tap_label(self, label: str, timeout: float = 20.0, exact: bool = True, index: int = 0) -> None:
        self.wait_for(label, timeout, exact)
        node = self.find(label, exact)[index]
        self.tap(*node.center)

    def type_text(self, text: str) -> None:
        self.shell(f"input text {text}")
        time.sleep(0.5)

    def key(self, code: int) -> None:
        self.shell(f"input keyevent {code}")
        time.sleep(0.5)

    def swipe(self, x1: int, y1: int, x2: int, y2: int, ms: int = 400) -> None:
        self.shell(f"input swipe {x1} {y1} {x2} {y2} {ms}")
        time.sleep(0.8)

    def size(self) -> tuple[int, int]:
        match = re.search(r"(\d+)x(\d+)", self.shell("wm size"))
        return int(match.group(1)), int(match.group(2))

    def screencap(self, path: Path) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(self.adb("exec-out", "screencap", "-p", binary=True))

    def geo_fix(self, lat: float, lng: float) -> None:
        # 에뮬레이터 콘솔 명령은 경도, 위도 순서
        self.adb("emu", "geo", "fix", f"{lng:.6f}", f"{lat:.6f}", check=False)


def _unescape(value: str) -> str:
    return (
        value.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", '"')
        .replace("&#10;", "\n")
        .replace("&apos;", "'")
    )
