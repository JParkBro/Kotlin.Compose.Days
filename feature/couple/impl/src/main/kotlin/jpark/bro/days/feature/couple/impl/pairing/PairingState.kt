package jpark.bro.days.feature.couple.impl.pairing

import androidx.annotation.StringRes
import jpark.bro.days.core.model.PairCode

internal enum class PairingTab { MY_CODE, INPUT }

internal enum class IssueError {
    /** 코드에 보일 내 이름이 없음 → 프로필 설정으로 */
    PROFILE_REQUIRED,
    FAILED,
}

internal data class PairingState(
    val tab: PairingTab = PairingTab.MY_CODE,
    val myCode: PairCode? = null,
    val isIssuing: Boolean = true,
    val issueError: IssueError? = null,
    /** 코드 입력 6칸 (빈 칸은 null). 허용 문자만, 대문자 */
    val cells: List<Char?> = List(PAIR_CODE_LENGTH) { null },
    /** 선택된 칸. 누른 칸을 덮어쓰고 다음 칸으로 넘어간다 */
    val activeCell: Int = 0,
    val isChecking: Boolean = false,
    /** 입력한 코드를 쓸 수 없는 이유 */
    @StringRes val inputError: Int? = null,
) {
    /** 모든 칸이 찼을 때만 코드 */
    val input: String? get() = if (cells.all { it != null }) cells.joinToString("") else null

    val canConnect: Boolean get() = input != null && !isChecking
}

internal const val PAIR_CODE_LENGTH = 6

/** 헷갈리는 0 · O · 1 · I는 코드에 쓰지 않는다 (product-spec 7장) */
internal const val PAIR_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
