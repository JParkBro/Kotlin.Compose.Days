package jpark.bro.days.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import jpark.bro.days.core.data.repository.ImageRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.observeImageFileOrNull
import jpark.bro.days.core.model.Item
import kotlinx.coroutines.flow.first
import java.io.File

/** 위젯 프로필 원 하나: 사진 → 이름 첫 글자 */
internal data class WidgetProfile(
    val name: String?,
    val photo: File?,
)

/** 위젯 프로필 원 하나 (위젯에 그릴 비트맵, 없으면 없으면 이름 첫 글자) */
internal data class WidgetAvatar(
    val initial: String?,
    val bitmap: Bitmap?,
)

/**
 * 위젯에 쓰는 사진 (product-spec 8장): 배경은 보여주는 디데이의 배경 사진, 프로필은 커플이면 두 사람 · 솔로면 나.
 * 기기에 저장된 파일만 쓴다. 아직 받지 않은 상대 사진은 비어 있고, 앱에서 받은 뒤 다음 갱신 때 보인다.
 */
internal class WidgetPhotos(
    private val imageRepository: ImageRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend fun background(item: Item?): File? =
        imageRepository.observeImageFileOrNull(item?.content?.backgroundImageId).first()

    /** 상대(커플일 때) → 나 순서. 내 프로필이 없으면 빈 목록 */
    suspend fun profiles(): List<WidgetProfile> {
        val me = profileRepository.observeMyProfile().first() ?: return emptyList()
        val partner = profileRepository.observePartnerProfile().first()
        return listOfNotNull(partner, me).map { profile ->
            WidgetProfile(
                name = profile.nickname,
                photo = imageRepository.observeImageFileOrNull(profile.profileImageId).first(),
            )
        }
    }
}

/**
 * 위젯에 넣을 비트맵. 위젯(RemoteViews)은 비트맵 크기 한도가 있어 긴 변 [maxEdge]px 정도로 줄여 읽는다.
 * 읽지 못하면 null
 */
internal fun File.decodeForWidget(maxEdge: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    val longEdge = maxOf(bounds.outWidth, bounds.outHeight)
    if (longEdge <= 0) return null
    var sample = 1
    while (longEdge / (sample * 2) >= maxEdge) sample *= 2
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}
