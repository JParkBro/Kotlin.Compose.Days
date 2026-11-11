package jpark.bro.days.feature.settings.impl.backup

import jpark.bro.days.core.ads.RewardedAdResult

internal sealed interface BackupAction {
    data object BackupClicked : BackupAction

    data class RewardedAdFinished(
        val result: RewardedAdResult,
    ) : BackupAction

    data object RestoreClicked : BackupAction

    data object RestoreConfirmed : BackupAction

    data object RestoreDismissed : BackupAction

    data object BackClicked : BackupAction
}
