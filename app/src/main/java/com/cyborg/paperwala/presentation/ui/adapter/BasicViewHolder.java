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

package com.cyborg.paperwala.presentation.ui.adapter;

import android.view.View;

public class BasicViewHolder<V extends View & Bindable<VM>, VM> extends BindableViewHolder<VM> {

    private final V itemView;

    public BasicViewHolder(V view) {
        super(view);
        itemView = view;
    }

    public View getView() {
        return itemView;
    }

    @Override
    public void bindViewModel(VM vm) {
        itemView.bindViewModel(vm);
    }
}