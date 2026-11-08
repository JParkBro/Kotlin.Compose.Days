package jpark.bro.days.feature.settings.impl.policydoc

internal sealed interface PolicyDocAction {
    data object PageLoaded : PolicyDocAction

    data object PageFailed : PolicyDocAction

    data object RetryClicked : PolicyDocAction

    data object BackClicked : PolicyDocAction
}
