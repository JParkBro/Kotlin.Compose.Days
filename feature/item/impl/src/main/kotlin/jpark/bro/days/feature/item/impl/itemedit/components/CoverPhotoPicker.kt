package jpark.bro.days.feature.item.impl.itemedit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface
import jpark.bro.days.core.ui.DaysPhoto
import jpark.bro.days.feature.item.impl.R
import java.io.File
import jpark.bro.days.core.designsystem.R as DesignR

/**
 * 디데이 커버 사진 1장 (product-spec 4-5, 시안 item-edit-dday). 없으면 "사진 추가" 칸.
 * [remainingQuota]가 있으면(우리 둘이 항목) 커플이 더 올릴 수 있는 사진 수를 위에 보여준다
 */
@Composable
internal fun CoverPhotoPicker(
    file: File?,
    hasPhoto: Boolean,
    isWorking: Boolean,
    remainingQuota: Int?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (remainingQuota != null) {
            Text(
                text = stringResource(R.string.item_edit_photo_remaining, remainingQuota),
                style = AppThemeTokens.typography.labelMd,
                color = colors.textSecondary,
                modifier = Modifier.testTag("item_edit_photo_remaining"),
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PhotoHeight)
                    .clip(AppShapes.lg)
                    .background(colors.fill)
                    .clickable(enabled = !isWorking, onClick = onPick)
                    .testTag("item_edit_photo"),
            contentAlignment = Alignment.Center,
        ) {
            if (hasPhoto) {
                DaysPhoto(
                    file = file,
                    contentDescription = stringResource(R.string.item_edit_photo_description),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_add),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.item_edit_photo_add),
                        style = AppThemeTokens.typography.labelMd,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        if (hasPhoto) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DaysSoftButton(
                    text = stringResource(R.string.item_edit_photo_change),
                    onClick = onPick,
                    enabled = !isWorking,
                    size = DaysButtonSize.SMALL,
                )
                DaysSecondaryButton(
                    text = stringResource(R.string.item_edit_photo_remove),
                    onClick = onRemove,
                    enabled = !isWorking,
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("item_edit_photo_remove"),
                )
            }
        }
    }
}

private val PhotoHeight = 140.dp

@ComponentPreviews
@Composable
private fun CoverPhotoPickerPreview() {
    DaysPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            CoverPhotoPicker(
                file = null,
                hasPhoto = false,
                isWorking = false,
                remainingQuota = null,
                onPick = {},
                onRemove = {},
            )
            CoverPhotoPicker(
                file = null,
                hasPhoto = false,
                isWorking = false,
                remainingQuota = 12,
                onPick = {},
                onRemove = {},
            )
        }
    }
}
