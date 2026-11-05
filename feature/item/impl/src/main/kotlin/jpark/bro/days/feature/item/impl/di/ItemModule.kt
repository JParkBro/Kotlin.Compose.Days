package jpark.bro.days.feature.item.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.item.impl.itemdetail.ItemDetailRoot
import jpark.bro.days.feature.item.impl.itemdetail.ItemDetailViewModel
import jpark.bro.days.feature.item.impl.itemedit.ItemEditRoot
import jpark.bro.days.feature.item.impl.itemedit.ItemEditViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val itemModule =
    module {
        viewModel { params ->
            ItemDetailViewModel(
                key = params.get(),
                itemRepository = get(),
                imageRepository = get(),
                homeWidgets = get(),
                clock = get(),
            )
        }
        viewModel { params ->
            ItemEditViewModel(
                key = params.get(),
                itemRepository = get(),
                coupleRepository = get(),
                imageRepository = get(),
                clock = get(),
            )
        }

        navigation<ItemDetailKey> { key ->
            val navigator = get<Navigator>()
            ItemDetailRoot(key = key, onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
        navigation<ItemEditKey> { key ->
            val navigator = get<Navigator>()
            ItemEditRoot(key = key, onBack = navigator::goBack)
        }
    }
