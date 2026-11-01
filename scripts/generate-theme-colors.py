#!/usr/bin/env python3
"""docs/days/assets/theme-tokens.json → core:designsystem ThemeTokens.kt 생성.

사용: python3 scripts/generate-theme-colors.py
값을 바꿀 때는 JSON만 고치고 이 스크립트를 다시 실행한다 (theme-guide.md).
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "docs/days/assets/theme-tokens.json"
OUT = ROOT / "core/designsystem/src/main/kotlin/jpark/bro/days/core/designsystem/ThemeTokens.kt"


def camel(name):
    return re.sub(r"-(\w)", lambda m: m.group(1).upper(), name)


def argb(value):
    """'#RRGGBB' | 'rgba(r,g,b,a)' → 0xAARRGGBB"""
    value = value.strip()
    if value.startswith("#"):
        return f"0xFF{value[1:].upper()}"
    m = re.fullmatch(r"rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)", value)
    if not m:
        raise ValueError(f"지원하지 않는 색 형식: {value}")
    r, g, b = (int(m.group(i)) for i in range(1, 4))
    a = round(float(m.group(4)) * 255)
    return f"0x{a:02X}{r:02X}{g:02X}{b:02X}"


def main():
    data = json.loads(SRC.read_text(encoding="utf-8"))
    tokens = [t["name"] for t in data["tokens"]]
    out = [
        "// 생성 파일 — 직접 고치지 않는다. 원본: docs/days/assets/theme-tokens.json",
        "// 다시 만들기: python3 scripts/generate-theme-colors.py",
        '@file:Suppress("MagicNumber")',
        "",
        "package jpark.bro.days.core.designsystem",
        "",
        "import androidx.compose.ui.graphics.Color",
        "import jpark.bro.days.core.model.AppThemeType",
        "",
    ]
    names = {}
    for theme, spec in data["themes"].items():
        for mode in ("light", "dark"):
            palette = spec[mode]
            missing = set(tokens) - set(palette)
            if missing:
                raise ValueError(f"{theme}.{mode}에 없는 토큰: {sorted(missing)}")
            val = f"{camel(theme).capitalize()}{mode.capitalize()}"
            names[(theme, mode)] = val
            out.append(f"/** {spec['name']} · {'라이트' if mode == 'light' else '다크'} */")
            out.append(f"internal val {val} =")
            out.append("    AppColors(")
            out += [f"        {camel(t)} = Color({argb(palette[t])})," for t in tokens]
            out += ["    )", ""]

    out.append("internal fun palette(")
    out.append("    type: AppThemeType,")
    out.append("    dark: Boolean,")
    out.append("): AppColors =")
    out.append("    when (type) {")
    for theme in data["themes"]:
        enum = theme.upper()
        out.append(f"        AppThemeType.{enum} -> if (dark) {names[(theme, 'dark')]} else {names[(theme, 'light')]}")
    out += ["    }", ""]

    out.append("/** 테마와 무관한 고정 색 (theme-guide 4장) */")
    out.append("object AppFixedColors {")
    out += [f"    val {camel(k)} = Color({argb(v)})" for k, v in data["fixed"].items()]
    out.append("}")

    OUT.write_text("\n".join(out) + "\n", encoding="utf-8")
    print(f"{OUT.relative_to(ROOT)}: {len(names)} palettes × {len(tokens)} tokens")


if __name__ == "__main__":
    main()
