package jpark.bro.days.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jpark.bro.days.data.local.CoupleDateDataStore
import jpark.bro.days.data.local.ProfileDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun provideCoupleDateDataStore(@ApplicationContext context: Context): CoupleDateDataStore {
        return CoupleDateDataStore(context = context)
    }

    @Singleton
    @Provides
    fun provideProfileDataStore(@ApplicationContext context: Context): ProfileDataStore {
        return ProfileDataStore(context = context)
    }

}