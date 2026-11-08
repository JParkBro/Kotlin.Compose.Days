package jpark.bro.days.core.domain.reminder

import jpark.bro.days.core.domain.dday.occurrencesIn
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.ItemType
import java.time.LocalDate
import java.time.YearMonth

/** 오늘 보낼 항목 알림 하나. [offsetDays] 0 = 당일, 1 = 하루 전, 7 = 일주일 전 */
data class DueReminder(
    val item: Item,
    val offsetDays: Int,
)

/**
 * [today]에 보낼 항목 알림 (product-spec 4-5). 발송 시각은 오전 9시 고정이라 날짜만 본다.
 * - 항목이 today + offsetDays에 놓이면 보낸다. 매년 · 매월 반복은 그 날의 도래일 기준 ([occurrencesIn]과 같은 규칙)
 * - 날짜 없는 일정 · 완료한 일정은 보내지 않는다
 * 결과는 [items] 순서, 한 항목 안에서는 offsetDays 오름차순
 */
fun dueReminders(
    items: List<Item>,
    remindersByItemId: Map<String, List<ItemReminder>>,
    today: LocalDate,
): List<DueReminder> =
    items
        .filterNot { it.content.type == ItemType.PLAN && it.content.isDone }
        .flatMap { item ->
            remindersByItemId[item.id]
                .orEmpty()
                .map { it.offsetDays }
                .distinct()
                .sorted()
                .filter { offset -> item.occursOn(today.plusDays(offset.toLong())) }
                .map { DueReminder(item, it) }
        }

private fun Item.occursOn(date: LocalDate): Boolean = date in content.occurrencesIn(YearMonth.from(date))
