package jpark.bro.days.feature.couple.impl.couplemanage

import jpark.bro.days.core.ui.MviViewModel

internal class CoupleManageViewModel :
    MviViewModel<CoupleManageState, CoupleManageAction, CoupleManageEvent>(CoupleManageState()) {
    override fun onAction(action: CoupleManageAction) {
        when (action) {
            CoupleManageAction.BackClicked -> sendEvent(CoupleManageEvent.NavigateBack)
        }
    }
}
