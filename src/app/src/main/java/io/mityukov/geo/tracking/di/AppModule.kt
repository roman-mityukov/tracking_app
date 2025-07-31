package io.mityukov.geo.tracking.di

import android.content.Context
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.app.AppProps
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
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
                .setDurationMillis(AppProps.LOCATION_REQUEST_DURATION)
                .setMaxUpdateAgeMillis(AppProps.LOCATION_MAX_UPDATE_AGE)
                .build()
        }

        @Provides
        @DispatcherIO
        fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

        @OptIn(ExperimentalCoroutinesApi::class)
        @Provides
        @DispatcherDefaultLimitedParallelism
        fun providesDispatcherDefaultLimitedParallelism(): CoroutineDispatcher =
            Dispatchers.Default.limitedParallelism(parallelism = 1)

        @Provides
        @DispatcherDefault
        fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    }
}
