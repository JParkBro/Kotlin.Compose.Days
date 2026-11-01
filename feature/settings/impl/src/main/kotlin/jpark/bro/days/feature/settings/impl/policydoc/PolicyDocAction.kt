package jpark.bro.days.feature.settings.impl.policydoc

internal sealed interface PolicyDocAction {
    data object Reload : PolicyDocAction
}
