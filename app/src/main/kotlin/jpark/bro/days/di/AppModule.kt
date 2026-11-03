package jpark.bro.days.di

import jpark.bro.days.MainViewModel
import jpark.bro.days.core.ads.di.adsModule
import jpark.bro.days.core.data.di.dataModule
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.database.di.databaseModule
import jpark.bro.days.core.datastore.di.datastoreModule
import jpark.bro.days.core.firebase.di.firebaseModule
import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.core.network.di.networkModule
import jpark.bro.days.core.notification.di.notificationModule
import jpark.bro.days.core.sync.di.syncModule
import jpark.bro.days.feature.calendar.impl.di.calendarModule
import jpark.bro.days.feature.couple.impl.di.coupleModule
import jpark.bro.days.feature.home.impl.di.homeModule
import jpark.bro.days.feature.item.impl.di.itemModule
import jpark.bro.days.feature.notifications.impl.di.notificationsModule
import jpark.bro.days.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.days.feature.onboarding.impl.di.onboardingModule
import jpark.bro.days.feature.settings.api.SessionEndedHandler
import jpark.bro.days.feature.settings.impl.di.settingsModule
import jpark.bro.days.navigation.AppOnboardingResultHandler
import jpark.bro.days.navigation.AppSessionEndedHandler
import jpark.bro.days.navigation.TopLevelDestination
import jpark.bro.days.navigation.startKeyFor
import jpark.bro.days.widget.di.widgetModule
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule =
    module {
        // 로그인했으면 홈, 아니면 온보딩(지금은 로그인부터). 비로그인 사용 없음 (product-spec 3-2)
        single {
            Navigator(
                startKey = startKeyFor(signedIn = get<AuthRepository>().currentUser() != null),
                topLevelKeys = TopLevelDestination.keys,
            )
        }
        singleOf(::AppOnboardingResultHandler) bind OnboardingResultHandler::class
        singleOf(::AppSessionEndedHandler) bind SessionEndedHandler::class
        viewModelOf(::MainViewModel)

        includes(
            // core
            databaseModule,
            datastoreModule,
            networkModule,
            firebaseModule,
            dataModule,
            syncModule,
            notificationModule,
            adsModule,
            widgetModule,
            // feature
            onboardingModule,
            coupleModule,
            homeModule,
            calendarModule,
            itemModule,
            notificationsModule,
            settingsModule,
        )
    }
