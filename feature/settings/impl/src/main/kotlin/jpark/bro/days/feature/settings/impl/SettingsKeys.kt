package jpark.bro.days.feature.settings.impl

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 이 기능 안에서만 이동하는 화면 키 */
@Serializable
internal data object ProfileEditKey : NavKey

@Serializable
internal data object NotificationSettingsKey : NavKey

@Serializable
internal data object WidgetSettingsKey : NavKey

@Serializable
internal data object AccountKey : NavKey

@Serializable
internal data object WithdrawKey : NavKey

@Serializable
internal data object PolicyListKey : NavKey

@Serializable
internal data class PolicyDocKey(
    val type: PolicyType,
) : NavKey

@Serializable
internal data object LicensesKey : NavKey

@Serializable
internal data class LicenseDetailKey(
    val id: String,
) : NavKey
