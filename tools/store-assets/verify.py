"""store-assets의 모든 파일이 Play Console 규격에 맞는지 확인하고 표로 출력한다. 하나라도 어긋나면 종료 코드 1."""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

from brand import OUT_DIR

MB = 1024 * 1024

# (glob, 크기, 알파 허용, 최대 용량, 최소 · 최대 장수)
RULES = [
    ("icon/icon-512.png", (512, 512), "required", 1024 * 1024, (1, 1)),
    ("feature-graphic/feature-1024x500.png", (1024, 500), "forbidden", 15 * MB, (1, 1)),
    ("screenshots/phone/*.png", (1080, 1920), "forbidden", 8 * MB, (4, 8)),
    ("screenshots/tablet-7/*.png", (1440, 2560), "forbidden", 8 * MB, (4, 8)),
    ("screenshots/tablet-10/*.png", (1800, 3200), "forbidden", 8 * MB, (4, 8)),
]


def main() -> None:
    rows, failed = [], False
    for pattern, size, alpha, max_bytes, (min_count, max_count) in RULES:
        files = sorted(OUT_DIR.glob(pattern))
        if not min_count <= len(files) <= max_count:
            failed = True
            rows.append((pattern, "-", "-", "-", "-", f"✗ {len(files)}장 (필요 {min_count}~{max_count})"))
        for path in files:
            with Image.open(path) as image:
                has_alpha = image.mode in ("RGBA", "LA", "PA") or "transparency" in image.info
                bits = {"RGB": 24, "RGBA": 32}.get(image.mode, image.mode)
                problems = []
                if image.size != size:
                    problems.append(f"크기 {image.size[0]}x{image.size[1]} ≠ {size[0]}x{size[1]}")
                if image.format not in ("PNG", "JPEG"):
                    problems.append(f"형식 {image.format}")
                if alpha == "forbidden" and has_alpha:
                    problems.append("알파 있음")
                if alpha == "required" and image.mode != "RGBA":
                    problems.append("32비트 아님")
                if path.stat().st_size > max_bytes:
                    problems.append(f"용량 초과 ({max_bytes // 1024}KB)")
                if size[0] != size[1] and pattern.startswith("screenshots") and image.size[0] * 16 != image.size[1] * 9:
                    problems.append("9:16 아님")
                failed |= bool(problems)
                rows.append(
                    (
                        str(path.relative_to(OUT_DIR)),
                        f"{image.size[0]}x{image.size[1]}",
                        f"{image.format} {bits}bit",
                        "있음" if has_alpha else "없음",
                        f"{path.stat().st_size / 1024:,.0f}KB",
                        "✓" if not problems else "✗ " + ", ".join(problems),
                    )
                )

    header = ("파일", "크기", "형식", "알파", "용량", "결과")
    widths = [max(_width(str(row[i])) for row in [header, *rows]) for i in range(len(header))]
    for row in [header, tuple("-" * w for w in widths), *rows]:
        print(" | ".join(_pad(str(cell), widths[i]) for i, cell in enumerate(row)))
    sys.exit(1 if failed else 0)


def _width(text: str) -> int:
    return sum(2 if ord(ch) > 0x1100 else 1 for ch in text)


def _pad(text: str, width: int) -> str:
    return text + " " * (width - _width(text))


if __name__ == "__main__":
    main()
