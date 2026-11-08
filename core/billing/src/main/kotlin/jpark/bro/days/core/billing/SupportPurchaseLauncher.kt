package jpark.bro.days.core.billing

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * 화면 Root에서 후원 결제 창을 띄우는 함수를 만든다. ViewModel의 Event로 [SupportProduct]를 받아 부르고,
 * 결과는 [onResult]로 Action에 넘긴다 (ViewModel은 Activity · 결제 SDK를 모른다).
 */
@Composable
fun rememberSupportPurchaseLauncher(onResult: (SupportPurchaseResult) -> Unit): (SupportProduct) -> Unit {
    val activity = LocalActivity.current
    val store = koinInject<PlaySupportStore>()
    val scope = rememberCoroutineScope()
    val currentOnResult = rememberUpdatedState(onResult)
    return { product ->
        scope.launch {
            val result = if (activity == null) SupportPurchaseResult.Failed else store.purchase(activity, product)
            currentOnResult.value(result)
        }
    }
}
