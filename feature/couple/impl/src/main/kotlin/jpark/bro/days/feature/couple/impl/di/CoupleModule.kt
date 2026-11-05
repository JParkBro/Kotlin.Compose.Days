package jpark.bro.days.feature.couple.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.couple.impl.AnniversaryListKey
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import jpark.bro.days.feature.couple.impl.anniversaries.AnniversaryListRoot
import jpark.bro.days.feature.couple.impl.anniversaries.AnniversaryListViewModel
import jpark.bro.days.feature.couple.impl.couplemanage.CoupleManageRoot
import jpark.bro.days.feature.couple.impl.couplemanage.CoupleManageViewModel
import jpark.bro.days.feature.couple.impl.paircodeconfirm.PairCodeConfirmRoot
import jpark.bro.days.feature.couple.impl.paircodeconfirm.PairCodeConfirmViewModel
import jpark.bro.days.feature.couple.impl.pairing.PairingRoot
import jpark.bro.days.feature.couple.impl.pairing.PairingViewModel
import jpark.bro.days.feature.home.api.HomeKey
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val coupleModule =
    module {
        viewModelOf(::PairingViewModel)
        viewModel { params -> PairCodeConfirmViewModel(key = params.get(), coupleRepository = get(), clock = get()) }
        viewModelOf(::CoupleManageViewModel)
        viewModelOf(::AnniversaryListViewModel)

        navigation<PairingKey> {
            val navigator = get<Navigator>()
            PairingRoot(
                onNavigate = navigator::goTo,
                onConnected = { navigator.replaceAll(HomeKey) },
                onBack = navigator::goBack,
            )
        }
        navigation<PairCodeConfirmKey> { key ->
            val navigator = get<Navigator>()
            PairCodeConfirmRoot(key = key, onConnected = { navigator.replaceAll(HomeKey) }, onBack = navigator::goBack)
        }
        navigation<CoupleManageKey> {
            val navigator = get<Navigator>()
            CoupleManageRoot(onOpenAnniversaries = { navigator.goTo(AnniversaryListKey) }, onBack = navigator::goBack)
        }
        navigation<AnniversaryListKey> {
            val navigator = get<Navigator>()
            AnniversaryListRoot(onBack = navigator::goBack)
        }
    }
