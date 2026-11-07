package jpark.bro.days.core.domain.reminder

import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** product-spec 9장 시나리오: 오늘 = 2026.11.05 */
class DueRemindersTest {
    private val today = LocalDate.of(2026, 11, 5)
    private val nine = LocalTime.of(9, 0)

    private fun item(
        id: String,
        date: LocalDate?,
        repeatRule: RepeatRule = RepeatRule.NONE,
        type: ItemType = ItemType.DDAY,
        isDone: Boolean = false,
    ) = Item(
        id = id,
        content =
            ItemContent(
                type = type,
                title = id,
                colorKey = "PINK",
                iconKey = "STAR",
                date = date,
                countMode = if (type == ItemType.DDAY) CountMode.DDAY else null,
                repeatRule = repeatRule,
                isDone = isDone,
            ),
        ownerUserId = "me",
        updatedBy = "me",
        createdAt = 0,
        updatedAt = 0,
    )

    private fun reminders(vararg offsets: Int) = offsets.map { ItemReminder(it, nine) }

    private fun due(
        items: List<Item>,
        reminders: Map<String, List<ItemReminder>>,
        on: LocalDate = today,
    ) = dueReminders(items, reminders, on).map { it.item.id to it.offsetDays }

    @Test
    fun `당일 하루 전 일주일 전은 항목 날짜에서 그만큼 앞선 날에 보냄`() {
        val trip = item("trip", today.plusDays(7))
        val dinner = item("dinner", today.plusDays(1), type = ItemType.PLAN)
        val exam = item("exam", today)
        val reminders = mapOf("trip" to reminders(0, 7), "dinner" to reminders(1), "exam" to reminders(0, 1))

        assertEquals(
            listOf("trip" to 7, "dinner" to 1, "exam" to 0),
            due(listOf(trip, dinner, exam), reminders),
        )
    }

    @Test
    fun `알림을 고르지 않았거나 날짜가 맞지 않으면 보내지 않음`() {
        val trip = item("trip", today.plusDays(6))
        val noReminder = item("none", today)

        assertTrue(due(listOf(trip, noReminder), mapOf("trip" to reminders(0, 1, 7))).isEmpty())
    }

    @Test
    fun `지난 항목은 보내지 않음`() {
        assertTrue(due(listOf(item("past", today.minusDays(1))), mapOf("past" to reminders(0))).isEmpty())
    }

    @Test
    fun `매년 반복은 올해 도래일 기준 (엄마 생신 1월 2일)`() {
        val mom = item("mom", LocalDate.of(1970, 1, 2), RepeatRule.YEARLY)
        val reminders = mapOf("mom" to reminders(0, 7))

        assertEquals(listOf("mom" to 7), due(listOf(mom), reminders, on = LocalDate.of(2026, 12, 26)))
        assertEquals(listOf("mom" to 0), due(listOf(mom), reminders, on = LocalDate.of(2027, 1, 2)))
    }

    @Test
    fun `반복 항목은 기준일 전에는 보내지 않음`() {
        val future = item("future", LocalDate.of(2027, 1, 2), RepeatRule.YEARLY)

        assertTrue(due(listOf(future), mapOf("future" to reminders(0)), on = LocalDate.of(2026, 1, 2)).isEmpty())
    }

    @Test
    fun `매월 31일 반복은 없는 달에 말일로 보냄`() {
        val rent = item("rent", LocalDate.of(2026, 1, 31), RepeatRule.MONTHLY)
        val reminders = mapOf("rent" to reminders(0, 1))

        assertEquals(listOf("rent" to 1), due(listOf(rent), reminders, on = LocalDate.of(2026, 11, 29)))
        assertEquals(listOf("rent" to 0), due(listOf(rent), reminders, on = LocalDate.of(2026, 11, 30)))
        assertEquals(listOf("rent" to 0), due(listOf(rent), reminders, on = LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun `윤년 2월 29일 매년 반복은 평년에 2월 28일`() {
        val leap = item("leap", LocalDate.of(2024, 2, 29), RepeatRule.YEARLY)
        val reminders = mapOf("leap" to reminders(0, 1))

        assertEquals(listOf("leap" to 1), due(listOf(leap), reminders, on = LocalDate.of(2027, 2, 27)))
        assertEquals(listOf("leap" to 0), due(listOf(leap), reminders, on = LocalDate.of(2027, 2, 28)))
        assertEquals(listOf("leap" to 0), due(listOf(leap), reminders, on = LocalDate.of(2028, 2, 29)))
    }

    @Test
    fun `해를 넘기는 일주일 전 알림`() {
        val newYear = item("newYear", LocalDate.of(2027, 1, 1))

        assertEquals(
            listOf("newYear" to 7),
            due(listOf(newYear), mapOf("newYear" to reminders(7)), on = LocalDate.of(2026, 12, 25)),
        )
    }

    @Test
    fun `완료한 일정과 날짜 없는 일정은 보내지 않음`() {
        val done = item("done", today, type = ItemType.PLAN, isDone = true)
        val undated = item("undated", null, type = ItemType.PLAN)

        assertTrue(due(listOf(done, undated), mapOf("done" to reminders(0), "undated" to reminders(0))).isEmpty())
    }

    @Test
    fun `일정은 반복하지 않음`() {
        val plan = item("plan", LocalDate.of(2026, 10, 5), RepeatRule.MONTHLY, type = ItemType.PLAN)

        assertTrue(due(listOf(plan), mapOf("plan" to reminders(0))).isEmpty())
    }
}
