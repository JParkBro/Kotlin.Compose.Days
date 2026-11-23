package jpark.bro.days.core.designsystem.component

import androidx.annotation.DrawableRes
import jpark.bro.days.core.designsystem.R

/** 마스코트 픽셀아트 (80×80px, `res/drawable-nodpi`). 원본은 저장소 밖 Mascot/sprites (theme-guide.md 11장) */
enum class Mascot(
    @param:DrawableRes val res: Int,
) {
    WAVE(R.drawable.mascot_wave),
    JUMP(R.drawable.mascot_jump),
    FACE_NEUTRAL(R.drawable.mascot_face_neutral),
    FACE_SMILE(R.drawable.mascot_face_smile),
    FACE_CONTENT(R.drawable.mascot_face_content),
    FACE_WORRIED(R.drawable.mascot_face_worried),
    FACE_SHY(R.drawable.mascot_face_shy),
    ;

    /** 표정은 아래가 잘린 상반신이라 원 아래에 붙여 그린다 */
    val isBust: Boolean get() = name.startsWith("FACE_")
}
