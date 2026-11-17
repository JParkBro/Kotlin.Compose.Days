"""스토어 그래픽 공통: 앱 테마 색, 글꼴, 아이콘 심볼(SVG), 헤드리스 Chrome 렌더링.

색은 docs/days/assets/theme-tokens.css 의 pink · light 에서, 글꼴은 앱에 든 Pretendard 에서 읽는다 (앱과 어긋나지 않게).
아이콘 심볼은 108x108 좌표(적응형 아이콘과 같은 크기)의 path로 정의해 SVG와 Android 벡터 드로어블에 함께 쓴다.
"""

from __future__ import annotations

import math
import re
import shutil
import subprocess
import tempfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
OUT_DIR = REPO_ROOT / "store-assets"
FONT_DIR = REPO_ROOT / "core/designsystem/src/main/res/font"
THEME_TOKENS = REPO_ROOT / "docs/days/assets/theme-tokens.css"

APP_NAME = "데이즈"
TAGLINE = "혼자 세는 디데이도, 둘이 챙기는 기념일도"


def _load_colors() -> dict[str, str]:
    """pink · light 팔레트 (토큰 파일에서 못 찾으면 같은 값의 기본값)."""
    colors = {
        "primary": "#D23C69",
        "primary-pressed": "#B8305A",
        "primary-soft": "#FCE8EE",
        "on-primary-soft": "#A62852",
        "accent": "#F28C6B",
        "accent-soft": "#FDEEE7",
        "bg": "#FBF8F9",
        "surface": "#FFFFFF",
        "text-primary": "#221A1E",
        "text-secondary": "#6E6268",
    }
    try:
        css = THEME_TOKENS.read_text(encoding="utf-8")
    except OSError:
        return colors
    block = re.search(r'\[data-theme="pink"\]\[data-mode="light"\]\s*\{(.*?)\}', css, re.S)
    if block:
        for name, value in re.findall(r"--color-([\w-]+):\s*(#[0-9A-Fa-f]{6})", block.group(1)):
            if name in colors:
                colors[name] = value.upper()
    return colors


COLORS = _load_colors()


def font_path(weight: str) -> Path:
    """weight: regular · medium · semi_bold · bold · extra_bold"""
    return FONT_DIR / f"pretendard_{weight}.otf"


def font_face_css() -> str:
    """HTML에서 Pretendard를 쓰기 위한 @font-face (로컬 파일)."""
    weights = {"regular": 400, "medium": 500, "semi_bold": 600, "bold": 700, "extra_bold": 800}
    return "\n".join(
        f"@font-face {{ font-family: 'Pretendard'; font-weight: {w}; src: url('{font_path(name).as_uri()}'); }}"
        for name, w in weights.items()
    )


# ---------------------------------------------------------------- 도형 → path


def rounded_rect(x: float, y: float, w: float, h: float, r: float) -> str:
    return (
        f"M{x + r:.2f},{y:.2f}h{w - 2 * r:.2f}a{r:.2f},{r:.2f} 0 0 1 {r:.2f},{r:.2f}"
        f"v{h - 2 * r:.2f}a{r:.2f},{r:.2f} 0 0 1 -{r:.2f},{r:.2f}"
        f"h-{w - 2 * r:.2f}a{r:.2f},{r:.2f} 0 0 1 -{r:.2f},-{r:.2f}"
        f"v-{h - 2 * r:.2f}a{r:.2f},{r:.2f} 0 0 1 {r:.2f},-{r:.2f}z"
    )


def circle(cx: float, cy: float, r: float) -> str:
    return f"M{cx - r:.2f},{cy:.2f}a{r:.2f},{r:.2f} 0 1 0 {2 * r:.2f},0a{r:.2f},{r:.2f} 0 1 0 -{2 * r:.2f},0z"


def sparkle(cx: float, cy: float, r: float, waist: float = 0.28) -> str:
    """끝이 뾰족한 4갈래 반짝임 (오목한 변은 2차 곡선)."""
    k = r * waist
    return (
        f"M{cx:.2f},{cy - r:.2f}Q{cx + k:.2f},{cy - k:.2f} {cx + r:.2f},{cy:.2f}"
        f"Q{cx + k:.2f},{cy + k:.2f} {cx:.2f},{cy + r:.2f}"
        f"Q{cx - k:.2f},{cy + k:.2f} {cx - r:.2f},{cy:.2f}"
        f"Q{cx - k:.2f},{cy - k:.2f} {cx:.2f},{cy - r:.2f}z"
    )


def arc_stroke(cx: float, cy: float, r: float, start_deg: float, sweep_deg: float) -> str:
    """시계 방향 호 (0도 = 12시). 굵기 · 끝 모양은 stroke로."""

    def point(deg: float) -> tuple[float, float]:
        rad = math.radians(deg - 90)
        return cx + r * math.cos(rad), cy + r * math.sin(rad)

    x1, y1 = point(start_deg)
    x2, y2 = point(start_deg + sweep_deg)
    large = 1 if sweep_deg > 180 else 0
    return f"M{x1:.2f},{y1:.2f}A{r:.2f},{r:.2f} 0 {large} 1 {x2:.2f},{y2:.2f}"


def arc_end(cx: float, cy: float, r: float, deg: float) -> tuple[float, float]:
    rad = math.radians(deg - 90)
    return cx + r * math.cos(rad), cy + r * math.sin(rad)


# ---------------------------------------------------------------- 아이콘 시안
# 각 시안: background(색), layers([{d, fill | stroke, width}]). 좌표는 108x108, 심볼은 지름 66 안전 영역 안.


def _calendar() -> dict:
    """A. 달력 한 장 + 동그라미 친 날 + 반짝임: 혼자든 둘이든 "기다리는 그날"."""
    c = COLORS
    return {
        "key": "a-calendar",
        "title": "A. 그날에 동그라미",
        "background": c["primary"],
        "layers": [
            {"d": rounded_rect(32, 36, 44, 41, 9), "fill": "#FFFFFF"},
            {"d": rounded_rect(41.5, 29, 6, 13, 3), "fill": c["accent"]},
            {"d": rounded_rect(60.5, 29, 6, 13, 3), "fill": c["accent"]},
            {"d": circle(54, 58.5, 12.5), "fill": c["primary"]},
            {"d": sparkle(54, 58.5, 7.6), "fill": "#FFFFFF"},
        ],
    }


def _ring() -> dict:
    """B. 다가오는 날까지 채워지는 진행 링 (홈의 "다음 기념일" 진행 바에서)."""
    c = COLORS
    cx, cy, r = 54, 54, 21
    end = arc_end(cx, cy, r, 270)
    return {
        "key": "b-ring",
        "title": "B. 채워지는 날들",
        "background": c["primary-soft"],
        "layers": [
            {"d": circle(cx, cy, r), "stroke": "#FFFFFF", "width": 10},
            {"d": arc_stroke(cx, cy, r, 0, 270), "stroke": c["primary"], "width": 10},
            {"d": circle(end[0], end[1], 6.6), "fill": c["accent"]},
            {"d": sparkle(cx, cy, 8.5), "fill": c["primary"]},
        ],
    }


def _two_days() -> dict:
    """C. 겹친 두 동그라미: 하나여도 완전하고, 둘이면 겹친다."""
    c = COLORS
    return {
        "key": "c-two-days",
        "title": "C. 하나여도, 둘이어도",
        "background": c["primary"],
        "layers": [
            {"d": circle(63, 56, 15.5), "fill": c["accent"]},
            {"d": circle(45, 52, 15.5), "fill": "#FFFFFF"},
            {"d": circle(45, 52, 15.5), "stroke": c["primary"], "width": 3.2},
        ],
    }


ICON_OPTIONS = [_calendar(), _ring(), _two_days()]
RECOMMENDED_ICON = "a-calendar"


def icon_option(key: str) -> dict:
    return next(option for option in ICON_OPTIONS if option["key"] == key)


def symbol_svg(option: dict) -> str:
    """배경 없는 심볼 <g> 조각 (108x108 좌표)."""
    parts = []
    for layer in option["layers"]:
        if "stroke" in layer:
            parts.append(
                f'<path d="{layer["d"]}" fill="none" stroke="{layer["stroke"]}" '
                f'stroke-width="{layer["width"]}" stroke-linecap="round"/>'
            )
        else:
            parts.append(f'<path d="{layer["d"]}" fill="{layer["fill"]}"/>')
    return "\n".join(parts)


def icon_svg(option: dict, size: int, view: tuple[float, float, float, float] = (0, 0, 108, 108)) -> str:
    """꽉 찬 정사각형 아이콘 SVG. [view]로 심볼 주변 여백을 정한다."""
    x, y, w, h = view
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="{x} {y} {w} {h}">'
        f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{option["background"]}"/>'
        f"{symbol_svg(option)}</svg>"
    )


# Play 아이콘(512)은 모서리가 둥글게 잘릴 뿐 가장자리까지 보인다. 런처는 108 중 가운데 약 72만 보이므로
# 같은 크기로 보이도록 가운데 80x80을 쓴다
STORE_ICON_VIEW = (14, 14, 80, 80)


# ---------------------------------------------------------------- 렌더링


def chrome_binary() -> str:
    for candidate in (
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        shutil.which("google-chrome") or "",
        shutil.which("chromium") or "",
    ):
        if candidate and Path(candidate).exists():
            return candidate
    raise RuntimeError("Chrome을 찾지 못함: Google Chrome을 설치하세요.")


def render_html(html: str, width: int, height: int, out_path: Path) -> None:
    """HTML을 헤드리스 Chrome으로 그대로 크기 width x height PNG로 찍는다 (배경 투명 허용)."""
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        page = Path(tmp) / "page.html"
        page.write_text(html, encoding="utf-8")
        subprocess.run(
            [
                chrome_binary(),
                "--headless=new",
                "--disable-gpu",
                "--hide-scrollbars",
                "--force-device-scale-factor=1",
                "--default-background-color=00000000",
                "--allow-file-access-from-files",
                f"--user-data-dir={tmp}/profile",
                f"--window-size={width},{height}",
                f"--screenshot={out_path}",
                page.as_uri(),
            ],
            check=True,
            capture_output=True,
            timeout=120,
        )


def page(body: str, width: int, height: int, extra_css: str = "") -> str:
    return f"""<!doctype html><html><head><meta charset="utf-8"><style>
{font_face_css()}
html, body {{ margin: 0; padding: 0; width: {width}px; height: {height}px; overflow: hidden; background: transparent; }}
body {{ font-family: 'Pretendard', sans-serif; -webkit-font-smoothing: antialiased; }}
{extra_css}
</style></head><body>{body}</body></html>"""
