package jpark.bro.days.core.ads.di

import jpark.bro.days.core.ads.RewardedAds
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** [jpark.bro.days.core.ads.AdsConfig]는 app이 빌드 변형 값으로 넣는다 */
val adsModule =
    module {
        single { RewardedAds(androidContext(), get()) }
    }
