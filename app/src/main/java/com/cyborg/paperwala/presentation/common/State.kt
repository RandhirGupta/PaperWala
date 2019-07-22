/*
 * Copyright 2019 randhirgupta
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

package com.cyborg.paperwala.presentation.common

sealed class State<T> {

    class Loading<T> : State<T>()
    data class Error<T>(val errorMessage: String?, val error: Throwable) : State<T>()
    data class Success<T>(var data: T) : State<T>()

    companion object {
        fun <T> loading(): State<T> =
                Loading()

        fun <T> error(errorMessage: String, error: Throwable): State<T> =
                Error(errorMessage, error)

        fun <T> success(data: T): State<T> =
                Success(data)
    }
}