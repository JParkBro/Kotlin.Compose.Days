package jpark.bro.days.core.ui

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 앱 UI는 한국어만 지원하므로 요일 · 오전/오후는 한국어로 고정한다 */
private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)

/** 2026.11.06 (금) */
fun LocalDate.toDisplayText(): String = format(DateFormat)

/** 오후 07:00 */
fun LocalTime.toDisplayText(): String = format(TimeFormat)
