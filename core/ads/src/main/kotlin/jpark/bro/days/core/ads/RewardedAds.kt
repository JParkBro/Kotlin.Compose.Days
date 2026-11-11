package jpark.bro.days.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** 앱이 넣는 AdMob 설정. dev는 Google 샘플 광고 단위, prod는 `local.properties` (module-map 8장) */
data class AdsConfig(
    val rewardedAdUnitId: String,
)

/**
 * 보상형 광고에 붙여 AdMob 서버 측 확인(SSV) 콜백으로 넘길 값 (`admobReward` 함수).
 * [userId] = Firebase uid, [customData] = 보상을 받을 커플 id
 */
data class RewardedAdRequest(
    val userId: String,
    val customData: String,
)

enum class RewardedAdResult {
    /** 끝까지 봐서 보상을 받음. 실제 보상(서버 bonus)은 SSV 콜백이 조금 늦게 반영한다 */
    Earned,

    /** 보상 전에 닫음 */
    Dismissed,

    /** 동의를 받지 못했거나, 광고를 불러오거나 보여주지 못함 */
    Failed,
}

/**
 * AdMob 보상형 광고 (ads-strategy.md 3장). 사용자가 버튼을 누를 때 불러와 바로 보여준다 (미리 불러 두지 않음).
 * 처음 한 번 맞춤 광고 동의(UMP)를 확인하고, 광고를 요청할 수 없으면 [RewardedAdResult.Failed].
 */
class RewardedAds(
    context: Context,
    private val config: AdsConfig,
) {
    private val appContext = context.applicationContext
    private var initialized = false

    /**
     * [request]가 null이면 서버 측 확인 값을 넣지 않는다 (백업처럼 앱에서만 보상하는 광고).
     * 같은 광고 단위라 AdMob 콜백은 오지만 `admobReward`가 값이 없어 무시한다 (함수 호출 1번)
     */
    suspend fun show(
        activity: Activity,
        request: RewardedAdRequest?,
    ): RewardedAdResult {
        val ad = if (gatherConsent(activity)) load() else null
        if (ad == null) return RewardedAdResult.Failed
        if (request != null) {
            ad.setServerSideVerificationOptions(
                ServerSideVerificationOptions
                    .Builder()
                    .setUserId(request.userId)
                    .setCustomData(request.customData)
                    .build(),
            )
        }
        return present(activity, ad)
    }

    /** EEA · 영국 등 동의가 필요한 지역이면 동의 화면을 띄운다. 그 밖은 바로 true */
    private suspend fun gatherConsent(activity: Activity): Boolean {
        val consent = UserMessagingPlatform.getConsentInformation(appContext)
        val updated =
            suspendCancellableCoroutine { continuation ->
                consent.requestConsentInfoUpdate(
                    activity,
                    ConsentRequestParameters.Builder().build(),
                    { continuation.resume(true) },
                    { continuation.resume(false) },
                )
            }
        if (updated) {
            suspendCancellableCoroutine { continuation ->
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { continuation.resume(Unit) }
            }
        }
        // 갱신에 실패해도 이전에 받은 동의로 요청할 수 있으면 진행한다
        return consent.canRequestAds()
    }

    private suspend fun initialize() {
        if (initialized) return
        suspendCancellableCoroutine { continuation ->
            MobileAds.initialize(appContext) { continuation.resume(Unit) }
        }
        initialized = true
    }

    private suspend fun load(): RewardedAd? {
        initialize()
        return suspendCancellableCoroutine { continuation ->
            RewardedAd.load(
                appContext,
                config.rewardedAdUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        continuation.resume(ad)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        continuation.resume(null)
                    }
                },
            )
        }
    }

    private suspend fun present(
        activity: Activity,
        ad: RewardedAd,
    ): RewardedAdResult =
        suspendCancellableCoroutine { continuation ->
            var earned = false
            ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        if (continuation.isActive) {
                            continuation.resume(if (earned) RewardedAdResult.Earned else RewardedAdResult.Dismissed)
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        if (continuation.isActive) continuation.resume(RewardedAdResult.Failed)
                    }
                }
            ad.show(activity) { earned = true }
        }
}
