package jpark.bro.days.core.firebase.di

import org.koin.dsl.module

/**
 * Firebase SDK 인스턴스(FirebaseAuth · FirebaseFirestore)는 Koin에 직접 등록하지 않는다.
 * 데이터 소스(6-3부터)가 생성자로 받도록 `single { XxxDataSource(Firebase.auth) }`처럼 감싸 등록한다.
 * (SDK 클래스를 등록하면 Koin verify가 SDK 내부 생성자 타입까지 요구한다)
 */
val firebaseModule =
    module {
    }
