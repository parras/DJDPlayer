package nu.staldal.djdplayer.gmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.felixgail.gplaymusic.util.TokenProvider;
import com.google.gson.Gson;

import java.io.IOException;

import nu.staldal.djdplayer.R;
import svarzee.gps.gpsoauth.AuthToken;
import svarzee.gps.gpsoauth.Gpsoauth;

import static android.provider.Settings.Secure.ANDROID_ID;
import static nu.staldal.djdplayer.SettingsActivity.GOOGLE_ACCOUNT_NAME;
import static nu.staldal.djdplayer.SettingsActivity.GOOGLE_MUSIC_TOKEN;

public class GoogleMusicOAuthActivity extends AppCompatActivity {

    private static final String TAG = GoogleMusicOAuthActivity.class.getSimpleName();
    private TextView statusText;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_music_oauth);
        statusText = findViewById(R.id.google_music_auth_status);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String accountName = sharedPref.getString(GOOGLE_ACCOUNT_NAME, null);
        if (accountName != null)
            statusText.setText("Already logged in as " + accountName);
    }

    public void performOAuth (View view) {
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);

        new PerformOAuth().execute(username.getText().toString(), password.getText().toString());
    }


    private class PerformOAuth extends AsyncTask<String, Void, AuthToken> {
        private String username;

        protected AuthToken doInBackground(String... params) {
            username = params[0];
            String password = params[1];
            AuthToken token = null;
            try {
                token = TokenProvider.provideToken(username, password, ANDROID_ID);
                Log.d(TAG, "Token retrieved: " + token);
            } catch (IOException e) {
                Log.e(TAG, "Network is busy. Try again later...", e);
            } catch (Gpsoauth.TokenRequestFailed e) {
                Log.e(TAG, "Token request failed", e);
            }
            return token;
        }

        @Override
        protected void onPostExecute(AuthToken token) {
            super.onPostExecute(token);

            if (token != null) {
                Gson gson = new Gson();
                String jsonToken = gson.toJson(token);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(GOOGLE_ACCOUNT_NAME, username);
                editor.putString(GOOGLE_MUSIC_TOKEN, jsonToken);
                editor.commit();

                statusText.setText("Logged in as " + username);

                Intent result = new Intent();
                result.putExtra("username", username);
                setResult(RESULT_OK, result);
                finish();
            }
            else
                statusText.setText("Authentication failed for " + username);
        }
    }
}
