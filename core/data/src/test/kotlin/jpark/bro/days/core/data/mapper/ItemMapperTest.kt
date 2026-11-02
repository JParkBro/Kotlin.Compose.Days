package jpark.bro.days.core.data.mapper

import jpark.bro.days.core.data.DinnerPlan
import jpark.bro.days.core.data.DischargeDday
import jpark.bro.days.core.data.ME
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ItemMapperTest {
    private fun item(content: ItemContent) =
        Item(id = "a", content = content, ownerUserId = ME, updatedBy = ME, createdAt = 1L, updatedAt = 2L)

    @Test
    fun `디데이를 Entity로 바꿨다가 되돌려도 값이 같음`() {
        val original = item(DischargeDday.copy(countMode = CountMode.DPLUS, countFirstDayAsOne = true))

        assertEquals(original, original.toEntity(SyncState.SYNCED).toModel())
    }

    @Test
    fun `일정을 Entity로 바꿨다가 되돌려도 값이 같음`() {
        val original = item(DinnerPlan)

        assertEquals(original, original.toEntity(SyncState.SYNCED).toModel())
    }

    @Test
    fun `열거형은 대문자 이름 날짜는 yyyy-MM-dd 시각은 HH mm으로 저장`() {
        val entity = item(DinnerPlan.copy(repeatRule = RepeatRule.MONTHLY)).toEntity(SyncState.PENDING)

        assertEquals("PLAN", entity.type)
        assertEquals("PRIVATE", entity.visibility)
        assertEquals("DAILY", entity.category)
        assertEquals("MONTHLY", entity.repeatRule)
        assertEquals("EXACT", entity.timeSlot)
        assertEquals("2026-11-06", entity.date)
        assertEquals("19:00", entity.time)
        assertEquals("PENDING", entity.syncState)
    }

    @Test
    fun `윤년 2월 29일 날짜가 그대로 유지됨`() {
        val original = item(DischargeDday.copy(date = LocalDate.of(2028, 2, 29)))

        assertEquals(
            LocalDate.of(2028, 2, 29),
            original
                .toEntity(SyncState.SYNCED)
                .toModel()
                .content.date,
        )
    }

    @Test
    fun `알림 시각을 HH mm으로 저장하고 되돌림`() {
        val reminder = ItemReminder(offsetDays = 7, timeOfDay = LocalTime.of(9, 0))

        val entity = reminder.toEntity(id = "r", userId = ME, itemId = "a")

        assertEquals("09:00", entity.timeOfDay)
        assertEquals(reminder, entity.toModel())
    }
}
