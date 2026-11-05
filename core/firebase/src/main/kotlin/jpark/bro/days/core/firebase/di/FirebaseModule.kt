package jpark.bro.days.core.firebase.di

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.storage.storage
import jpark.bro.days.core.firebase.FUNCTIONS_REGION
import jpark.bro.days.core.firebase.auth.FirebaseAuthDataSource
import jpark.bro.days.core.firebase.config.RemoteFlags
import jpark.bro.days.core.firebase.functions.FirebaseFunctionsDataSource
import jpark.bro.days.core.firebase.sync.CoupleRemoteDataSource
import jpark.bro.days.core.firebase.sync.FirebaseImageDataSource
import jpark.bro.days.core.firebase.sync.FirestoreCoupleDataSource
import jpark.bro.days.core.firebase.sync.FirestoreProfileDataSource
import jpark.bro.days.core.firebase.sync.FirestoreSyncDataSource
import jpark.bro.days.core.firebase.sync.ImageRemoteDataSource
import jpark.bro.days.core.firebase.sync.ProfileRemoteDataSource
import jpark.bro.days.core.firebase.sync.SyncRemoteDataSource
import org.koin.dsl.module

/**
 * Firebase SDK 인스턴스(FirebaseAuth · FirebaseFirestore · FirebaseFunctions · FirebaseStorage)는
 * Koin에 직접 등록하지 않고 데이터 소스로 감싼다.
 * (SDK 클래스를 등록하면 Koin verify가 SDK 내부 생성자 타입까지 요구한다)
 * 인스턴스는 FirebaseSetup.initialize 이후의 것(에뮬레이터 · 캐시 설정이 적용된 것)이다.
 */
val firebaseModule =
    module {
        single { FirebaseAuthDataSource(Firebase.auth) }
        single { FirebaseFunctionsDataSource(Firebase.functions(FUNCTIONS_REGION)) }
        single { RemoteFlags(Firebase.remoteConfig) }
        single<SyncRemoteDataSource> { FirestoreSyncDataSource(Firebase.firestore) }
        single<ProfileRemoteDataSource> { FirestoreProfileDataSource(Firebase.firestore) }
        single<CoupleRemoteDataSource> { FirestoreCoupleDataSource(Firebase.firestore) }
        single<ImageRemoteDataSource> { FirebaseImageDataSource(Firebase.firestore, Firebase.storage) }
    }
