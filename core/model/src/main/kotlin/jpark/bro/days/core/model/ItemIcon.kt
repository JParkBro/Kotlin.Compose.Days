package jpark.bro.days.core.model

/** 항목 아이콘 8종 (product-spec 4-5). `item.icon_key`에는 [name]을 저장한다 */
enum class ItemIcon {
    HEART,
    CAKE,
    FLIGHT,
    SCHOOL,
    STAR,
    EVENT,
    GIFT,
    FLAG,
    ;

    companion object {
        /** 저장된 키를 읽는다. 모르는 값이면 [STAR] */
        fun fromKey(key: String): ItemIcon = entries.firstOrNull { it.name == key } ?: STAR
    }
}

/** 카테고리를 고르면 미리 골라주는 아이콘 (사용자가 아이콘을 직접 바꾸기 전까지) */
val Category.defaultIcon: ItemIcon
    get() =
        when (this) {
            Category.ANNIVERSARY -> ItemIcon.HEART
            Category.BIRTHDAY -> ItemIcon.CAKE
            Category.TRAVEL -> ItemIcon.FLIGHT
            Category.GOAL -> ItemIcon.SCHOOL
            Category.DAILY -> ItemIcon.EVENT
            Category.ETC -> ItemIcon.STAR
        }

/**
 * 항목 색 4종 = 테마 4종의 대표색 (product-spec 4-5). `item.color_key`에는 [AppThemeType.name]을 저장한다.
 * 모르는 값이면 기본 테마([AppThemeType.PINK])
 */
fun itemColorFromKey(key: String): AppThemeType =
    AppThemeType.entries.firstOrNull { it.name == key } ?: AppThemeType.PINK
