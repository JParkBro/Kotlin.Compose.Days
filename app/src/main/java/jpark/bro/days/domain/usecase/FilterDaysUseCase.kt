package jpark.bro.days.domain.usecase

import jpark.bro.days.ui.state.FilterOption
import javax.inject.Inject

class FilterDaysUseCase
    @Inject
    constructor() {
        operator fun invoke(
            allDays: List<Triple<String, String, String>>,
            option: FilterOption?,
        ): List<Triple<String, String, String>> =
            when (option) {
                is FilterOption.All -> {
                    allDays
                }

                is FilterOption.Yearly -> {
                    allDays.filter { day ->
                        val year = day.second.split("-")[0]
                        year == option.year.toString()
                    }
                }

                is FilterOption.Monthly -> {
                    allDays.filter { day ->
                        val dateParts = day.second.split("-")
                        val year = dateParts[0]
                        val month = dateParts[1]
                        year == option.year.toString() && month == option.month.toString().padStart(2, '0')
                    }
                }

                else -> {
                    allDays
                }
            }
    }
