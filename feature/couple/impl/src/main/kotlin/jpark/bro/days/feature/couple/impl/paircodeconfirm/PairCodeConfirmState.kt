package jpark.bro.days.feature.couple.impl.paircodeconfirm

import androidx.annotation.StringRes
import jpark.bro.days.core.domain.dday.daysSince
import java.time.LocalDate

internal data class PairCodeConfirmState(
    val code: String,
    val isLoading: Boolean = true,
    /** 코드 발급자 이름 */
    val partnerName: String = "",
    /** 코드를 쓸 수 없는 이유. 있으면 연결할 수 없다 */
    @StringRes val codeError: Int? = null,
    val today: LocalDate = LocalDate.MIN,
    /** 기본은 오늘. 오늘 이후는 고를 수 없다 */
    val startDate: LocalDate = LocalDate.MIN,
    /** 한국식 커플 디데이 기본값 */
    val countFirstDayAsOne: Boolean = true,
    val isDatePickerOpen: Boolean = false,
    val isConnecting: Boolean = false,
) {
    val togetherDays: Int get() = daysSince(startDate, today, countFirstDayAsOne)

    val canConnect: Boolean get() = !isLoading && codeError == null && !isConnecting
}
