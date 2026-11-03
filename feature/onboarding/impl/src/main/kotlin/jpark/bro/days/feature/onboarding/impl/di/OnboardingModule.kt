package jpark.bro.days.feature.onboarding.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.onboarding.api.OnboardingKey
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import jpark.bro.days.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.days.feature.onboarding.impl.IntroKey
import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey
import jpark.bro.days.feature.onboarding.impl.intro.IntroRoot
import jpark.bro.days.feature.onboarding.impl.intro.IntroViewModel
import jpark.bro.days.feature.onboarding.impl.login.LoginRoot
import jpark.bro.days.feature.onboarding.impl.login.LoginViewModel
import jpark.bro.days.feature.onboarding.impl.startmode.StartModeRoot
import jpark.bro.days.feature.onboarding.impl.startmode.StartModeViewModel
import jpark.bro.days.feature.onboarding.impl.terms.TermsRoot
import jpark.bro.days.feature.onboarding.impl.terms.TermsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val onboardingModule =
    module {
        viewModelOf(::IntroViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::TermsViewModel)
        viewModelOf(::StartModeViewModel)

        // 소개 · 약관 · 시작 방식은 7단계에서 만든다. 그때까지 온보딩은 로그인부터 시작하고, 로그인하면 혼자 시작(홈)
        navigation<OnboardingKey> {
            LoginRoot(onLoggedIn = { get<OnboardingResultHandler>().handle(OnboardingResult.Solo) })
        }
        navigation<LoginKey> {
            LoginRoot(onLoggedIn = { get<OnboardingResultHandler>().handle(OnboardingResult.Solo) })
        }
        navigation<IntroKey> {
            val navigator = get<Navigator>()
            IntroRoot(onNavigate = navigator::goTo)
        }
        navigation<TermsKey> {
            val navigator = get<Navigator>()
            TermsRoot(onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
        navigation<StartModeKey> {
            StartModeRoot(onFinished = get<OnboardingResultHandler>()::handle)
        }
    }
