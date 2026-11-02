package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class PlanTimeOrderTest {
    private fun plan(
        title: String,
        slot: TimeSlot?,
        time: LocalTime? = null,
    ) = ItemContent(
        type = ItemType.PLAN,
        title = title,
        colorKey = "PINK",
        iconKey = "EVENT",
        timeSlot = slot,
        time = time,
    )

    @Test
    fun `종일 아침 점심 시간 지정 저녁 순으로 정렬됨`() {
        val plans =
            listOf(
                plan("저녁", TimeSlot.EVENING),
                plan("오후 3시", TimeSlot.EXACT, LocalTime.of(15, 0)),
                plan("점심", TimeSlot.LUNCH),
                plan("종일", TimeSlot.ALL_DAY),
                plan("아침", TimeSlot.MORNING),
            )

        assertEquals(listOf("종일", "아침", "점심", "오후 3시", "저녁"), plans.sortedBy { it.sortTime }.map { it.title })
    }

    @Test
    fun `시간을 정하지 않은 일정은 맨 앞`() {
        assertEquals(LocalTime.MIN, plan("미정", null).sortTime)
    }
}
