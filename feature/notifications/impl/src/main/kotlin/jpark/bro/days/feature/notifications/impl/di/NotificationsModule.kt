package jpark.bro.days.feature.notifications.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.notifications.api.NotificationsKey
import jpark.bro.days.feature.notifications.impl.NotificationsRoot
import jpark.bro.days.feature.notifications.impl.NotificationsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val notificationsModule =
    module {
        viewModelOf(::NotificationsViewModel)

        navigation<NotificationsKey> {
            val navigator = get<Navigator>()
            NotificationsRoot(onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
    }
