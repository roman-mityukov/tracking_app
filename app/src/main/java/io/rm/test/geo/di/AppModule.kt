package io.rm.test.geo.di

import android.content.Context
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    companion object {
        @Provides
        fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
            return LocationServices.getFusedLocationProviderClient(context)
        }

        @Provides
        fun provideCurrentLocationRequest(): CurrentLocationRequest {
            return CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setDurationMillis(30 * 1000)
                .setMaxUpdateAgeMillis(10 * 60 * 1000)
                .build()
        }

        @Provides
        fun provideLocationRequest() : LocationRequest {
            return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 60000)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .build()
        }
    }
}