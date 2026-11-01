package jpark.bro.days.feature.couple.impl.couplemanage

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey

internal class CoupleManageViewModel :
    MviViewModel<CoupleManageState, CoupleManageAction, CoupleManageEvent>(CoupleManageState()) {
    override fun onAction(action: CoupleManageAction) {
        when (action) {
            CoupleManageAction.BackClicked -> sendEvent(CoupleManageEvent.NavigateBack)
        }
    }
}
