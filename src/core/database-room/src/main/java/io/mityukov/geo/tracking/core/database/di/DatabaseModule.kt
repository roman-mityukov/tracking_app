package io.mityukov.geo.tracking.core.database.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.database.AppDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Singleton
    @Provides
    internal fun providesDb(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    internal fun providesUserTaskDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }
}
