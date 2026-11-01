package jpark.bro.days.feature.couple.impl.paircodeconfirm

import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import jpark.bro.days.feature.home.api.HomeKey

internal sealed interface PairCodeConfirmAction {
    data object ConnectClicked : PairCodeConfirmAction

    data object BackClicked : PairCodeConfirmAction
}
