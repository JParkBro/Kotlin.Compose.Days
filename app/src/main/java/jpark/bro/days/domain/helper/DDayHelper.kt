package jpark.bro.days.domain.helper

import jpark.bro.days.R
import jpark.bro.days.core.util.DateUtil
import jpark.bro.days.core.util.DateUtil.calculateDaysSinceStart
import jpark.bro.days.core.util.ResourceProvider
import jpark.bro.days.domain.model.DDayData
import jpark.bro.days.domain.model.ProfileData
import jpark.bro.days.ui.util.EMOJIS
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import javax.inject.Inject
import kotlin.math.absoluteValue

class DDayHelper
    @Inject
    constructor(
        private val resourceProvider: ResourceProvider,
    ) {
        fun generateDDayDates(
            startDate: LocalDate?,
            profileA: ProfileData?,
            profileB: ProfileData?,
        ): List<Triple<String, String, String>>? {
            if (startDate == null) return null

            val today = DateUtil.getCurrentLocalDate()
            val importantDates = mutableListOf<DDayData>()

            // 첫날
            importantDates +=
                DDayData(
                    resourceProvider.asString(R.string.first_meeting_day, EMOJIS.HEART),
                    startDate,
                    startDate.daysUntil(today),
                )

            // 오늘
            if (startDate !=
                today
            ) {
                importantDates +=
                    DDayData(
                        resourceProvider.asString(R.string.day_count, calculateDaysSinceStart(startDate, today)),
                        today,
                        0,
                    )
            }

            importantDates +=
                listOf(
                    DDayData(
                        resourceProvider.asString(R.string.day_count, 20),
                        startDate.plus(DatePeriod(days = 19)),
                        startDate.plus(DatePeriod(days = 19)).daysUntil(today),
                    ),
                    DDayData(
                        resourceProvider.asString(R.string.day_count, 50),
                        startDate.plus(DatePeriod(days = 49)),
                        startDate.plus(DatePeriod(days = 49)).daysUntil(today),
                    ),
                ).filter { event -> importantDates.none { it.date == event.date } }

            // N00일
            importantDates +=
                generateSequence(1) { it + 1 }
                    .flatMap { increment ->
                        listOf(
                            DDayData(
                                resourceProvider.asString(R.string.day_count, increment * 100),
                                startDate.plus(DatePeriod(days = (increment * 100) - 1)),
                                startDate.plus(DatePeriod(days = (increment * 100) - 1)).daysUntil(today),
                            ),
                        )
                    }.takeWhile { it.date <= startDate.plus(DatePeriod(days = 30 * 365)) }
                    .filter { event -> importantDates.none { it.date == event.date } }
                    .toList()

            // N주년
            importantDates +=
                generateSequence(1) { it + 1 }
                    .flatMap { increment ->
                        listOf(
                            DDayData(
                                resourceProvider.asString(R.string.year_count, increment),
                                startDate.plus(DatePeriod(years = increment)),
                                startDate.plus(DatePeriod(years = increment)).daysUntil(today),
                            ),
                        )
                    }.takeWhile { it.date <= startDate.plus(DatePeriod(years = 30)) }
                    .filter { event -> importantDates.none { it.date == event.date } }
                    .toList()

            profileA?.let {
                val thisYearBirthDay =
                    LocalDate(
                        year = today.year,
                        monthNumber = profileA.birthday.monthNumber,
                        dayOfMonth = profileA.birthday.dayOfMonth,
                    )
                val nextClosestBirthday =
                    if (thisYearBirthDay < today) {
                        thisYearBirthDay.plus(DatePeriod(years = 1))
                    } else {
                        thisYearBirthDay
                    }

                generateSequence(nextClosestBirthday) { it.plus(DatePeriod(years = 1)) }
                    .takeWhile { it <= startDate.plus(DatePeriod(years = 30)) }
                    .forEach { nextBirthday ->
                        val existingEventIndex = importantDates.indexOfFirst { it.date == nextBirthday }
                        if (existingEventIndex != -1) {
                            // 중복된 날짜가 있을 경우 isABirthday만 업데이트
                            val existingEvent = importantDates[existingEventIndex]
                            importantDates[existingEventIndex] =
                                existingEvent.copy(
                                    isABirthday = true,
                                )
                        } else {
                            // 중복된 날짜가 없으면 새로 추가
                            importantDates +=
                                DDayData(
                                    label =
                                        resourceProvider.asString(
                                            R.string.day_count,
                                            calculateDaysSinceStart(startDate, nextBirthday),
                                        ),
                                    date = nextBirthday,
                                    dDayCount = nextBirthday.daysUntil(today),
                                    isABirthday = true,
                                )
                        }
                    }
            }

            profileB?.let {
                val thisYearBirthDay =
                    LocalDate(
                        year = today.year,
                        monthNumber = profileB.birthday.monthNumber,
                        dayOfMonth = profileB.birthday.dayOfMonth,
                    )
                val nextClosestBirthday =
                    if (thisYearBirthDay < today) {
                        thisYearBirthDay.plus(DatePeriod(years = 1))
                    } else {
                        thisYearBirthDay
                    }

                generateSequence(nextClosestBirthday) { it.plus(DatePeriod(years = 1)) }
                    .takeWhile { it <= startDate.plus(DatePeriod(years = 30)) }
                    .forEach { nextBirthday ->
                        val existingEventIndex = importantDates.indexOfFirst { it.date == nextBirthday }
                        if (existingEventIndex != -1) {
                            // 중복된 날짜가 있을 경우 isABirthday만 업데이트
                            val existingEvent = importantDates[existingEventIndex]
                            importantDates[existingEventIndex] =
                                existingEvent.copy(
                                    isBBirthday = true,
                                )
                        } else {
                            // 중복된 날짜가 없으면 새로 추가
                            importantDates +=
                                DDayData(
                                    label =
                                        resourceProvider.asString(
                                            R.string.day_count,
                                            calculateDaysSinceStart(startDate, nextBirthday),
                                        ),
                                    date = nextBirthday,
                                    dDayCount = nextBirthday.daysUntil(today),
                                    isBBirthday = true,
                                )
                        }
                    }
            }

            val sortedDates = importantDates.sortedBy { it.date }

            val formattedDates =
                sortedDates.map { data ->
                    Triple(data.label, formatWithBirthdayInfo(data, profileA, profileB), formatToDDay(data.dDayCount))
                }
            return formattedDates
        }

        private fun formatToDDay(dayDifference: Int): String =
            when {
                dayDifference < 0 -> resourceProvider.asString(R.string.d_day_format, dayDifference.absoluteValue)
                dayDifference == 0 -> resourceProvider.asString(R.string.today)
                else -> ""
            }

        private fun formatWithBirthdayInfo(
            data: DDayData,
            profileA: ProfileData?,
            profileB: ProfileData?,
        ): String {
            val dayOfWeekKorean =
                when (data.date.dayOfWeek) {
                    DayOfWeek.MONDAY -> resourceProvider.asString(R.string.monday)
                    DayOfWeek.TUESDAY -> resourceProvider.asString(R.string.tuesday)
                    DayOfWeek.WEDNESDAY -> resourceProvider.asString(R.string.wednesday)
                    DayOfWeek.THURSDAY -> resourceProvider.asString(R.string.thursday)
                    DayOfWeek.FRIDAY -> resourceProvider.asString(R.string.friday)
                    DayOfWeek.SATURDAY -> resourceProvider.asString(R.string.saturday)
                    DayOfWeek.SUNDAY -> resourceProvider.asString(R.string.sunday)
                }

            val birthdayInfo = mutableListOf<String>()
            if (data.isABirthday == true && profileA?.nickname != null) {
                birthdayInfo.add(
                    resourceProvider.asString(R.string.birthday_emoji_nick, EMOJIS.CAKE, profileA.nickname),
                )
            }
            if (data.isBBirthday == true && profileB?.nickname != null) {
                birthdayInfo.add(
                    resourceProvider.asString(R.string.birthday_emoji_nick, EMOJIS.CAKE, profileB.nickname),
                )
            }

            return if (birthdayInfo.isNotEmpty()) {
                resourceProvider.asString(
                    R.string.birthday_info_two,
                    data.date,
                    dayOfWeekKorean,
                    birthdayInfo.joinToString(", "),
                )
            } else {
                resourceProvider.asString(R.string.birthday_info_one, data.date, dayOfWeekKorean)
            }
        }
    }
