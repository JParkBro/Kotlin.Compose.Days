package jpark.bro.days.core.data.account

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.image.ImageStore
import jpark.bro.days.core.database.dao.AccountDataDao
import jpark.bro.days.core.database.dao.CoupleDao
import jpark.bro.days.core.database.dao.ImageDao
import jpark.bro.days.core.database.dao.ImageSyncDao
import jpark.bro.days.core.datastore.SettingsDataStore
import jpark.bro.days.core.firebase.sync.CoupleRemoteDataSource
import jpark.bro.days.core.firebase.sync.ImageRemoteDataSource
import jpark.bro.days.core.firebase.sync.ProfileRemoteDataSource

/** 탈퇴 때 서버에서 지울 내 데이터 (sync-and-pairing.md 7장). 공유 항목 문서는 상대의 것이기도 해서 지우지 않는다 */
internal class AccountServerData(
    private val imageSyncDao: ImageSyncDao,
    private val settingsDataStore: SettingsDataStore,
    private val imageRemote: ImageRemoteDataSource,
    private val coupleRemote: CoupleRemoteDataSource,
    private val profileRemote: ProfileRemoteDataSource,
) {
    /** 내가 올린 사진 파일 · 발급한 커플 코드 · `users/{uid}`. 이미 없는 것은 성공으로 본다 */
    suspend fun delete(userId: String) {
        imageSyncDao.getUploadedImages(userId).forEach { image ->
            val coupleId = image.remoteCoupleId ?: return@forEach
            imageRemote.delete(coupleId, image.id)
        }
        settingsDataStore.issuedPairCode()?.takeIf { it.userId == userId }?.let { issued ->
            // 만료됐거나 이미 지운 코드여도 탈퇴는 계속한다
            suspendRunCatching { coupleRemote.deletePairCode(issued.code) }
        }
        profileRemote.deleteProfile(userId)
    }
}

/** 탈퇴 때 이 기기에서 지울 이 계정의 데이터. 한 기기의 다른 계정 데이터와 기기 설정(테마 · 알림)은 둔다 */
internal class AccountLocalData(
    private val accountDataDao: AccountDataDao,
    private val coupleDao: CoupleDao,
    private val imageDao: ImageDao,
    private val imageStore: ImageStore,
    private val settingsDataStore: SettingsDataStore,
) {
    suspend fun delete(userId: String) {
        val coupleIds = coupleDao.getCoupleIds(userId)
        imageDao.getAccountImagePaths(userId, coupleIds).forEach { imageStore.delete(it) }
        accountDataDao.deleteAccountData(userId, coupleIds)
        settingsDataStore.removeAccount(userId)
    }
}
