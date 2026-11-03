package jpark.bro.days.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.NavKey
import com.google.firebase.auth.FirebaseAuth
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.reflect.KClass

class KoinModulesTest {
    /** 화면 키를 parametersOf로 받는 ViewModel의 생성자 타입. feature 내부(internal) 키는 이름으로 찾는다. */
    private val navKeyTypes: List<KClass<*>> =
        listOf(ItemDetailKey::class, ItemEditKey::class) +
            listOf(
                "jpark.bro.days.feature.couple.impl.PairCodeConfirmKey",
                "jpark.bro.days.feature.settings.impl.PolicyDocKey",
            ).map { Class.forName(it).kotlin }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `모든 Koin 모듈의 의존성이 해결됨`() {
        // Navigator(startKey, topLevelKeys)는 appModule에서 값을 직접 넣어 만든다
        val navigatorArgs = listOf(NavKey::class, Set::class)
        // 위젯 설정 ViewModel은 위젯 id · 크기를 parametersOf로 받는다
        val widgetConfigArgs = listOf(Int::class, WidgetSize::class)
        // Firebase SDK 인스턴스는 Koin이 아니라 Firebase.auth가 만들어 데이터 소스에 넘긴다
        val firebaseTypes = listOf(FirebaseAuth::class)
        appModule.verify(
            extraTypes =
                listOf(Context::class, SavedStateHandle::class) + navigatorArgs + navKeyTypes + widgetConfigArgs +
                    firebaseTypes,
        )
    }
}
