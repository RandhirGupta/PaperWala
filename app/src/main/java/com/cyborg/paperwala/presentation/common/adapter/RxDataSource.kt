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

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import java.util.Collections.emptyList

class RxDataSource<LayoutBinding : ViewDataBinding, DataType>(@LayoutRes private val itemLayout: Int, var dataSet: List<DataType>) {

    private val rxAdapter: RxAdapter<DataType, LayoutBinding> = RxAdapter(itemLayout, dataSet)

    fun bindRecyclerView(
            recyclerView: RecyclerView): Observable<SimpleViewHolder<DataType, LayoutBinding>> {
        recyclerView.adapter = rxAdapter
        return rxAdapter.asObservable()
    }

    fun asObservable(): Observable<SimpleViewHolder<DataType, LayoutBinding>> {
        return rxAdapter.asObservable()
    }

    fun updateDataSet(dataSet: List<DataType>): RxDataSource<LayoutBinding, DataType> {
        this.dataSet = dataSet
        return this
    }

    fun updateAdapter() {
        //update the update
        rxAdapter.updateDataSet(dataSet)
    }

    fun map(mapper: (DataType) -> DataType): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).map(mapper).toList().blockingGet()
        rxAdapter.updateDataSet(dataSet)
        return this
    }

    fun filter(predicate: (DataType) -> Boolean): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).filter(predicate).toList().blockingGet()
        rxAdapter.updateDataSet(dataSet)
        return this
    }

    fun last(): RxDataSource<LayoutBinding, DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).blockingLast())
        return this
    }

    fun first(): RxDataSource<LayoutBinding, DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).blockingFirst())
        return this
    }

    fun lastOrDefault(defaultValue: DataType): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet)
                .takeLast(1)
                .defaultIfEmpty(defaultValue)
                .toList()
                .blockingGet()
        return this
    }

    fun limit(count: Int): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).take(count.toLong()).toList().blockingGet()
        return this
    }

    fun repeat(count: Long): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).repeat(count).toList().blockingGet()
        return this
    }

    fun empty(): RxDataSource<LayoutBinding, DataType> {
        dataSet = emptyList<DataType>()
        return this
    }

    fun concatMap(func: (DataType) -> Observable<out DataType>): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).concatMap(func).toList().blockingGet()
        return this
    }

    fun concatWith(observable: Observable<out DataType>): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).concatWith(observable).toList().blockingGet()
        return this
    }

    fun distinct(): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).distinct().toList().blockingGet()
        return this
    }

    fun elementAt(index: Long): RxDataSource<LayoutBinding, DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).elementAt(index).blockingGet())
        return this
    }

    fun elementAtOrDefault(index: Long, defaultValue: DataType): RxDataSource<LayoutBinding, DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).elementAt(index, defaultValue)
                .blockingGet())
        return this
    }

    fun first(defaultItem: DataType): RxDataSource<LayoutBinding, DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).first(defaultItem).blockingGet())
        return this
    }

    fun flatMap(func: (DataType) -> Observable<out DataType>): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).flatMap(func).toList().blockingGet()
        return this
    }

    fun reduce(initialValue: DataType, reducer: (DataType, DataType) -> DataType): RxDataSource<LayoutBinding, DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).reduce(initialValue, reducer).blockingGet())
        return this
    }

    fun take(count: Long): RxDataSource<LayoutBinding, DataType> {
        dataSet = Observable.fromIterable(dataSet).take(count).toList().blockingGet()
        return this
    }

}