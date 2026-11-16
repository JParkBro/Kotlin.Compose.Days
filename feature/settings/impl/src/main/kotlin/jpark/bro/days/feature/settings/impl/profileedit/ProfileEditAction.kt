package jpark.bro.days.feature.settings.impl.profileedit

import jpark.bro.days.core.ads.RewardedAdResult
import jpark.bro.days.core.model.ImageCrop
import java.time.LocalDate

internal sealed interface ProfileEditAction {
    data class NicknameChanged(
        val value: String,
    ) : ProfileEditAction

    data object BirthDateClicked : ProfileEditAction

    data class BirthDateSelected(
        val date: LocalDate,
    ) : ProfileEditAction

    data object BirthDateDismissed : ProfileEditAction

    /** 생일 지우기 (생일 기념일을 만들지 않음) */
    data object BirthDateCleared : ProfileEditAction

    /** 프로필 사진 · 커플 사진 한도 시트 */
    sealed interface PhotoInput : ProfileEditAction

    /** 사진 선택기를 연다. 커플이고 사진 한도를 다 썼으면 한도 시트를 먼저 띄운다 */
    data object PhotoClicked : PhotoInput

    /** 사진을 고름 → 자르기 화면을 연다 */
    data class PhotoPicked(
        val uri: String,
    ) : PhotoInput

    /** 자르기 화면에서 확인: 고른 영역만 가져온다 */
    data class PhotoCropped(
        val crop: ImageCrop,
    ) : PhotoInput

    /** 자르기 화면을 닫음: 사진을 바꾸지 않는다 */
    data object PhotoCropDismissed : PhotoInput

    data object PhotoRemoved : PhotoInput

    /** 사진 한도 시트: 광고 보고 1장 더 */
    data object WatchAdClicked : PhotoInput

    data class RewardedAdFinished(
        val result: RewardedAdResult,
    ) : PhotoInput

    data object ImageLimitDismissed : PhotoInput

    data object SaveClicked : ProfileEditAction

    data object BackClicked : ProfileEditAction
}
