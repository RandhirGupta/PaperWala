/*
 * Copyright 2018 randhirgupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyborg.paperwala.di

import com.cyborg.paperwala.network.LiveDataCallAdapterFactory
import com.cyborg.paperwala.network.PaperWalaApiService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object RepositoryModule {

    private const val URL = ""

    @Provides
    @Singleton
    @JvmStatic
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder().addHeader("Accept", "application/json").build()
            chain.proceed(request)
        }.connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideRetrofit(okHttpClient: OkHttpClient, callAdapter: CallAdapter.Factory): Retrofit {
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(callAdapter)
                .client(okHttpClient)
                .baseUrl(URL)
                .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun providesCallAdapterFactory(): CallAdapter.Factory {
        return LiveDataCallAdapterFactory()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun providesPaperWalaApiService(retrofit: Retrofit): PaperWalaApiService {
        return retrofit.create(PaperWalaApiService::class.java)
    }
}