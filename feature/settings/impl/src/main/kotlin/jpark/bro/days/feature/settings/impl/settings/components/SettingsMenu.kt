package jpark.bro.days.feature.settings.impl.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeSize
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSection
import jpark.bro.days.feature.settings.impl.R
import jpark.bro.days.feature.settings.impl.settings.SettingsAction
import jpark.bro.days.feature.settings.impl.settings.SettingsDestination
import jpark.bro.days.feature.settings.impl.settings.SettingsState
import jpark.bro.days.feature.settings.impl.settings.SupportItem
import jpark.bro.days.core.designsystem.R as DesignR

/** 프로필 카드 + 메뉴 섹션 (product-spec 6장) */
@Composable
internal fun SettingsMenu(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    ProfileCard(
        profile = state.profile,
        onEditClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.PROFILE)) },
    )

    DaysSection(title = stringResource(R.string.settings_main_section_couple)) {
        SettingsMenuRow(
            icon = DesignR.drawable.ic_favorite,
            title = stringResource(R.string.settings_main_couple_manage),
            onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.COUPLE)) },
            trailing = {
                if (state.isCoupled) {
                    val partner = state.partnerName
                    DaysBadge(
                        text =
                            if (partner != null) {
                                stringResource(R.string.settings_main_couple_connected, partner)
                            } else {
                                stringResource(R.string.settings_main_couple_connected_unnamed)
                            },
                        tone = DaysBadgeTone.SUCCESS,
                        size = DaysBadgeSize.MEDIUM,
                        showDot = true,
                    )
                } else {
                    Text(
                        stringResource(R.string.settings_main_couple_connect),
                        style = typography.labelMd,
                        color = colors.primary,
                    )
                }
            },
        )
    }

    DaysSection(title = stringResource(R.string.settings_main_section_app)) {
        SettingsMenuRow(
            icon = DesignR.drawable.ic_palette,
            title = stringResource(R.string.settings_main_theme),
            onClick = { onAction(SettingsAction.ThemeClicked) },
            trailing = {
                DaysBadge(
                    text = stringResource(state.themeSetting.type.label.name),
                    tone = DaysBadgeTone.SHARED,
                    size = DaysBadgeSize.MEDIUM,
                    showDot = true,
                )
            },
        )
        DaysDivider(inset = AppSpacing.marginMobile)
        SettingsMenuRow(
            icon = DesignR.drawable.ic_notifications,
            title = stringResource(R.string.settings_main_notification),
            subtitle = stringResource(R.string.settings_main_notification_desc),
            onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.NOTIFICATION)) },
        )
        DaysDivider(inset = AppSpacing.marginMobile)
        SettingsMenuRow(
            icon = DesignR.drawable.ic_widgets,
            title = stringResource(R.string.settings_main_widget),
            onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.WIDGET)) },
            trailing = {
                Text(
                    stringResource(R.string.settings_main_widget_guide),
                    style = typography.bodySm,
                    color = colors.textSecondary,
                )
            },
        )
    }

    DaysSection(title = stringResource(R.string.settings_main_section_support)) {
        SettingsMenuRow(
            icon = DesignR.drawable.ic_campaign,
            title = stringResource(R.string.settings_main_notice),
            onClick = { onAction(SettingsAction.SupportClicked(SupportItem.NOTICE)) },
        )
        DaysDivider(inset = AppSpacing.marginMobile)
        SettingsMenuRow(
            icon = DesignR.drawable.ic_help,
            title = stringResource(R.string.settings_main_inquiry),
            onClick = { onAction(SettingsAction.SupportClicked(SupportItem.INQUIRY)) },
        )
        DaysDivider(inset = AppSpacing.marginMobile)
        SettingsMenuRow(
            icon = DesignR.drawable.ic_star,
            title = stringResource(R.string.settings_main_rate),
            onClick = { onAction(SettingsAction.SupportClicked(SupportItem.RATE_APP)) },
            trailing = {
                Text(
                    stringResource(R.string.settings_main_rate_action),
                    style = typography.labelMd,
                    color = colors.primary,
                )
            },
        )
    }

    DaysSection(title = stringResource(R.string.settings_main_section_info)) {
        SettingsMenuRow(
            icon = DesignR.drawable.ic_shield,
            title = stringResource(R.string.settings_main_policy),
            onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.POLICY)) },
        )
        DaysDivider(inset = AppSpacing.marginMobile)
        SettingsMenuRow(
            icon = DesignR.drawable.ic_apps,
            title = stringResource(R.string.settings_main_more_apps),
            onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.MORE_APPS)) },
        )
    }

    DaysSection(title = stringResource(R.string.settings_main_section_account)) {
        SettingsMenuRow(
            icon = DesignR.drawable.ic_manage_accounts,
            title = stringResource(R.string.settings_main_account),
            subtitle = state.accountEmail,
            onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.ACCOUNT)) },
        )
    }
}

/** 설정 메뉴 행: primary-soft 아이콘 + 제목 · 설명 + 끝 표시 + 화살표 */
@Composable
private fun SettingsMenuRow(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    DaysListItem(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        leading = { DaysIconBadge(icon = icon, tone = DaysIconBadgeTone.SHARED) },
        trailing = trailing,
    )
}
