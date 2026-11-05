package jpark.bro.days.feature.settings.impl.settings

import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.ThemeSetting
import java.io.File
import java.time.LocalDate

internal data class SettingsState(
    /** null이면 프로필 미설정 */
    val profile: ProfileSummary? = null,
    /** 커플이 연결됨 */
    val isCoupled: Boolean = false,
    /** 상대 이름. 연결됐어도 상대 프로필을 아직 받지 못했으면 null */
    val partnerName: String? = null,
    val accountEmail: String? = null,
    /** 내 프로필 사진 파일. 없으면 이름 첫 글자 */
    val profilePhoto: File? = null,
    val themeSetting: ThemeSetting = ThemeSetting(),
    /** null이면 테마 시트 닫힘 */
    val themeSheet: ThemeSheetState? = null,
)

internal data class ProfileSummary(
    val nickname: String,
    val birthDate: LocalDate?,
    /** 함께한 날 (커플일 때) */
    val togetherDays: Int?,
)

/** 테마 시트에서 고르는 중인 값. 적용하기 전에는 저장하지 않는다. */
internal data class ThemeSheetState(
    val type: AppThemeType,
    val mode: AppThemeMode,
    val isSaving: Boolean = false,
)
