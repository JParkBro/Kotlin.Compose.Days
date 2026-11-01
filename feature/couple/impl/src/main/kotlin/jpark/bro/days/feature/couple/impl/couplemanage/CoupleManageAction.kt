package jpark.bro.days.feature.couple.impl.couplemanage

import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey

internal sealed interface CoupleManageAction {
    data object BackClicked : CoupleManageAction
}
