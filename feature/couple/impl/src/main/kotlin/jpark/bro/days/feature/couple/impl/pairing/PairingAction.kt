package jpark.bro.days.feature.couple.impl.pairing

internal sealed interface PairingAction {
    data class TabSelected(
        val tab: PairingTab,
    ) : PairingAction

    /** 실패 · 만료 후 다시 발급 */
    data object IssueClicked : PairingAction

    data object CopyClicked : PairingAction

    data object SetProfileClicked : PairingAction

    data class CellClicked(
        val index: Int,
    ) : PairingAction

    /** 키보드 입력 (붙여넣기면 여러 글자) */
    data class CharsTyped(
        val text: String,
    ) : PairingAction

    /** 백스페이스: 선택된 칸을 지우고, 빈 칸이면 앞 칸을 지운다 */
    data object DeletePressed : PairingAction

    data object ConnectClicked : PairingAction

    data object BackClicked : PairingAction
}
