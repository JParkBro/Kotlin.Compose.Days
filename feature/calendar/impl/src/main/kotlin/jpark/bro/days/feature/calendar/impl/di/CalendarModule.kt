package jpark.bro.days.feature.calendar.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.calendar.api.CalendarKey
import jpark.bro.days.feature.calendar.impl.CalendarRoot
import jpark.bro.days.feature.calendar.impl.CalendarViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val calendarModule =
    module {
        viewModelOf(::CalendarViewModel)

        navigation<CalendarKey> {
            val navigator = get<Navigator>()
            CalendarRoot(onNavigate = navigator::goTo)
        }
    }
