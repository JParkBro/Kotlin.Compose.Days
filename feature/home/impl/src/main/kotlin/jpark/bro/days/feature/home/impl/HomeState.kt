package jpark.bro.days.feature.home.impl

import java.time.LocalDate

internal data class HomeState(
    val isLoading: Boolean = true,
    /** 디데이 · 일정을 하나라도 등록했는지 (없으면 빈 상태) */
    val hasItems: Boolean = false,
    val today: LocalDate = LocalDate.MIN,
    val content: HomeContent = HomeContent(),
    /** 솔로일 때 "커플 코드로 연결하기" 카드 (닫기 가능, product-spec 4-1) */
    val showCoupleBanner: Boolean = false,
    /** + 버튼으로 연 [디데이 / 일정] 선택 시트 (product-spec 4-2) */
    val isAddSheetOpen: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && !hasItems
}
