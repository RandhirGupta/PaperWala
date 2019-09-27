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
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal class RxAdapterForTypes<T>(dataSet: List<T>, private val mViewHolderInfoList: List<ViewHolderInfo>, private val mViewTypeCallback: OnGetItemViewType) : RecyclerView.Adapter<TypesViewHolder<T>>() {

    var dataSet: List<T>? = null
        private set
    private val mPublishSubject: PublishSubject<TypesViewHolder<T>>
    private var mOnViewHolderInflate: OnViewHolderInflated? = null

    init {
        this.dataSet = dataSet
        mPublishSubject = PublishSubject.create()
    }

    fun setOnViewHolderInflate(onViewHolderInflate: OnViewHolderInflated) {
        mOnViewHolderInflate = onViewHolderInflate
    }

    fun asObservable(): Observable<TypesViewHolder<T>> {
        return mPublishSubject
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TypesViewHolder<T> {
        for (viewHolderInfo in mViewHolderInfoList) {
            if (viewType == viewHolderInfo.type) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(viewHolderInfo.layoutRes, parent, false)
                if (mOnViewHolderInflate != null)
                    mOnViewHolderInflate!!.onInflated(view, parent, viewType)
                return TypesViewHolder(view)
            }
        }
        throw RuntimeException("View Type in RxAdapter not found!")
    }

    override fun onBindViewHolder(holder: TypesViewHolder<T>, position: Int) {
        holder.item = dataSet!![position]
        mPublishSubject.onNext(holder)
    }

    override fun getItemCount(): Int {
        return dataSet!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return mViewTypeCallback.getItemViewType(position)
    }

    fun updateDataSet(dataSet: List<T>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}