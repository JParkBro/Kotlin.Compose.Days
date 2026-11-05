package jpark.bro.days.core.data.repository

import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * 배경 · 프로필 사진 (infra-and-billing-protection.md 2-2). 기기 파일이 원본이고, 공유될 때만 동기화가 커플 저장소에 올린다.
 * 나만 보기 항목 · 솔로 프로필 사진은 서버에 올리지 않는다.
 */
interface ImageRepository {
    /** 사진 선택기가 준 content Uri를 줄여 기기에 저장하고 사진 id를 돌려준다 */
    suspend fun importImage(uri: String): Result<String>

    /** 기기 파일. 상대가 올린 사진이면 처음 한 번 받아 저장한다. 없거나 받지 못하면 null */
    fun observeImageFile(imageId: String): Flow<File?>

    /** 더 쓰지 않는 사진 (바꾸거나 지움). 기기 파일을 지우고, 내가 올린 서버 파일은 동기화가 지운다 */
    suspend fun discard(imageId: String): Result<Unit>
}
