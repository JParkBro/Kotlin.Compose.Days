package jpark.bro.days.feature.home.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.home.impl.HomeRoot
import jpark.bro.days.feature.home.impl.HomeViewModel
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val homeModule =
    module {
        viewModelOf(::HomeViewModel)

        navigation<HomeKey> {
            val navigator = get<Navigator>()
            HomeRoot(onNavigate = navigator::goTo)
        }
    }
