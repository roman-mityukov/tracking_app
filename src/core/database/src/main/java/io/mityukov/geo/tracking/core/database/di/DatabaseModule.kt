package io.mityukov.geo.tracking.core.database.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun providesDb(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
}
