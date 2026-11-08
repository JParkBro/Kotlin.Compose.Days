package jpark.bro.days.feature.settings.impl.policydoc

internal sealed interface PolicyDocEvent {
    data object NavigateBack : PolicyDocEvent
}
