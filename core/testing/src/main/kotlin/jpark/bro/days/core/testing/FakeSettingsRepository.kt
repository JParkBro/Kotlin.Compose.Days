package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initial: ThemeSetting = ThemeSetting(),
) : SettingsRepository {
    private val theme = MutableStateFlow(initial)

    /** true면 저장이 실패한다 */
    var failNextSave: Boolean = false

    override fun observeThemeSetting(): Flow<ThemeSetting> = theme

    override suspend fun setThemeSetting(setting: ThemeSetting): Result<Unit> {
        if (failNextSave) {
            failNextSave = false
            return Result.failure(IllegalStateException("저장 실패 (테스트)"))
        }
        theme.value = setting
        return Result.success(Unit)
    }
}
