package jpark.bro.days.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface

/** 포커스 바깥 링 두께 (theme-guide.md 6장: primary-soft 3px) */
private val FocusRingWidth = 3.dp

/**
 * 입력창. 기본 surface + border-strong, 포커스 시 primary 1.5dp + 바깥 primary-soft 링.
 * 링은 바깥에 그리기만 하므로 포커스가 바뀌어도 크기 · 위치가 변하지 않는다.
 */
@Composable
fun DaysTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = AppShapes.default
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        textStyle = typography.bodyMd.copy(color = if (enabled) colors.textPrimary else colors.textTertiary),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier =
                    Modifier
                        .heightIn(min = 48.dp)
                        .drawBehind {
                            if (focused) {
                                val ring = FocusRingWidth.toPx()
                                val outline =
                                    shape.createOutline(
                                        Size(size.width + ring * 2, size.height + ring * 2),
                                        layoutDirection,
                                        this,
                                    )
                                translate(left = -ring, top = -ring) { drawOutline(outline, colors.primarySoft) }
                            }
                        }.background(if (enabled) colors.surface else colors.fill, shape)
                        .border(
                            width = if (focused) 1.5.dp else 1.dp,
                            color = if (focused) colors.primary else colors.borderStrong,
                            shape = shape,
                        ).padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty() && placeholder != null) {
                    Text(text = placeholder, style = typography.bodyMd, color = colors.textTertiary)
                }
                innerTextField()
            }
        },
    )
}

@ComponentPreviews
@Composable
private fun DaysTextFieldPreview() {
    DaysPreviewSurface {
        var title by remember { mutableStateOf("") }
        DaysTextField(value = title, onValueChange = { title = it }, placeholder = "디데이 이름")
        DaysTextField(value = "전역일", onValueChange = {})
        DaysTextField(value = "수정할 수 없음", onValueChange = {}, enabled = false)
    }
}
