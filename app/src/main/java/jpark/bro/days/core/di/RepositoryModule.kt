package jpark.bro.days.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jpark.bro.days.data.repository.CoupleDateRepositoryImpl
import jpark.bro.days.data.repository.ProfileRepositoryImpl
import jpark.bro.days.domain.repository.CoupleDateRepository
import jpark.bro.days.domain.repository.ProfileRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCoupleDateRepository(
        coupleDateRepositoryImpl: CoupleDateRepositoryImpl
    ) : CoupleDateRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ) : ProfileRepository

}