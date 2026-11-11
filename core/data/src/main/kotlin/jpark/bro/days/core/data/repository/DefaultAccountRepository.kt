package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.account.AccountAuth
import jpark.bro.days.core.data.account.AccountLocalData
import jpark.bro.days.core.data.account.AccountServerData
import jpark.bro.days.core.data.auth.ProviderSessions
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.model.Reauthentication
import kotlinx.coroutines.flow.first

internal class DefaultAccountRepository(
    private val userSession: UserSession,
    private val accountAuth: AccountAuth,
    private val coupleRepository: CoupleRepository,
    private val serverData: AccountServerData,
    private val localData: AccountLocalData,
    private val providerSessions: ProviderSessions,
) : AccountRepository {
    override suspend fun withdraw(reauthentication: Reauthentication): Result<Unit> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            accountAuth.reauthenticate(me, reauthentication)
            // 상대 기기는 해제를 받아 공유 항목을 나만 보기 사본으로 남긴다 (사본 삭제 안 함)
            if (coupleRepository.observeCouple().first() !=
                null
            ) {
                coupleRepository.disconnect(deleteCopies = false).getOrThrow()
            }
            serverData.delete(me)
            accountAuth.deleteAccount()
            localData.delete(me)
            providerSessions.clear(unlinkKakao = true)
        }
}
