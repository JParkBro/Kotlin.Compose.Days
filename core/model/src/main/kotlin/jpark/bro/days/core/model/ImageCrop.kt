package jpark.bro.days.core.model

/**
 * 사진에서 쓸 영역 (프로필 사진 자르기). 값은 회전을 반영한 사진의 가로 · 세로에 대한 비율(0~1)이라
 * 화면에 보인 크기와 저장할 때 읽는 크기가 달라도 같은 영역을 가리킨다.
 */
data class ImageCrop(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)
