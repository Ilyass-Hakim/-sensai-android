package com.example.sensai.di

import com.example.sensai.data.repository.AuthRepository
import com.example.sensai.data.repository.AuthRepositoryImpl
import com.example.sensai.data.repository.AnimeRepository
import com.example.sensai.data.repository.AnimeRepositoryImpl
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAnimeRepository(
        animeRepositoryImpl: AnimeRepositoryImpl
    ): AnimeRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        aiRepositoryImpl: com.example.sensai.data.repository.AiRepositoryImpl
    ): com.example.sensai.data.repository.AiRepository
}
