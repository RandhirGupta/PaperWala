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

package com.cyborg.paperwala.data.network

import com.cyborg.paperwala.data.network.model.GoogleNewsResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface PaperWalaApiService {

    @GET("https://newsapi.org/v2/top-headlines?")
    fun fetchGoogleNews(@Query("sources") source: String, @Query("apiKey") apiKey: String): Observable<GoogleNewsResponse>

    @GET("http://webhose.io/filterWebContent?")
    fun fetchWebHoseNews(@Query("token") token: String, @Query(     "format") format: String, @Query("sort") sort: String): Observable<GoogleNewsResponse>
}