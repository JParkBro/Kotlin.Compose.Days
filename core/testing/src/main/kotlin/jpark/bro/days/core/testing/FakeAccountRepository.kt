package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.AccountRepository
import jpark.bro.days.core.model.Reauthentication

/** 회원 탈퇴 Fake. [result]를 바꿔 실패 · 계정 불일치를 흉내 낸다 */
class FakeAccountRepository : AccountRepository {
    val withdrawn = mutableListOf<Reauthentication>()
    var result: Result<Unit> = Result.success(Unit)

    override suspend fun withdraw(reauthentication: Reauthentication): Result<Unit> {
        withdrawn += reauthentication
        return result
    }
}
