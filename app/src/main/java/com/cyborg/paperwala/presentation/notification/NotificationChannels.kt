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

package com.cyborg.paperwala.presentation.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import com.cyborg.paperwala.R

@RequiresApi(api = Build.VERSION_CODES.O)
internal class NotificationChannels(base: Context) : ContextWrapper(base) {

    val primaryChannel: String = getString(R.string.app_name)

    private var manager: NotificationManager? = null

    init {
        val channelPrimary = NotificationChannel(primaryChannel, primaryChannel, NotificationManager.IMPORTANCE_LOW)
        channelPrimary.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channelPrimary.setSound(null, null)
        channelPrimary.enableVibration(false)
        getManager().createNotificationChannel(channelPrimary)
    }

    private fun getManager(): NotificationManager {
        if (manager == null) {
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return manager!!
    }
}