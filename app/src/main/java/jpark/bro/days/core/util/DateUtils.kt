package jpark.bro.days.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

object DateUtil {
    fun getCurrentLocalDate(): LocalDate =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

    fun formatDateToLocalDate(dateMillis: Long): LocalDate =
        Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date

    fun formatDateToString(date: LocalDate): String =
        "${date.year}.${date.monthNumber.toString().padStart(2, '0')}.${date.dayOfMonth.toString().padStart(2, '0')}"

    fun calculateDaysSinceStart(startDate: LocalDate): Int {
        val today = getCurrentLocalDate()
        val daysDifference = startDate.daysUntil(today).plus(1)
        return if (daysDifference < 1) 0 else daysDifference
    }

    fun calculateDaysSinceStart(
        startDate: LocalDate,
        targetDate: LocalDate,
    ): Int {
        val daysDifference = startDate.daysUntil(targetDate).plus(1)
        return if (daysDifference < 1) 0 else daysDifference
    }

    fun LocalDate.toEpochMilli(): Long = this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    fun calculateKoreanAge(
        birthday: LocalDate,
        currentDate: LocalDate,
    ): Int {
        val currentYear = currentDate.year
        val birthYear = birthday.year
        return currentYear - birthYear + 1
    }
}
