package jpark.bro.days.feature.item.impl.itemedit

internal sealed interface ItemEditEvent {
    /** 저장 · 삭제 · 취소 후, 또는 수정할 항목이 없을 때 */
    data object NavigateBack : ItemEditEvent

    data object SaveFailed : ItemEditEvent

    data object DeleteFailed : ItemEditEvent

    data object OpenPhotoPicker : ItemEditEvent

    /** 고른 사진을 읽거나 줄이지 못함 */
    data object PhotoFailed : ItemEditEvent
}
