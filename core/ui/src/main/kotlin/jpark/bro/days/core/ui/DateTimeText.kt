package jpark.bro.days.core.ui

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 앱 UI는 한국어만 지원하므로 요일 · 오전/오후는 한국어로 고정한다 */
private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
private val ShortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
private val MonthDayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
private val YearMonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)

/** 2026.11.06 (금) */
fun LocalDate.toDisplayText(): String = format(DateFormat)

/** 2026.11.06 (동의 일자처럼 요일이 필요 없을 때) */
fun LocalDate.toShortDisplayText(): String = format(ShortDateFormat)

/** 11월 6일 (금) */
fun LocalDate.toMonthDayText(): String = format(MonthDayFormat)

/** 2026년 11월 */
fun YearMonth.toDisplayText(): String = format(YearMonthFormat)

/** 오후 07:00 */
fun LocalTime.toDisplayText(): String = format(TimeFormat)
