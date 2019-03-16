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

package com.cyborg.paperwala.ui.adapter

import android.view.View

class BasicViewHolder<V, VM>(private val itemView: V) : BindableViewHolder<VM>(itemView) where V : View, V : Bindable<VM> {

    val view: View
        get() = itemView

   override fun bindViewModel(vm: VM) {
        itemView.bindViewModel(vm)
    }
}