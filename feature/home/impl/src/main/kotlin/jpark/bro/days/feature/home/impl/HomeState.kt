package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.Item

internal data class HomeState(
    val isLoading: Boolean = true,
    val items: List<Item> = emptyList(),
    /** 솔로일 때 "커플 코드로 연결하기" 카드 (닫기 가능, product-spec 4-1) */
    val showCoupleBanner: Boolean = false,
) {
    /** 등록한 디데이 · 일정이 하나도 없음 */
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}
