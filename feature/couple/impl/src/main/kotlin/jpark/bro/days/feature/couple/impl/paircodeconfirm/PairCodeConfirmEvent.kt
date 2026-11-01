package jpark.bro.days.feature.couple.impl.paircodeconfirm

import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import jpark.bro.days.feature.home.api.HomeKey

internal sealed interface PairCodeConfirmEvent {
    data object Connected : PairCodeConfirmEvent

    data object NavigateBack : PairCodeConfirmEvent
}
