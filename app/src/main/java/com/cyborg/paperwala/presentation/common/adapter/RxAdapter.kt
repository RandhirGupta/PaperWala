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

package com.cyborg.paperwala.presentation.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal class RxAdapter<DataType, LayoutBinding : ViewDataBinding>(@param:LayoutRes private val mItem_layout: Int, dataSet: List<DataType>) : RecyclerView.Adapter<SimpleViewHolder<DataType, LayoutBinding>>() {

    private var dataSet: List<DataType>
    private val mPublishSubject: PublishSubject<SimpleViewHolder<DataType, LayoutBinding>>
    private var mOnViewHolderInflate: OnViewHolderInflated? = null

    init {
        this.dataSet = dataSet
        mPublishSubject = PublishSubject.create()
    }

    fun setOnViewHolderInflate(onViewHolderInflate: OnViewHolderInflated) {
        mOnViewHolderInflate = onViewHolderInflate
    }

    fun asObservable(): Observable<SimpleViewHolder<DataType, LayoutBinding>> {
        return mPublishSubject
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): SimpleViewHolder<DataType, LayoutBinding> {
        val view = LayoutInflater.from(parent.context).inflate(mItem_layout, parent, false)
        if (mOnViewHolderInflate != null) mOnViewHolderInflate!!.onInflated(view, parent, viewType)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder<DataType, LayoutBinding>,
                                  position: Int) {
        holder.item = dataSet[position]
        mPublishSubject.onNext(holder)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun updateDataSet(dataSet: List<DataType>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}