/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2014-2015 Mikael Ståldal
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    private static final String LOGTAG = "MediaButtonIntentRecv";

    private static long mLastClickTime = 0;
    private static boolean mDown = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            
            if (event == null) {
                return;
            }

            Log.d(LOGTAG, "Button: " + event.toString());

            // single quick press: pause/resume.
            // double press: next track

            String action = null;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    action = nu.staldal.djdplayer.MediaPlaybackService.PLAY_ACTION;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    action = nu.staldal.djdplayer.MediaPlaybackService.PAUSE_ACTION;
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    action = nu.staldal.djdplayer.MediaPlaybackService.STOP_ACTION;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    action = nu.staldal.djdplayer.MediaPlaybackService.TOGGLEPAUSE_ACTION;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    action = nu.staldal.djdplayer.MediaPlaybackService.NEXT_ACTION;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    action = nu.staldal.djdplayer.MediaPlaybackService.PREVIOUS_ACTION;
                    break;
            }

            if (action != null) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (!mDown) {
                        // if this isn't a repeat event

                        // The service may or may not be running, but we need to send it a command.
                        Intent i = new Intent(context, MobileMediaPlaybackService.class);
                        if (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK && event.getEventTime() - mLastClickTime < 300) {
                            action = nu.staldal.djdplayer.MediaPlaybackService.NEXT_ACTION;
                            mLastClickTime = 0;
                        } else {
                            mLastClickTime = event.getEventTime();
                        }
                        i.setAction(action);
                        context.startService(i);

                        mDown = true;
                    }
                } else {
                    mDown = false;
                }
                if (isOrderedBroadcast()) {
                    abortBroadcast();
                }
            }
        }
    }
}
