package jpark.bro.days.feature.couple.impl.couplemanage

import jpark.bro.days.core.domain.dday.daysSince
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.UserProfile
import java.io.File
import java.time.LocalDate

internal data class CoupleManageState(
    val isLoading: Boolean = true,
    val couple: Couple? = null,
    /** 상대 프로필. 아직 받지 못했으면 null */
    val partner: UserProfile? = null,
    /** 상대 프로필 사진 파일. 없으면 이름 첫 글자 */
    val partnerPhoto: File? = null,
    val today: LocalDate = LocalDate.MIN,
    val isDatePickerOpen: Boolean = false,
    val isUnpairSheetOpen: Boolean = false,
    /** 해제할 때 공유 항목 사본도 삭제 */
    val deleteCopies: Boolean = false,
    val isWorking: Boolean = false,
) {
    val togetherDays: Int? get() = couple?.let { daysSince(it.startDate, today, it.countFirstDayAsOne) }
}
