package jpark.bro.days.feature.onboarding.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.onboarding.api.OnboardingKey
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import jpark.bro.days.feature.onboarding.api.OnboardingResultHandler
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
import jpark.bro.days.feature.settings.api.PolicyDocKey
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

        // 소개(기기마다 한 번) → 로그인 → 약관 동의(계정마다 한 번) → 시작 방식. 이미 동의한 계정은 로그인 후 바로 끝
        navigation<OnboardingKey> {
            val navigator = get<Navigator>()
            IntroRoot(onNavigateToLogin = { navigator.replaceTop(LoginKey) })
        }
        navigation<LoginKey> {
            val navigator = get<Navigator>()
            LoginRoot(
                onNeedsTerms = { navigator.goTo(TermsKey) },
                onFinished = { get<OnboardingResultHandler>().handle(OnboardingResult.Solo) },
            )
        }
        navigation<TermsKey> {
            val navigator = get<Navigator>()
            TermsRoot(
                onNavigateToStartMode = { navigator.replaceTop(StartModeKey) },
                onOpenPolicy = { type -> navigator.goTo(PolicyDocKey(type)) },
                onBack = navigator::goBack,
            )
        }
        navigation<StartModeKey> {
            StartModeRoot(onFinished = get<OnboardingResultHandler>()::handle)
        }
    }
