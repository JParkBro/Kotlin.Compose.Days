package jpark.bro.days.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * 앱의 백스택. topLevelKeys(하단 탭)마다 백스택을 따로 두어 탭을 바꿔도 각 탭의 화면 위치가 유지된다.
 * - backStack: NavDisplay에 넘기는 목록 = 시작 탭 스택 (+ 현재 탭이 시작 탭이 아니면 현재 탭 스택)
 * - 현재 탭의 스택이 첫 화면만 남은 상태에서 뒤로 가면 시작 탭으로 돌아간다.
 */
class Navigator(
    startKey: NavKey,
    private val topLevelKeys: Set<NavKey> = emptySet(),
) {
    private val stacks = linkedMapOf<NavKey, MutableList<NavKey>>()
    private var startTab: NavKey = startKey

    val backStack: SnapshotStateList<NavKey> = mutableStateListOf()

    /** 현재 탭 (하단 탭 선택 표시용) */
    var currentTab: NavKey by mutableStateOf(startKey)
        private set

    init {
        reset(startKey)
    }

    /** 탭 키면 그 탭으로 전환하고, 아니면 현재 탭 스택에 쌓는다. */
    fun goTo(key: NavKey) {
        if (key in topLevelKeys) {
            stacks.getOrPut(key) { mutableListOf(key) }
            currentTab = key
        } else {
            currentStack().add(key)
        }
        sync()
    }

    fun goBack() {
        val stack = currentStack()
        when {
            stack.size > 1 -> stack.removeAt(stack.lastIndex)
            currentTab != startTab -> currentTab = startTab
            else -> return
        }
        sync()
    }

    /** 로그인 완료 → 메인처럼 이전 화면을 모두 지우고 이동 */
    fun replaceAll(key: NavKey) {
        reset(key)
    }

    /** 현재 화면을 다른 화면으로 교체 */
    fun replaceTop(key: NavKey) {
        val stack = currentStack()
        stack.removeAt(stack.lastIndex)
        stack.add(key)
        sync()
    }

    private fun currentStack(): MutableList<NavKey> = stacks.getValue(currentTab)

    private fun reset(key: NavKey) {
        stacks.clear()
        stacks[key] = mutableListOf(key)
        startTab = key
        currentTab = key
        sync()
    }

    private fun sync() {
        val visible = stacks.getValue(startTab) + if (currentTab != startTab) currentStack() else emptyList()
        backStack.clear()
        backStack.addAll(visible)
    }
}
