/*
 * Copyright (C) 2009 The Android Open Source Project
 * Copyright (C) 2013-2015 Mikael Ståldal
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

package nu.staldal.djdplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.widget.RemoteViews;

/**
 * Simple widget to show currently playing song along
 * with play/pause and next track buttons.  
 */
public class MediaAppWidgetProvider extends AppWidgetProvider {

    private static final MediaAppWidgetProvider INSTANCE = new MediaAppWidgetProvider();
    
    static MediaAppWidgetProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        
        // Send broadcast intent to any running MediaPlaybackService so it can
        // wrap around with an immediate update.
        Intent updateIntent = new Intent(MobileMediaPlaybackService.APPWIDGETUPDATE_ACTION);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }
    
    /**
     * Initialize given widgets to default state, where we launch Music on default click
     * and hide actions if service not running.
     */
    private void defaultAppWidget(Context context, int[] appWidgetIds) {
        final Resources res = context.getResources();
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        
        views.setTextViewText(R.id.title, res.getText(R.string.widget_initial_text));
        views.setTextViewText(R.id.artist, "");
        views.setTextViewText(R.id.genre, "");

        linkButtons(context, views, false /* not playing */);
        pushUpdate(context, appWidgetIds, views);
    }
    
    private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            gm.updateAppWidget(appWidgetIds, views);
        } else {
            gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }
    
    /**
     * Check against {@link AppWidgetManager} if there are any instances of this widget.
     */
    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, this.getClass()));
        return (appWidgetIds.length > 0);
    }

    /**
     * Handle a change notification coming over from {@link MobileMediaPlaybackService}
     */
    void notifyChange(nu.staldal.djdplayer.MediaPlaybackService service, String what) {
        if (hasInstances(service)) {
            if (nu.staldal.djdplayer.MediaPlaybackService.META_CHANGED.equals(what) ||
                    nu.staldal.djdplayer.MediaPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }
    
    /**
     * Update all active widget instances by pushing changes 
     */
    void performUpdate(nu.staldal.djdplayer.MediaPlaybackService service, int[] appWidgetIds) {
        final Resources res = service.getResources();
        final RemoteViews views = new RemoteViews(service.getPackageName(), R.layout.appwidget);
        
        CharSequence titleName = service.getTrackName();
        CharSequence artistName = service.getArtistName();
        CharSequence genreName = service.getGenreName();
        CharSequence errorState = null;
        
        // Format title string with track number, or show SD card message
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_SHARED) ||
                status.equals(Environment.MEDIA_UNMOUNTED)) {
            errorState = res.getText(R.string.sdcard_busy_title);
        } else if (status.equals(Environment.MEDIA_REMOVED)) {
            errorState = res.getText(R.string.sdcard_missing_title);
        } else if (titleName == null) {
            errorState = res.getText(R.string.emptyplaylist);
        }
        
        if (errorState != null) {
            // Show error state to user
            views.setTextViewText(R.id.title, errorState);
            views.setTextViewText(R.id.artist, "");
            views.setTextViewText(R.id.genre, "");
        } else {
            // No error, so show normal titles
            views.setTextViewText(R.id.title, titleName);
            views.setTextViewText(R.id.artist, artistName);
            views.setTextViewText(R.id.genre, genreName);
        }
        
        // Set correct drawable for pause state
        final boolean playing = service.isPlaying();
        if (playing) {
            views.setImageViewResource(R.id.pause, android.R.drawable.ic_media_pause);
        } else {
            views.setImageViewResource(R.id.pause, android.R.drawable.ic_media_play);
        }

        // Link actions buttons to intents
        linkButtons(service, views, playing);
        
        pushUpdate(service, appWidgetIds, views);
    }

    /**
     * Link up various button actions using pending intents.
     * 
     * @param playerActive {@code true} if player is active in background
     */
    private void linkButtons(Context context, RemoteViews views, boolean playerActive) {
        Class<?> activityClass = playerActive && !context.getResources().getBoolean(R.bool.tablet_layout)
                ? MediaPlaybackActivity.class
                : MusicBrowserActivity.class;

        views.setOnClickPendingIntent(R.id.appwidget, PendingIntent.getActivity(context,
                0, new Intent(context, activityClass), 0));

        views.setOnClickPendingIntent(R.id.pause, PendingIntent.getService(context,
                0, new Intent(nu.staldal.djdplayer.MediaPlaybackService.TOGGLEPAUSE_ACTION).setClass(context, MobileMediaPlaybackService.class), 0));

        views.setOnClickPendingIntent(R.id.next, PendingIntent.getService(context,
                0, new Intent(nu.staldal.djdplayer.MediaPlaybackService.NEXT_ACTION).setClass(context, MobileMediaPlaybackService.class), 0));

        views.setOnClickPendingIntent(R.id.prev, PendingIntent.getService(context,
                0, new Intent(nu.staldal.djdplayer.MediaPlaybackService.PREVIOUS_ACTION).setClass(context, MobileMediaPlaybackService.class), 0));
    }
}
