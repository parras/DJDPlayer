/*
 * Copyright (C) 2014 Mikael Ståldal
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
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import static android.app.Activity.RESULT_OK;
import static nu.staldal.djdplayer.SettingsActivity.GOOGLE_ACCOUNT_NAME;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int PERFORM_AUTH_REQUEST = 1;

    private Preference gmusic;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        gmusic = findPreference("gmusic");

        // Check GMusic auth status
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String accountName = sharedPref.getString(GOOGLE_ACCOUNT_NAME, null);
        if (accountName != null)
            gmusic.setSummary("Already logged in as " + accountName);
        else
            gmusic.setSummary("Not logged in");

        // Handle GMusic login
        Preference loginButton = findPreference("login");
        loginButton.setOnPreferenceClickListener(preference -> {
            Intent launchAuth = new Intent(getContext(), GoogleMusicOAuthActivity.class);
            startActivityForResult(launchAuth, PERFORM_AUTH_REQUEST);
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PERFORM_AUTH_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String username = data.getStringExtra("username");
                gmusic.setSummary("Logged in as " + username);
            }
        }
    }

}