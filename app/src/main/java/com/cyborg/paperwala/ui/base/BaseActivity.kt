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

package com.cyborg.paperwala.ui.base

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cyborg.paperwala.R
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getLayout() != 0) setContentView(getLayout())
    }

    abstract fun getLayout(): Int

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    fun showBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun showLoading() {
        if (progressDialog == null) progressDialog = ProgressDialog(this)
        progressDialog?.setMessage(getString(R.string.please_wait))
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun showLoading(message: String) {
        if (progressDialog == null) progressDialog = ProgressDialog(this)
        progressDialog?.setMessage(message)
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun dismissLoading() {
        progressDialog?.let { if (it.isShowing) it.dismiss() }
    }

    fun showSnackBarMessage(message: String) {
        Snackbar.make(findViewById(R.id.root), message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackBarMessage(@StringRes message: Int) {
        Snackbar.make(findViewById(R.id.root), message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackBarMessage(@StringRes message: Int, @StringRes action: Int, unit: () -> Unit) {
        Snackbar.make(findViewById(R.id.root), message, Snackbar.LENGTH_LONG).apply {
            setAction(action) {
                unit()
            }
        }.show()
    }

    fun showMessageDialog(@StringRes message: Int,
                          @StringRes positive: Int? = null, positiveListener: DialogInterface.OnClickListener? = null,
                          @StringRes negative: Int? = null, negativeListener: DialogInterface.OnClickListener? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)

        positive?.let { builder.setPositiveButton(it, positiveListener) }

        negative?.let { builder.setNegativeButton(it, negativeListener) }

        builder.show()
    }

}