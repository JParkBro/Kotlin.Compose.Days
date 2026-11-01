package jpark.bro.days.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

class Navigator(
    startKey: NavKey,
) {
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(startKey)

    fun goTo(key: NavKey) {
        backStack.add(key)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    /** 로그인 완료 → 메인처럼 이전 화면을 모두 지우고 이동 */
    fun replaceAll(key: NavKey) {
        backStack.clear()
        backStack.add(key)
    }

    /** 현재 화면을 다른 화면으로 교체 */
    fun replaceTop(key: NavKey) {
        backStack.removeLastOrNull()
        backStack.add(key)
    }
}
