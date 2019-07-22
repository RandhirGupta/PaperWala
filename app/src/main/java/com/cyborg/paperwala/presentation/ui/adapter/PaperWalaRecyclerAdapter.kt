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

package com.cyborg.paperwala.presentation.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import rx.Observable
import rx.Subscription

class PaperWalaRecyclerAdapter<VM>(viewModels: Observable<List<VM>>, private val mViewProvider: ViewProvider<BindableViewHolder<VM>>) : RecyclerView.Adapter<BindableViewHolder<VM>>() {

    private var mViewModels: List<VM> = ArrayList()
    private val mSubscription: Subscription

    init {
        mSubscription = viewModels.subscribe { vms ->
            this.mViewModels = vms
            this.notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<VM> {
        return mViewProvider.createView()
    }

    override fun onBindViewHolder(holder: BindableViewHolder<VM>, position: Int) {
        val vm = mViewModels[position]
        holder.bindViewModel(vm)
    }

    override fun getItemCount(): Int {
        return mViewModels.size
    }

    fun close() {
        mSubscription.unsubscribe()
    }
}
