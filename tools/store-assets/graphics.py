"""앱 아이콘(시안 3개 + 최종 512) · 적응형 아이콘 리소스 · 대표 그래픽(1024x500)을 만든다.

    store-assets/icon/options/{key}.png       시안 (512, 같은 규격)
    store-assets/icon/options/{key}.svg
    store-assets/icon/options/overview.png    시안 비교 (런처 모양 미리보기 포함)
    store-assets/icon/icon-512.png            최종 (brand.RECOMMENDED_ICON), 32비트 RGBA
    store-assets/icon/android-res/            앱에 넣을 적응형 아이콘 리소스 (기존 app 리소스는 덮어쓰지 않는다)
    store-assets/feature-graphic/feature-1024x500.png   24비트 RGB
"""

from __future__ import annotations

import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from brand import (
    APP_NAME,
    COLORS,
    ICON_OPTIONS,
    OUT_DIR,
    RECOMMENDED_ICON,
    STORE_ICON_VIEW,
    TAGLINE,
    font_path,
    icon_option,
    icon_svg,
    page,
    render_html,
    symbol_svg,
)

ICON_DIR = OUT_DIR / "icon"
RES_DIR = ICON_DIR / "android-res"
FEATURE_DIR = OUT_DIR / "feature-graphic"

LEGACY_SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def render_svg(svg: str, size: int, out_path: Path) -> Image.Image:
    render_html(page(svg, size, size, "svg { display: block; }"), size, size, out_path)
    image = Image.open(out_path).convert("RGBA")
    if image.size != (size, size):
        image = image.crop((0, 0, size, size))
    image.save(out_path, "PNG", optimize=True)
    return image


# ---------------------------------------------------------------- 아이콘


def make_icon_options() -> None:
    options_dir = ICON_DIR / "options"
    previews = []
    for option in ICON_OPTIONS:
        svg = icon_svg(option, 512, STORE_ICON_VIEW)
        (options_dir / f"{option['key']}.svg").parent.mkdir(parents=True, exist_ok=True)
        (options_dir / f"{option['key']}.svg").write_text(svg, encoding="utf-8")
        image = render_svg(svg, 512, options_dir / f"{option['key']}.png")
        # 런처 미리보기: 108 전체를 원으로 잘라 가운데 72만 보이게 (적응형 아이콘 원형 마스크)
        with tempfile.TemporaryDirectory() as tmp:
            launcher = render_svg(icon_svg(option, 512, (18, 18, 72, 72)), 512, Path(tmp) / "l.png")
        mask = Image.new("L", (512, 512), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, 511, 511), fill=255)
        launcher.putalpha(mask)
        previews.append((option, image, launcher))
        print(f"시안 {option['key']}: {option['title']}")

    # 비교 시트: 시안마다 [Play 아이콘(둥근 사각) · 런처(원) · 작게(48)]
    cell = 560
    sheet = Image.new("RGB", (cell * len(previews), 450), COLORS["bg"])
    for index, (option, image, launcher) in enumerate(previews):
        x = index * cell + 24
        rounded = Image.new("L", (512, 512), 0)
        ImageDraw.Draw(rounded).rounded_rectangle((0, 0, 511, 511), radius=100, fill=255)
        tile = image.copy()
        tile.putalpha(rounded)
        sheet.paste(tile.resize((300, 300), Image.LANCZOS), (x, 40), tile.resize((300, 300), Image.LANCZOS))
        small = launcher.resize((160, 160), Image.LANCZOS)
        sheet.paste(small, (x + 330, 40), small)
        tiny = launcher.resize((48, 48), Image.LANCZOS)
        sheet.paste(tiny, (x + 386, 230), tiny)
        mark = "  ← 추천" if option["key"] == RECOMMENDED_ICON else ""
        draw = ImageDraw.Draw(sheet)
        draw.text((x, 380), option["title"] + mark, font=ImageFont.truetype(str(font_path("bold")), 34), fill=COLORS["text-primary"])
    sheet.save(options_dir / "overview.png", "PNG", optimize=True)


def make_final_icon() -> None:
    option = icon_option(RECOMMENDED_ICON)
    out = ICON_DIR / "icon-512.png"
    render_svg(icon_svg(option, 512, STORE_ICON_VIEW), 512, out)
    print(f"최종 아이콘: {out.relative_to(OUT_DIR.parent)} ({option['title']})")


def vector_drawable(option: dict, monochrome: bool = False) -> str:
    paths = []
    for layer in option["layers"]:
        if "stroke" in layer:
            color = "#FF000000" if monochrome else "#FF" + layer["stroke"].lstrip("#").upper()
            if monochrome and layer["stroke"] == option["background"]:
                continue  # 배경색 테두리(구분선)는 단색 아이콘에서 뺀다
            paths.append(
                f'    <path\n        android:pathData="{layer["d"]}"\n        android:fillColor="#00000000"\n'
                f'        android:strokeColor="{color}"\n        android:strokeWidth="{layer["width"]}"\n'
                f'        android:strokeLineCap="round" />'
            )
        else:
            if monochrome and layer["fill"] == option["background"]:
                color = "#00000000"  # 배경색으로 뚫은 부분은 단색 아이콘에서도 비운다
            else:
                color = "#FF000000" if monochrome else "#FF" + layer["fill"].lstrip("#").upper()
            paths.append(f'    <path\n        android:pathData="{layer["d"]}"\n        android:fillColor="{color}" />')
    body = "\n".join(paths)
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- tools/store-assets/graphics.py가 만든 파일. 심볼 원본: tools/store-assets/brand.py -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n    android:height="108dp"\n'
        '    android:viewportWidth="108"\n    android:viewportHeight="108">\n'
        f"{body}\n</vector>\n"
    )


def make_android_res() -> None:
    """적응형 아이콘(API 26+, 단색 테마 아이콘 포함) + 해상도별 PNG. app/src/main/res에 그대로 덮어 쓸 수 있는 구조."""
    option = icon_option(RECOMMENDED_ICON)
    adaptive = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@color/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
        "</adaptive-icon>\n"
    )
    files = {
        "mipmap-anydpi-v26/ic_launcher.xml": adaptive,
        "mipmap-anydpi-v26/ic_launcher_round.xml": adaptive,
        "drawable/ic_launcher_foreground.xml": vector_drawable(option),
        "drawable/ic_launcher_monochrome.xml": vector_drawable(option, monochrome=True),
        "values/ic_launcher_background.xml": (
            '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
            f'    <color name="ic_launcher_background">{option["background"]}</color>\n</resources>\n'
        ),
    }
    for relative, text in files.items():
        path = RES_DIR / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    # API 26 미만 · 런처가 적응형을 못 쓸 때의 PNG (정사각 · 원형)
    with tempfile.TemporaryDirectory() as tmp:
        square = render_svg(icon_svg(option, 512, STORE_ICON_VIEW), 512, Path(tmp) / "s.png")
        full = render_svg(icon_svg(option, 512, (18, 18, 72, 72)), 512, Path(tmp) / "f.png")
    rounded_mask = Image.new("L", (512, 512), 0)
    ImageDraw.Draw(rounded_mask).rounded_rectangle((0, 0, 511, 511), radius=96, fill=255)
    circle_mask = Image.new("L", (512, 512), 0)
    ImageDraw.Draw(circle_mask).ellipse((0, 0, 511, 511), fill=255)
    square.putalpha(rounded_mask)
    full.putalpha(circle_mask)
    for density, size in LEGACY_SIZES.items():
        folder = RES_DIR / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        square.resize((size, size), Image.LANCZOS).save(folder / "ic_launcher.png", optimize=True)
        full.resize((size, size), Image.LANCZOS).save(folder / "ic_launcher_round.png", optimize=True)
    # Android Studio가 만드는 Play용 원본과 같은 이름
    (RES_DIR / "ic_launcher-playstore.png").write_bytes((ICON_DIR / "icon-512.png").read_bytes())
    print(f"적응형 아이콘 리소스: {RES_DIR.relative_to(OUT_DIR.parent)}")


# ---------------------------------------------------------------- 대표 그래픽


def make_feature_graphic() -> None:
    option = icon_option(RECOMMENDED_ICON)
    c = COLORS
    width, height = 1024, 500
    icon = icon_svg(option, 168, STORE_ICON_VIEW)
    deco = (
        # 가장자리 장식: 옅은 원. 잘리거나 재생 버튼이 겹쳐도 상관없는 요소만 둔다
        f'<div class="blob" style="width:420px;height:420px;left:-150px;top:-170px;background:{c["primary"]};opacity:.07"></div>'
        f'<div class="blob" style="width:320px;height:320px;right:-90px;bottom:-150px;background:{c["accent"]};opacity:.14"></div>'
        f'<div class="blob" style="width:90px;height:90px;right:120px;top:54px;background:{c["primary"]};opacity:.08"></div>'
    )
    body = f"""
<div class="canvas">
  {deco}
  <div class="center">
    <div class="icon">{icon}</div>
    <div class="text">
      <div class="name">{APP_NAME}</div>
      <div class="tagline">{TAGLINE}<br><b>한 곳에서</b></div>
    </div>
  </div>
</div>"""
    css = f"""
.canvas {{ position: relative; width: {width}px; height: {height}px; overflow: hidden;
  background: linear-gradient(135deg, {c["primary-soft"]} 0%, #FFFFFF 55%, {c["accent-soft"]} 100%); }}
.blob {{ position: absolute; border-radius: 50%; }}
.center {{ position: absolute; left: 0; right: 0; top: 0; bottom: 0; display: flex; align-items: center;
  justify-content: center; gap: 44px; }}
.icon {{ width: 168px; height: 168px; border-radius: 38px; overflow: hidden;
  box-shadow: 0 18px 40px rgba(210, 60, 105, .22); flex: none; }}
.icon svg {{ display: block; }}
.name {{ font-weight: 800; font-size: 76px; letter-spacing: -2px; color: {c["text-primary"]}; line-height: 1.05; }}
.tagline {{ margin-top: 14px; font-weight: 500; font-size: 30px; line-height: 1.38; color: {c["text-secondary"]};
  letter-spacing: -0.6px; }}
.tagline b {{ font-weight: 700; color: {c["primary"]}; }}
"""
    out = FEATURE_DIR / "feature-1024x500.png"
    render_html(page(body, width, height, css), width, height, out)
    image = Image.open(out).convert("RGB")  # 알파 제거
    image.crop((0, 0, width, height)).save(out, "PNG", optimize=True)
    print(f"대표 그래픽: {out.relative_to(OUT_DIR.parent)}")


def main() -> None:
    make_icon_options()
    make_final_icon()
    make_android_res()
    make_feature_graphic()


if __name__ == "__main__":
    main()
