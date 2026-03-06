package com.example.kairn.di

import com.example.kairn.data.repository.AuthRepositoryImpl
import com.example.kairn.data.repository.ChatRepositoryImpl
import com.example.kairn.data.repository.FriendshipRepositoryImpl
import com.example.kairn.data.repository.HikeRepositoryImpl
import com.example.kairn.data.repository.ProfileRepositoryImpl
import com.example.kairn.domain.repository.AuthRepository
import com.example.kairn.domain.repository.ChatRepository
import com.example.kairn.domain.repository.FriendshipRepository
import com.example.kairn.domain.repository.HikeRepository
import com.example.kairn.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindFriendshipRepository(impl: FriendshipRepositoryImpl): FriendshipRepository

    @Binds
    @Singleton
    abstract fun bindHikeRepository(impl: HikeRepositoryImpl): HikeRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
