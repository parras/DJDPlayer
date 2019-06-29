/*
 * Copyright (C) 2015 Mikael Ståldal
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

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MyMediaPlayer {
    private static final String LOGTAG = "MyMediaPlayer";

    public static final int TRACK_ENDED = 1;
    public static final int RELEASE_WAKELOCK = 2;
    public static final int SERVER_DIED = 3;

    private final Context mContext;
    private final Handler mHandler;
    private final PowerManager.WakeLock mWakeLock;

    private SimpleExoPlayer mPlayer;
    private boolean mIsInitialized;

    public MyMediaPlayer(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakeLock.setReferenceCounted(false);

        mPlayer = ExoPlayerFactory.newSimpleInstance(context);;
        // TODO acquire WakeLock and WifiLock when streaming

        mIsInitialized = false;
    }

    private final Player.EventListener listener = new Player.EventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                // Acquire a temporary wakelock, since when we return from
                // this callback the ExoPlayer will release its wakelock
                // and allow the device to go to sleep.
                // This temporary wakelock is released when the RELEASE_WAKELOCK
                // message is processed, but just in case, put a timeout on it.
                mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            Log.w(LOGTAG, "ExoPlayer error: " + e);
            // TODO handle error
        }
    };

    /**
     * @return true if successful, false if failed
     */
    public boolean prepare(String path) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "DJDPlayer"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(path));
        mPlayer.prepare(videoSource);
        mPlayer.setPlaybackParameters(new PlaybackParameters(1, 1, true));

        mPlayer.addListener(listener);

        Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
        mContext.sendBroadcast(i);

        Log.d(LOGTAG, "Prepared song: " + path);

        mIsInitialized = true;
        return true;
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_READY;
    }

    public void start() {
        mPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        mPlayer.setPlayWhenReady(false);
    }

    public void stop() {
        mPlayer.stop(true);
        mIsInitialized = false;
    }

    public long duration() {
        return mPlayer.getDuration();
    }

    public long currentPosition() {
        return mPlayer.getCurrentPosition();
    }

    public void seek(long whereto) {
        mPlayer.seekTo((int)whereto);
    }

    public void setVolume(float vol) {
        mPlayer.setVolume(vol);
    }

    public int getAudioSessionId() {
        return mPlayer.getAudioSessionId();
    }

    public void releaseWakeLock() {
        mWakeLock.release();
    }

    /**
     * You CANNOT use this player anymore after calling release()
     */
    public void release() {
        stop();

        Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
        mContext.sendBroadcast(i);

        mPlayer.release();
        mWakeLock.release();
    }

}
