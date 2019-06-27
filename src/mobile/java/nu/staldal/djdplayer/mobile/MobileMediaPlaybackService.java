/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2012-2015 Mikael Ståldal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nu.staldal.djdplayer.mobile;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.app.NotificationCompat;

import nu.staldal.djdplayer.MediaPlaybackService;
import nu.staldal.djdplayer.R;

public class MobileMediaPlaybackService extends MediaPlaybackService {

    @SuppressWarnings("unused")
    private static final String LOGTAG = "MobileMediaPlaybackService";

    public static final String APPWIDGETUPDATE_ACTION = "nu.staldal.djdplayer.musicservicecommand.appwidgetupdate";

    // Delegates

    @Override
    protected void enrichActionFilter(IntentFilter actionFilter) {
        actionFilter.addAction(APPWIDGETUPDATE_ACTION);
    }

    @Override
    protected void handleAdditionalActions(Intent intent) {
        if (APPWIDGETUPDATE_ACTION.equals(intent.getAction())) {
            // Someone asked us to refresh a set of specific widgets, probably
            // because they were just added.
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            MediaAppWidgetProvider.getInstance().performUpdate(MobileMediaPlaybackService.this, appWidgetIds);
        }
    }

    @Override
    protected void extraNotifyChange(String what) {
        // Share this notification directly with our widgets
        MediaAppWidgetProvider.getInstance().notifyChange(this, what);
    }

    @Override
    protected void enrichNotification(NotificationCompat.Builder builder) {
        Class<?> activityClass = getResources().getBoolean(R.bool.tablet_layout)
                ? MusicBrowserActivity.class
                : MediaPlaybackActivity.class;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, activityClass).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                0);

        builder.setContentIntent(pendingIntent);
    }
}
