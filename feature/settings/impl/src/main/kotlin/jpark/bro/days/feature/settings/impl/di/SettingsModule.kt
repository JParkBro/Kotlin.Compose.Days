package jpark.bro.days.feature.settings.impl.di

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.settings.api.SessionEnded
import jpark.bro.days.feature.settings.api.SessionEndedHandler
import jpark.bro.days.feature.settings.api.SettingsKey
import jpark.bro.days.feature.settings.impl.AccountKey
import jpark.bro.days.feature.settings.impl.LicenseDetailKey
import jpark.bro.days.feature.settings.impl.LicensesKey
import jpark.bro.days.feature.settings.impl.NotificationSettingsKey
import jpark.bro.days.feature.settings.impl.PolicyDocKey
import jpark.bro.days.feature.settings.impl.PolicyListKey
import jpark.bro.days.feature.settings.impl.ProfileEditKey
import jpark.bro.days.feature.settings.impl.WidgetSettingsKey
import jpark.bro.days.feature.settings.impl.WithdrawKey
import jpark.bro.days.feature.settings.impl.account.AccountRoot
import jpark.bro.days.feature.settings.impl.account.AccountViewModel
import jpark.bro.days.feature.settings.impl.licensedetail.LicenseDetailRoot
import jpark.bro.days.feature.settings.impl.licensedetail.LicenseDetailViewModel
import jpark.bro.days.feature.settings.impl.licenses.LicensesRoot
import jpark.bro.days.feature.settings.impl.licenses.LicensesViewModel
import jpark.bro.days.feature.settings.impl.notificationsettings.NotificationSettingsRoot
import jpark.bro.days.feature.settings.impl.notificationsettings.NotificationSettingsViewModel
import jpark.bro.days.feature.settings.impl.policydoc.PolicyDocRoot
import jpark.bro.days.feature.settings.impl.policydoc.PolicyDocViewModel
import jpark.bro.days.feature.settings.impl.policylist.PolicyListRoot
import jpark.bro.days.feature.settings.impl.policylist.PolicyListViewModel
import jpark.bro.days.feature.settings.impl.profileedit.ProfileEditRoot
import jpark.bro.days.feature.settings.impl.profileedit.ProfileEditViewModel
import jpark.bro.days.feature.settings.impl.settings.SettingsRoot
import jpark.bro.days.feature.settings.impl.settings.SettingsViewModel
import jpark.bro.days.feature.settings.impl.widgetsettings.WidgetSettingsRoot
import jpark.bro.days.feature.settings.impl.widgetsettings.WidgetSettingsViewModel
import jpark.bro.days.feature.settings.impl.withdraw.WithdrawRoot
import jpark.bro.days.feature.settings.impl.withdraw.WithdrawViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val settingsModule =
    module {
        viewModelOf(::SettingsViewModel)
        viewModelOf(::ProfileEditViewModel)
        viewModelOf(::NotificationSettingsViewModel)
        viewModelOf(::WidgetSettingsViewModel)
        viewModelOf(::AccountViewModel)
        viewModelOf(::WithdrawViewModel)
        viewModelOf(::PolicyListViewModel)
        viewModel { params -> PolicyDocViewModel(key = params.get()) }
        viewModelOf(::LicensesViewModel)
        viewModel { params -> LicenseDetailViewModel(key = params.get()) }

        navigation<SettingsKey> {
            val navigator = get<Navigator>()
            SettingsRoot(onNavigate = navigator::goTo)
        }
        navigation<ProfileEditKey> {
            val navigator = get<Navigator>()
            ProfileEditRoot(onBack = navigator::goBack)
        }
        navigation<NotificationSettingsKey> {
            val navigator = get<Navigator>()
            NotificationSettingsRoot(onBack = navigator::goBack)
        }
        navigation<WidgetSettingsKey> {
            val navigator = get<Navigator>()
            WidgetSettingsRoot(onBack = navigator::goBack)
        }
        navigation<AccountKey> {
            val navigator = get<Navigator>()
            val sessionEndedHandler = get<SessionEndedHandler>()
            AccountRoot(onNavigate = navigator::goTo, onSessionEnded = {
                sessionEndedHandler.handle(SessionEnded)
            }, onBack = navigator::goBack)
        }
        navigation<WithdrawKey> {
            val navigator = get<Navigator>()
            val sessionEndedHandler = get<SessionEndedHandler>()
            WithdrawRoot(onSessionEnded = { sessionEndedHandler.handle(SessionEnded) }, onBack = navigator::goBack)
        }
        navigation<PolicyListKey> {
            val navigator = get<Navigator>()
            PolicyListRoot(onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
        navigation<PolicyDocKey> { key ->
            val navigator = get<Navigator>()
            PolicyDocRoot(key = key, onBack = navigator::goBack)
        }
        navigation<LicensesKey> {
            val navigator = get<Navigator>()
            LicensesRoot(onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
        navigation<LicenseDetailKey> { key ->
            val navigator = get<Navigator>()
            LicenseDetailRoot(key = key, onBack = navigator::goBack)
        }
    }
