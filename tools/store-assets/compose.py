"""원본 캡처(store-assets/raw)에 배경 · 문구 · 기기 프레임을 얹어 스토어 스크린샷을 만든다.

    phone      1080x1920  (기기 아래쪽이 화면 밖으로 이어지는 구도)
    tablet-7   1440x2560  (기기 전체가 보이는 구도)
    tablet-10  1800x3200

모두 9:16 세로, 알파 없는 24비트 PNG. 색 · 글꼴은 앱 테마(핑크 라이트) · Pretendard.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

from brand import COLORS, OUT_DIR, font_path, icon_option, RECOMMENDED_ICON

RAW_DIR = OUT_DIR / "raw"
SHOT_DIR = OUT_DIR / "screenshots"
SS = 2  # 2배로 그린 뒤 줄여서 가장자리를 부드럽게 한다

# (원본, 문구, 위 작은 라벨). 문구는 기능 이름이 아니라 사용자가 얻는 것. 라벨은 혼자 · 둘이 쓰는 모습을 구분할 때만
SCREENS = [
    ("01_couple_home.png", "둘이 함께한 날을 한눈에", "둘이 쓸 때"),
    ("02_solo_home.png", "혼자여도 충분한 디데이", "혼자 쓸 때"),
    ("03_detail.png", "기념일은 알아서 챙겨요", None),
    ("04_calendar.png", "둘의 약속도 달력 하나에", None),
    ("05_widget.png", "앱을 열지 않아도 바로", None),
    ("06_dark_calendar.png", "내 취향대로 고르는 테마", None),
]

SPECS = {
    "phone": {
        "size": (1080, 1920),
        "device_width": 0.78,  # 캔버스 너비 대비
        "device_top": 0.215,  # 캔버스 높이 대비
        "bezel": 14,
        "radius": 54,
        "font": 74,
        "caption_y": 0.125,
    },
    "tablet-7": {
        "size": (1440, 2560),
        "device_width": 0.80,
        "device_top": 0.19,
        "bezel": 22,
        "radius": 36,
        "font": 100,
        "caption_y": 0.112,
    },
    "tablet-10": {
        "size": (1800, 3200),
        "device_width": 0.80,
        "device_top": 0.19,
        "bezel": 26,
        "radius": 44,
        "font": 124,
        "caption_y": 0.112,
    },
}

# 홈 화면 위젯 캡처는 아래쪽에 다른 앱 아이콘(독)이 있어 기기를 더 크게 놓아 화면 밖으로 밀어낸다
ZOOM = {("phone", "05_widget.png"): 0.92}

BEZEL_COLOR = "#1C1418"
BEZEL_EDGE = "#3A2A31"


def rgb(value: str) -> tuple[int, int, int]:
    value = value.lstrip("#")
    return tuple(int(value[i : i + 2], 16) for i in (0, 2, 4))


def font(weight: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(font_path(weight)), size)


def fit_font(text: str, size: int, max_width: int) -> ImageFont.FreeTypeFont:
    """문구가 max_width 안에 들어올 때까지 글자 크기를 줄인다."""
    while size > 20:
        candidate = font("extra_bold", size)
        left, _, right, _ = candidate.getbbox(text)
        if right - left <= max_width:
            return candidate
        size -= 2
    return font("extra_bold", size)


def background(width: int, height: int) -> Image.Image:
    """primary-soft → 흰색 세로 그라데이션 + 옅은 원 장식 (대표 그래픽과 같은 결)."""
    top, bottom = rgb(COLORS["primary-soft"]), rgb("#FFFFFF")
    gradient = Image.new("RGB", (1, 256))
    for y in range(256):
        t = y / 255
        gradient.putpixel((0, y), tuple(round(a + (b - a) * t) for a, b in zip(top, bottom)))
    canvas = gradient.resize((width, height), Image.BICUBIC).convert("RGBA")
    deco = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(deco)
    r = width * 0.42
    draw.ellipse((-r * 0.9, -r * 0.8, r * 1.1, r * 1.2), fill=rgb(COLORS["primary"]) + (16,))
    r2 = width * 0.30
    draw.ellipse((width - r2 * 1.2, height * 0.55, width + r2 * 0.8, height * 0.55 + r2 * 2), fill=rgb(COLORS["accent"]) + (30,))
    canvas.alpha_composite(deco)
    return canvas


def draw_label(canvas: Image.Image, text: str, center_x: float, bottom: float, size: float) -> None:
    """문구 위의 작은 알약 라벨 (primary 글자 · 흰 바탕)."""
    label_font = font("bold", round(size))
    left, top, right, bottom_px = label_font.getbbox(text)
    pad_x, pad_y = size * 0.75, size * 0.42
    w, h = right - left + pad_x * 2, bottom_px - top + pad_y * 2
    box = (center_x - w / 2, bottom - h, center_x + w / 2, bottom)
    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle(box, radius=h / 2, fill=(255, 255, 255, 255))
    draw.text((box[0] + pad_x - left, box[1] + pad_y - top), text, font=label_font, fill=rgb(COLORS["primary"]) + (255,))


def compose(kind: str, raw_path: Path, caption: str, label: str | None, out_path: Path) -> None:
    spec = SPECS[kind]
    width, height = spec["size"]
    W, H = width * SS, height * SS
    canvas = background(W, H)
    draw = ImageDraw.Draw(canvas)

    # 문구 (가운데 정렬)
    caption_font = fit_font(caption, spec["font"] * SS, int(W * 0.88))
    left, top, right, bottom = caption_font.getbbox(caption)
    text_x = (W - (right - left)) / 2 - left
    center_y = H * spec["caption_y"]
    text_y = center_y - (bottom - top) / 2 - top
    draw.text((text_x, text_y), caption, font=caption_font, fill=rgb(COLORS["text-primary"]) + (255,))
    if label:
        draw_label(canvas, label, W / 2, text_y + top - spec["font"] * SS * 0.42, spec["font"] * SS * 0.46)

    # 기기
    shot = Image.open(raw_path).convert("RGB")
    device_w = round(W * ZOOM.get((kind, raw_path.name), spec["device_width"]))
    device_h = round(device_w * shot.height / shot.width)
    x = (W - device_w) // 2
    y = round(H * spec["device_top"])
    bezel = spec["bezel"] * SS
    radius = spec["radius"] * SS
    outer = (x - bezel, y - bezel, x + device_w + bezel, y + device_h + bezel)

    shadow = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        (outer[0], outer[1] + 20 * SS, outer[2], outer[3] + 20 * SS), radius=radius + bezel, fill=rgb(COLORS["primary"]) + (70,)
    )
    canvas.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(30 * SS)))

    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle(outer, radius=radius + bezel, fill=rgb(BEZEL_COLOR) + (255,))
    draw.rounded_rectangle(outer, radius=radius + bezel, outline=rgb(BEZEL_EDGE) + (255,), width=2 * SS)

    screen = shot.resize((device_w, device_h), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (device_w, device_h), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, device_w, device_h), radius=radius, fill=255)
    canvas.paste(screen, (x, y), mask)

    final = canvas.resize((width, height), Image.LANCZOS).convert("RGB")  # 알파 제거
    out_path.parent.mkdir(parents=True, exist_ok=True)
    final.save(out_path, "PNG", optimize=True)


def main() -> None:
    assert icon_option(RECOMMENDED_ICON)  # 색 체계가 아이콘과 같은지 확인용 (같은 brand.COLORS)
    for kind in SPECS:
        for old in (SHOT_DIR / kind).glob("*.png"):
            old.unlink()
        index = 0
        for file_name, caption, label in SCREENS:
            raw_path = RAW_DIR / kind / file_name
            if not raw_path.exists():
                print(f"건너뜀 (원본 없음): {raw_path.relative_to(OUT_DIR.parent)}")
                continue
            index += 1
            out_path = SHOT_DIR / kind / f"{index:02d}.png"
            compose(kind, raw_path, caption, label, out_path)
            print(f"{out_path.relative_to(OUT_DIR.parent)}  ← {file_name}  「{caption}」")


if __name__ == "__main__":
    main()
