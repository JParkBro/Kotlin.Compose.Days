package jpark.bro.days.feature.home.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.home.impl.HomeCoupleSource
import jpark.bro.days.feature.home.impl.HomeRoot
import jpark.bro.days.feature.home.impl.HomeViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val homeModule =
    module {
        factoryOf(::HomeCoupleSource)
        viewModelOf(::HomeViewModel)

        navigation<HomeKey> {
            val navigator = get<Navigator>()
            HomeRoot(onNavigate = navigator::goTo)
        }
    }
