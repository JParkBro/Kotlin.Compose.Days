package jpark.bro.days.core.data.di

import android.annotation.SuppressLint
import android.content.Context
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.common.UuidIdGenerator
import jpark.bro.days.core.data.auth.CredentialManagerGoogleSignIn
import jpark.bro.days.core.data.auth.GoogleSignInClient
import jpark.bro.days.core.data.auth.KakaoSdkSignIn
import jpark.bro.days.core.data.auth.KakaoSignInClient
import jpark.bro.days.core.data.couple.CoupleTransitions
import jpark.bro.days.core.data.image.ImageStore
import jpark.bro.days.core.data.repository.ActivityRepository
import jpark.bro.days.core.data.repository.AnniversaryRepository
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.DefaultActivityRepository
import jpark.bro.days.core.data.repository.DefaultAnniversaryRepository
import jpark.bro.days.core.data.repository.DefaultAuthRepository
import jpark.bro.days.core.data.repository.DefaultCoupleRepository
import jpark.bro.days.core.data.repository.DefaultImageQuotaRepository
import jpark.bro.days.core.data.repository.DefaultImageRepository
import jpark.bro.days.core.data.repository.DefaultItemRepository
import jpark.bro.days.core.data.repository.DefaultOnboardingRepository
import jpark.bro.days.core.data.repository.DefaultProfileRepository
import jpark.bro.days.core.data.repository.DefaultSettingsRepository
import jpark.bro.days.core.data.repository.DefaultWidgetConfigRepository
import jpark.bro.days.core.data.repository.ImageQuotaRepository
import jpark.bro.days.core.data.repository.ImageRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.OnboardingRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import jpark.bro.days.core.data.session.FirebaseUserSession
import jpark.bro.days.core.data.session.UserSession
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.Clock

val dataModule =
    module {
        single<Clock> { Clock.systemDefaultZone() }
        single<IdGenerator> { UuidIdGenerator }
        singleOf(::FirebaseUserSession) bind UserSession::class
        singleOf(::DefaultAuthRepository) bind AuthRepository::class
        single<GoogleSignInClient> {
            CredentialManagerGoogleSignIn(
                webClientId = androidContext().defaultWebClientId(),
            )
        }
        singleOf(::KakaoSdkSignIn) bind KakaoSignInClient::class
        singleOf(::DefaultSettingsRepository) bind SettingsRepository::class
        singleOf(::DefaultItemRepository) bind ItemRepository::class
        singleOf(::DefaultProfileRepository) bind ProfileRepository::class
        single { CoupleTransitions(get(), get(), get(), get(), get(), get()) }
        single<CoupleRepository> { DefaultCoupleRepository(get(), get(), get(), get(), get(), get()) }
        singleOf(::DefaultAnniversaryRepository) bind AnniversaryRepository::class
        single { ImageStore(androidContext(), Dispatchers.IO) }
        singleOf(::DefaultImageRepository) bind ImageRepository::class
        singleOf(::DefaultImageQuotaRepository) bind ImageQuotaRepository::class
        singleOf(::DefaultOnboardingRepository) bind OnboardingRepository::class
        singleOf(::DefaultWidgetConfigRepository) bind WidgetConfigRepository::class
        // ActivityNotifier 구현은 core:notification (notificationModule)
        singleOf(::DefaultActivityRepository) bind ActivityRepository::class
    }

/** google-services 플러그인이 app 리소스에 만드는 웹 클라이언트 ID. core:data는 app R을 볼 수 없어 이름으로 찾는다 */
@SuppressLint("DiscouragedApi")
private fun Context.defaultWebClientId(): String =
    getString(resources.getIdentifier("default_web_client_id", "string", packageName))
