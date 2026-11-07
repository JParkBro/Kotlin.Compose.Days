package jpark.bro.days.core.navigation

/** 앱 밖(위젯 · 알림)에서 MainActivity를 열 때 쓰는 인텐트 값 */
object AppIntents {
    /** 이 값이 있으면 [홈, 해당 항목 상세] 백스택으로 연다 */
    const val EXTRA_OPEN_ITEM_ID = "jpark.bro.days.extra.OPEN_ITEM_ID"

    /** [EXTRA_OPEN_ITEM_ID]와 함께. 값이 일정(ItemType.PLAN 이름)이면 상세 대신 [홈, 일정 수정]으로 연다 (product-spec 4-5) */
    const val EXTRA_OPEN_ITEM_TYPE = "jpark.bro.days.extra.OPEN_ITEM_TYPE"
}
