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

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.functions.Function
import java.util.*

class RxDataSourceSectioned<DataType>(var dataSet: List<DataType>, viewHolderInfoList: List<ViewHolderInfo>, viewTypeCallback: OnGetItemViewType) {

    private val rxAdapter: RxAdapterForTypes<DataType> = RxAdapterForTypes(dataSet, viewHolderInfoList, viewTypeCallback)

    fun bindRecyclerView(recyclerView: RecyclerView): Observable<TypesViewHolder<DataType>> {
        recyclerView.adapter = rxAdapter
        return rxAdapter.asObservable()
    }

    fun updateDataSet(dataSet: List<DataType>): RxDataSourceSectioned<DataType> {
        this.dataSet = dataSet
        return this
    }

    fun asObservable(): Observable<TypesViewHolder<DataType>> {
        return rxAdapter.asObservable()
    }

    fun updateAdapter() {
        //update the update
        rxAdapter.updateDataSet(dataSet)
    }

    fun map(mapper: Function<in DataType, out DataType>): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).map(mapper).toList().blockingGet()
        rxAdapter.updateDataSet(dataSet)
        return this
    }

    fun filter(predicate: (DataType) -> Boolean): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).filter(predicate).toList().blockingGet()
        rxAdapter.updateDataSet(dataSet)
        return this
    }

    fun last(): RxDataSourceSectioned<DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).blockingLast())
        return this
    }

    fun first(): RxDataSourceSectioned<DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).blockingFirst())
        return this
    }

    fun lastOrDefault(defaultValue: DataType): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet)
                .takeLast(1)
                .defaultIfEmpty(defaultValue)
                .toList()
                .blockingGet()
        return this
    }

    fun limit(count: Int): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).take(count.toLong()).toList().blockingGet()
        return this
    }

    fun repeat(count: Long): RxDataSourceSectioned<DataType> {
        val dataSet = dataSet
        this.dataSet = Observable.fromIterable(dataSet).repeat(count).toList().blockingGet()
        return this
    }

    fun empty(): RxDataSourceSectioned<DataType> {
        dataSet = Collections.emptyList<DataType>()
        return this
    }

    fun concatMap(func: (DataType) -> Observable<out DataType>): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).concatMap(func).toList().blockingGet()
        return this
    }

    fun concatWith(observable: Observable<out DataType>): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).concatWith(observable).toList().blockingGet()
        return this
    }

    fun distinct(): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).distinct().toList().blockingGet()
        return this
    }

    fun elementAt(index: Long): RxDataSourceSectioned<DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).elementAt(index).blockingGet())
        return this
    }

    fun elementAtOrDefault(index: Long, defaultValue: DataType): RxDataSourceSectioned<DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).elementAt(index, defaultValue)
                .blockingGet())
        return this
    }

    fun first(defaultItem: DataType): RxDataSourceSectioned<DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).first(defaultItem).blockingGet())
        return this
    }

    fun flatMap(func: (DataType) -> Observable<out DataType>): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).flatMap(func).toList().blockingGet()
        return this
    }

    fun reduce(initialValue: DataType, reducer: (DataType, DataType) -> DataType): RxDataSourceSectioned<DataType> {
        dataSet = listOf(Observable.fromIterable(dataSet).reduce(initialValue, reducer).blockingGet())
        return this
    }

    fun take(count: Long): RxDataSourceSectioned<DataType> {
        dataSet = Observable.fromIterable(dataSet).take(count).toList().blockingGet()
        return this
    }

}