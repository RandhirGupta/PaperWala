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

package com.cyborg.paperwala.presentation.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


class VMAdapter<VM, V>(context: Context, private var mViewModels: List<VM>?, private val mViewProvider: ViewProvider<V>) :
        BaseAdapter() where V : View, V : Bindable<VM> {

    override fun getCount(): Int {
        return mViewModels!!.size
    }

    override fun getItem(position: Int): VM {
        return mViewModels!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @Suppress("UNCHECKED_CAST")
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var view: V? = convertView as V
        if (view == null) {
            view = mViewProvider.createView()
        }

        val viewModel = getItem(position)
        view.bindViewModel(viewModel)

        return view
    }

    fun setViewModels(viewModels: List<VM>) {
        mViewModels = viewModels
    }
}