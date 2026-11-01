package jpark.bro.days.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.NavKey
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
                "jpark.bro.days.feature.settings.impl.LicenseDetailKey",
            ).map { Class.forName(it).kotlin }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `모든 Koin 모듈의 의존성이 해결됨`() {
        // Navigator(startKey, topLevelKeys)는 appModule에서 값을 직접 넣어 만든다
        val navigatorArgs = listOf(NavKey::class, Set::class)
        appModule.verify(extraTypes = listOf(Context::class, SavedStateHandle::class) + navigatorArgs + navKeyTypes)
    }
}
