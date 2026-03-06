package com.example.kairn.di

import com.example.kairn.data.repository.AuthRepositoryImpl
import com.example.kairn.data.repository.HikeRepositoryImpl
import com.example.kairn.data.repository.ProfileRepositoryImpl
import com.example.kairn.domain.repository.AuthRepository
import com.example.kairn.domain.repository.HikeRepository
import com.example.kairn.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHikeRepository(impl: HikeRepositoryImpl): HikeRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
