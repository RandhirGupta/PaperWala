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

package com.cyborg.paperwala.presentation.ui.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment : Fragment() {

    protected var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        if (rootView == null) {
            rootView = inflater.inflate(getLayout(), container, false)
        }

        return rootView
    }

    abstract fun getLayout(): Int

    override fun onDestroy() {
        super.onDestroy()
        clearView()
    }

    open fun showLoading() {
        activity?.let { (it as BaseActivity).showLoading() }
    }

    open fun hideLoading() {
        activity?.let { (it as BaseActivity).dismissLoading() }
    }

    open fun showSnackBarMessage(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    fun showSnackBarMessage(@StringRes message: Int) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    fun showSnackBarMessage(@StringRes message: Int, @StringRes action: Int, unit: () -> Unit) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).apply {
                setAction(action) {
                    unit()
                }
            }.show()
        }
    }

    fun clearView() {
        rootView = null
    }

    fun showMessageDialog(@StringRes message: Int,
                          @StringRes positive: Int? = null, positiveListener: DialogInterface.OnClickListener? = null,
                          @StringRes negative: Int? = null, negativeListener: DialogInterface.OnClickListener? = null) {
        activity?.let { (it as BaseActivity).showMessageDialog(message, positive, positiveListener, negative, negativeListener) }
    }

}