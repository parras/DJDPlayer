/*
 * Copyright (C) 2012-2016 Mikael Ståldal
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

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;

public class SettingsActivity extends AppCompatActivity {
    public static final String CLICK_ON_SONG = "clickonsong";
    public static final String SHOW_ARTISTS_TAB = "show_artists_tab";
    public static final String SHOW_ALBUMS_TAB = "show_albums_tab";
    public static final String SHOW_GENRES_TAB = "show_genres_tab";
    public static final String SHOW_FOLDERS_TAB = "show_folders_tab";
    public static final String SHOW_PLAYLISTS_TAB = "show_playlists_tab";
    public static final String MUSIC_FOLDER = "music_folder";
    public static final String FADE_SECONDS = "fade_seconds";
    public static final String CROSS_FADE = "cross_fade";
    public static final String SKIP_SILENCE = "skip_silence";
    public static final String CLIPPING = "clipping";

    public static final String GOOGLE_MUSIC_TOKEN = "GoogleMusicToken";
    public static final String GOOGLE_ACCOUNT_NAME = "GoogleAccountName";

    public static final String PLAYQUEUE = "queue";
    public static final String CARDID = "cardid";
    public static final String CURPOS = "curpos";
    public static final String SEEKPOS = "seekpos";
    public static final String REPEATMODE = "repeatmode";
    public static final String NUMWEEKS = "numweeks";
    public static final String ACTIVE_TAB = "ActiveTab";

    // CLICK_ON_SONG values
    public static final String PLAY_NEXT = "PLAY_NEXT";
    public static final String PLAY_NOW = "PLAY_NOW";
    public static final String QUEUE = "QUEUE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

}