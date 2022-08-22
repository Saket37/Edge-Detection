package com.example.edgedetectioninterview.di

import android.content.Context
import com.example.edgedetectioninterview.firebase.FirebaseRepository
import com.example.edgedetectioninterview.utils.BitmapHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideBitmapHelper() = BitmapHelper()

    @Singleton
    @Provides
    fun firebaseRepository(@ApplicationContext applicationContext: Context) = FirebaseRepository(applicationContext)
}