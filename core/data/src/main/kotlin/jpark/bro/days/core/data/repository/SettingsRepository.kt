package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** 저장된 테마 */
    fun observeThemeSetting(): Flow<ThemeSetting>

    /** 화면에 적용할 테마 = 미리보기 중이면 미리보기 값, 아니면 저장된 값 */
    fun observeEffectiveThemeSetting(): Flow<ThemeSetting>

    suspend fun setThemeSetting(setting: ThemeSetting): Result<Unit>

    /** 테마 선택 시트에서 고르는 동안 저장하지 않고 앱에 바로 보여준다. null이면 미리보기를 끝낸다. */
    fun setThemePreview(setting: ThemeSetting?)

    /** 홈의 커플 연결 카드를 닫았는지 (기기에만 저장) */
    fun observeCoupleBannerDismissed(): Flow<Boolean>

    suspend fun dismissCoupleBanner(): Result<Unit>
}
