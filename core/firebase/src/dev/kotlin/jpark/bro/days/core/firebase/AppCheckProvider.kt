package jpark.bro.days.core.firebase

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/** dev: 디버그 제공자. 첫 실행 때 logcat(DebugAppCheckProvider)에 찍힌 토큰을 Firebase 콘솔 App Check에 등록한다 */
internal fun appCheckProviderFactory(): AppCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
