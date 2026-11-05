package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.AnniversaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** 메모리에 숨김을 두는 Fake. [failure]를 넣으면 숨기기 · 풀기가 실패한다 */
class FakeAnniversaryRepository(
    initial: Set<String> = emptySet(),
) : AnniversaryRepository {
    val hidden = MutableStateFlow(initial)
    var failure: Throwable? = null

    override fun observeHiddenRuleKeys(): Flow<Set<String>> = hidden

    override suspend fun setHidden(
        ruleKey: String,
        hidden: Boolean,
    ): Result<Unit> {
        failure?.let { return Result.failure(it) }
        this.hidden.update { if (hidden) it + ruleKey else it - ruleKey }
        return Result.success(Unit)
    }
}
