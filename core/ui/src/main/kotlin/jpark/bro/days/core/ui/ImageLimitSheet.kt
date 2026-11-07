package jpark.bro.days.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBottomSheet
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.model.ImageUploadQuota

/**
 * 커플 사진 업로드 한도를 다 썼을 때 (ads-strategy.md 3장 보상형 광고). 커버 사진 · 프로필 사진 공용.
 * 광고로 더 늘릴 수 없으면(bonus 상한) 광고 버튼 없이 안내만 한다.
 */
@Composable
fun ImageLimitSheet(
    quota: ImageUploadQuota,
    isWorking: Boolean,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit,
) {
    DaysBottomSheet(onDismissRequest = onDismiss) {
        ImageLimitSheetContent(quota = quota, isWorking = isWorking, onWatchAd = onWatchAd, onDismiss = onDismiss)
    }
}

@Composable
fun ImageLimitSheetContent(
    quota: ImageUploadQuota,
    isWorking: Boolean,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                text = stringResource(R.string.common_image_limit_title),
                style = AppThemeTokens.typography.headlineMd,
                color = AppThemeTokens.colors.textPrimary,
            )
            Text(
                text =
                    if (quota.canEarnBonus) {
                        stringResource(R.string.common_image_limit_desc, quota.limit)
                    } else {
                        stringResource(R.string.common_image_limit_desc_max, quota.limit)
                    },
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
            )
        }
        if (quota.canEarnBonus) {
            DaysPrimaryButton(
                text =
                    stringResource(
                        if (isWorking) R.string.common_image_limit_working else R.string.common_image_limit_watch_ad,
                    ),
                onClick = onWatchAd,
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth().testTag("image_limit_watch_ad"),
            )
        }
        DaysSecondaryButton(
            text = stringResource(R.string.common_image_limit_close),
            onClick = onDismiss,
            enabled = !isWorking,
            modifier = Modifier.fillMaxWidth().testTag("image_limit_close"),
        )
    }
}

@ComponentPreviews
@Composable
private fun ImageLimitSheetPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppThemeTokens.colors.surfaceRaised)) {
            ImageLimitSheetContent(
                quota = ImageUploadQuota(userId = "me", coupleId = "c1", used = 30, bonus = 0),
                isWorking = false,
                onWatchAd = {},
                onDismiss = {},
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun ImageLimitSheetMaxPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppThemeTokens.colors.surfaceRaised)) {
            ImageLimitSheetContent(
                quota = ImageUploadQuota(userId = "me", coupleId = "c1", used = 60, bonus = 30),
                isWorking = false,
                onWatchAd = {},
                onDismiss = {},
            )
        }
    }
}
