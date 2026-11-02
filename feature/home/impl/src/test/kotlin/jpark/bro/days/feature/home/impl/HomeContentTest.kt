package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.DdayCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HomeContentTest {
    private val all = listOf(Discharge, MomBirthday, QuitSmoking, Toeic, SeongsuDinner, JejuTrip, Someday)

    @Test
    fun `고정한 디데이가 히어로가 되고 리스트에서는 빠짐`() {
        val content = buildHomeContent(all, pinnedItemId = Discharge.id, today = Today)

        assertEquals(Discharge, content.hero?.row?.item)
        assertTrue(content.hero!!.isPinned)
        assertEquals(DdayCount.Remaining(134), content.hero!!.row.dday)
        assertFalse(content.rows.any { it.item == Discharge })
    }

    @Test
    fun `리스트는 다가오는 것 가까운 순 그다음 지난 것`() {
        val content = buildHomeContent(all, pinnedItemId = Discharge.id, today = Today)

        assertEquals(
            listOf("토익 시험", "제주 여행", "엄마 생신", "금연"),
            content.rows.map { it.item.content.title },
        )
    }

    @Test
    fun `디데이로 세기를 켜지 않은 일정과 날짜 없는 일정은 리스트에 없음`() {
        val content = buildHomeContent(all, pinnedItemId = null, today = Today)

        val titles =
            content.rows.map { it.item.content.title } +
                content.hero!!
                    .row.item.content.title
        assertFalse("성수 저녁" in titles)
        assertFalse("캠핑 가기" in titles)
    }

    @Test
    fun `고정이 없으면 가장 가까운 다가오는 항목이 히어로`() {
        val content = buildHomeContent(all, pinnedItemId = null, today = Today)

        assertEquals(Toeic, content.hero?.row?.item)
        assertFalse(content.hero!!.isPinned)
    }

    @Test
    fun `고정이 없고 다가오는 항목도 없으면 히어로 없음`() {
        val content = buildHomeContent(listOf(QuitSmoking), pinnedItemId = null, today = Today)

        assertNull(content.hero)
        assertEquals(listOf(QuitSmoking), content.rows.map { it.item })
    }

    @Test
    fun `삭제됐거나 일정인 고정 id는 무시함`() {
        val content = buildHomeContent(listOf(Toeic, JejuTrip), pinnedItemId = JejuTrip.id, today = Today)

        assertEquals(Toeic, content.hero?.row?.item)
        assertFalse(content.hero!!.isPinned)
    }

    @Test
    fun `다가오는 일정은 오늘 이후 미완료 일정 중 가장 빠른 것`() {
        val past = sampleItem("past", SeongsuDinner.content.copy(title = "지난 약속", date = Today.minusDays(1)))
        val done = sampleItem("done", SeongsuDinner.content.copy(title = "끝난 약속", date = Today, isDone = true))
        val todayLunch =
            sampleItem("lunch", SeongsuDinner.content.copy(title = "점심", date = Today, time = LocalTime.of(12, 0)))

        val content = buildHomeContent(listOf(past, done, SeongsuDinner, todayLunch, Someday), null, Today)

        assertEquals(todayLunch, content.nextPlan)
    }

    @Test
    fun `일정이 없으면 다가오는 일정 없음`() {
        assertNull(buildHomeContent(listOf(Discharge), null, Today).nextPlan)
    }
}
