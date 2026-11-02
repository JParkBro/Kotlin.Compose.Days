package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.Category

/** 홈 빈 상태의 빠른 시작 칩 (product-spec 4-1): 디데이 추가를 카테고리 · 제목을 채워서 연다 */
internal enum class QuickStart(
    val category: Category,
) {
    BIRTHDAY(Category.BIRTHDAY),
    TRAVEL(Category.TRAVEL),
    EXAM(Category.GOAL),
    DISCHARGE(Category.GOAL),
}
