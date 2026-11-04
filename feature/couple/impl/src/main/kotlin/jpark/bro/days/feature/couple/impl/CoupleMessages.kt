package jpark.bro.days.feature.couple.impl

import androidx.annotation.StringRes
import jpark.bro.days.core.model.PairCodeCheck

/** 코드를 쓸 수 없는 이유 안내 문구. 유효하면 null */
@StringRes
internal fun PairCodeCheck.messageRes(): Int? =
    when (this) {
        is PairCodeCheck.Valid -> null
        PairCodeCheck.NotFound -> R.string.couple_code_not_found
        PairCodeCheck.Expired -> R.string.couple_code_expired
        PairCodeCheck.AlreadyUsed -> R.string.couple_code_used
        PairCodeCheck.OwnCode -> R.string.couple_code_own
        PairCodeCheck.AlreadyConnected -> R.string.couple_code_already_connected
    }
