package jpark.bro.days.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppFixedColors
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.R
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface

/** 주 버튼 (primary, 눌림 primary-pressed). 화면당 한 곳만 */
@Composable
fun DaysPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DaysButtonSize = DaysButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    DaysButton(
        text = text,
        onClick = onClick,
        container = if (pressed) colors.primaryPressed else colors.primary,
        content = colors.onPrimary,
        modifier = modifier,
        enabled = enabled,
        size = size,
        icon = icon,
        interactionSource = interactionSource,
    )
}

/** 보조 버튼 (취소 등): fill */
@Composable
fun DaysSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DaysButtonSize = DaysButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    DaysButton(text, onClick, colors.fill, colors.textPrimary, modifier, enabled, size, icon)
}

/** 부드러운 강조 버튼 (프로필 수정 등): primary-soft */
@Composable
fun DaysSoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DaysButtonSize = DaysButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    DaysButton(text, onClick, colors.primarySoft, colors.onPrimarySoft, modifier, enabled, size, icon)
}

/** 아웃라인 버튼: 투명 + border-strong */
@Composable
fun DaysOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DaysButtonSize = DaysButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    DaysButton(
        text = text,
        onClick = onClick,
        container = Color.Transparent,
        content = colors.textPrimary,
        modifier = modifier,
        enabled = enabled,
        size = size,
        icon = icon,
        border = BorderStroke(1.dp, colors.borderStrong),
    )
}

/** 카카오 로그인 · 공유 버튼: 테마와 무관하게 카카오 노랑 (theme-guide 4장 고정 색) */
@Composable
fun DaysKakaoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DaysButtonSize = DaysButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    DaysButton(text, onClick, AppFixedColors.kakao, AppFixedColors.onKakao, modifier, enabled, size, icon)
}

/**
 * 위험 버튼 (연결 해제 · 탈퇴 등).
 * 기본은 danger-soft, 마지막 확인처럼 강하게 보여야 하면 [filled] = true (danger + on-primary).
 */
@Composable
fun DaysDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
    size: DaysButtonSize = DaysButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    DaysButton(
        text = text,
        onClick = onClick,
        container = if (filled) colors.danger else colors.dangerSoft,
        content = if (filled) colors.onPrimary else colors.danger,
        modifier = modifier,
        enabled = enabled,
        size = size,
        icon = icon,
    )
}

/** 모든 버튼의 공통 모양: 알약 · 최소 높이(글꼴이 커지면 늘어남) · 비활성 fill + text-tertiary */
@Composable
private fun DaysButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DaysButtonSize = DaysButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = size.minHeight),
        enabled = enabled,
        shape = AppShapes.full,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = container,
                contentColor = content,
                disabledContainerColor = colors.fill,
                disabledContentColor = colors.textTertiary,
            ),
        border = border.takeIf { enabled },
        contentPadding = PaddingValues(horizontal = size.horizontalPadding, vertical = AppSpacing.sm),
        interactionSource = interactionSource,
    ) {
        Row {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(size.iconSize),
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
            }
            Text(
                text = text,
                style = if (size == DaysButtonSize.LARGE) typography.labelLg else typography.labelMd,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun DaysButtonPreview() {
    DaysPreviewSurface {
        val full = Modifier.fillMaxWidth()
        DaysPrimaryButton(text = "저장하기", onClick = {}, modifier = full, icon = R.drawable.ic_check)
        DaysPrimaryButton(text = "저장하기", onClick = {}, modifier = full, enabled = false)
        DaysSecondaryButton(text = "취소", onClick = {}, modifier = full)
        DaysSoftButton(text = "커플 코드로 연결하기", onClick = {}, modifier = full)
        DaysOutlineButton(text = "다른 방법으로 시작", onClick = {}, modifier = full)
        DaysKakaoButton(text = "카카오로 계속하기", onClick = {}, modifier = full)
        DaysDangerButton(text = "연결 해제", onClick = {}, modifier = full)
        DaysDangerButton(text = "탈퇴하기", onClick = {}, modifier = full, filled = true)
        Row {
            DaysSoftButton(text = "수정", onClick = {}, size = DaysButtonSize.SMALL)
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            DaysSecondaryButton(text = "닫기", onClick = {}, size = DaysButtonSize.SMALL)
        }
    }
}
