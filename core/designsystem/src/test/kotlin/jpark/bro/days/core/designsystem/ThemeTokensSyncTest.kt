package jpark.bro.days.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import jpark.bro.days.core.model.AppThemeType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import kotlin.math.roundToInt

/** ThemeTokens.kt(생성 파일)가 원본 theme-tokens.json과 같은지 확인한다. */
class ThemeTokensSyncTest {
    private val json: JsonObject =
        Json.parseToJsonElement(File("../../docs/days/assets/theme-tokens.json").readText()).jsonObject

    @Test
    fun `8개 팔레트의 27개 토큰이 JSON과 같다`() {
        val tokenNames =
            json.getValue("tokens").jsonArray.map {
                it.jsonObject
                    .getValue("name")
                    .jsonPrimitive.content
            }
        assertEquals(27, tokenNames.size)

        json.getValue("themes").jsonObject.forEach { (theme, spec) ->
            listOf(false, true).forEach { dark ->
                val expected = spec.jsonObject.getValue(if (dark) "dark" else "light").jsonObject
                val actual = palette(AppThemeType.valueOf(theme.uppercase()), dark).byToken()

                assertEquals("$theme 토큰 목록", tokenNames.toSet(), actual.keys)
                tokenNames.forEach { token ->
                    assertEquals(
                        "$theme ${if (dark) "dark" else "light"} $token",
                        parse(expected.getValue(token).jsonPrimitive.content),
                        actual.getValue(token).toArgb(),
                    )
                }
            }
        }
    }

    @Test
    fun `고정 색이 JSON과 같다`() {
        val fixed = json.getValue("fixed").jsonObject
        val actual =
            mapOf(
                "kakao" to AppFixedColors.kakao,
                "on-kakao" to AppFixedColors.onKakao,
                "on-image" to AppFixedColors.onImage,
                "image-scrim" to AppFixedColors.imageScrim,
            )

        assertEquals(fixed.keys, actual.keys)
        fixed.forEach { (name, value) ->
            assertEquals(name, parse(value.jsonPrimitive.content), actual.getValue(name).toArgb())
        }
    }

    private fun parse(value: String): Int {
        if (value.startsWith("#")) return (0xFF000000 or value.removePrefix("#").toLong(16)).toInt()
        val rgba =
            value
                .removePrefix("rgba(")
                .removeSuffix(")")
                .split(",")
                .map { it.trim() }
        val alpha = (rgba[3].toFloat() * 255).roundToInt()
        return Color(rgba[0].toInt(), rgba[1].toInt(), rgba[2].toInt(), alpha).toArgb()
    }

    private fun AppColors.byToken(): Map<String, Color> =
        mapOf(
            "primary" to primary,
            "primary-pressed" to primaryPressed,
            "on-primary" to onPrimary,
            "primary-soft" to primarySoft,
            "on-primary-soft" to onPrimarySoft,
            "accent" to accent,
            "accent-soft" to accentSoft,
            "on-accent-soft" to onAccentSoft,
            "bg" to bg,
            "surface" to surface,
            "surface-raised" to surfaceRaised,
            "fill" to fill,
            "overlay" to overlay,
            "border" to border,
            "border-strong" to borderStrong,
            "text-primary" to textPrimary,
            "text-secondary" to textSecondary,
            "text-tertiary" to textTertiary,
            "danger" to danger,
            "danger-soft" to dangerSoft,
            "success" to success,
            "success-soft" to successSoft,
            "warning" to warning,
            "warning-soft" to warningSoft,
            "cal-sunday" to calSunday,
            "cal-saturday" to calSaturday,
            "shadow" to shadow,
        )
}
