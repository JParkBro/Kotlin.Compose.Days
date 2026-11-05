package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.model.Visibility
import java.time.LocalDate

/** 커플 홈 공개 범위 필터 (product-spec 4-1): 전체 / 우리 / 나만 */
internal enum class HomeFilter {
    ALL,
    SHARED,
    PRIVATE,
}

internal data class HomeState(
    val isLoading: Boolean = true,
    /** 디데이 · 일정을 하나라도 등록했는지 (없으면 빈 상태) */
    val hasItems: Boolean = false,
    val today: LocalDate = LocalDate.MIN,
    val content: HomeContent = HomeContent(),
    /** 솔로일 때 "커플 코드로 연결하기" 카드 (닫기 가능, product-spec 4-1) */
    val showCoupleBanner: Boolean = false,
    /** 커플일 때만 보이므로 솔로로 바뀌면 의미가 없다 */
    val filter: HomeFilter = HomeFilter.ALL,
    /** + 버튼으로 연 [디데이 / 일정] 선택 시트 (product-spec 4-2) */
    val isAddSheetOpen: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && !hasItems

    val isCoupled: Boolean get() = content.couple != null

    /** 필터를 적용한 리스트. 솔로는 필터가 없다 */
    val visibleRows: List<ItemDday>
        get() =
            if (!isCoupled) {
                content.rows
            } else {
                when (filter) {
                    HomeFilter.ALL -> content.rows
                    HomeFilter.SHARED -> content.rows.filter { it.item.content.visibility == Visibility.SHARED }
                    HomeFilter.PRIVATE -> content.rows.filter { it.item.content.visibility == Visibility.PRIVATE }
                }
            }
}
