package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class FakeSettingsRepository(
    initial: ThemeSetting = ThemeSetting(),
) : SettingsRepository {
    private val theme = MutableStateFlow(initial)
    private val preview = MutableStateFlow<ThemeSetting?>(null)
    private val coupleBannerDismissed = MutableStateFlow(false)

    /** 현재 미리보기 값 (null이면 미리보기 아님) */
    val themePreview: ThemeSetting? get() = preview.value

    /** true면 다음 저장이 실패한다 */
    var failNextSave: Boolean = false

    override fun observeThemeSetting(): Flow<ThemeSetting> = theme

    override fun observeEffectiveThemeSetting(): Flow<ThemeSetting> = combine(theme, preview) { saved, p -> p ?: saved }

    override suspend fun setThemeSetting(setting: ThemeSetting): Result<Unit> {
        if (failNextSave) {
            failNextSave = false
            return Result.failure(IllegalStateException("저장 실패 (테스트)"))
        }
        theme.value = setting
        return Result.success(Unit)
    }

    override fun setThemePreview(setting: ThemeSetting?) {
        preview.value = setting
    }

    override fun observeCoupleBannerDismissed(): Flow<Boolean> = coupleBannerDismissed

    override suspend fun dismissCoupleBanner(): Result<Unit> {
        coupleBannerDismissed.value = true
        return Result.success(Unit)
    }
}
