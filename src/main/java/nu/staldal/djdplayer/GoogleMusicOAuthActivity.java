package nu.staldal.djdplayer;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;

public class GoogleMusicOAuthActivity extends AppCompatActivity {

    private static final String GOOGLE_MUSIC_TOKEN = "GoogleMusicToken";
    private static final String GOOGLE_ACCOUNT_NAME = "GoogleAccountName";
    private static final String TAG = GoogleMusicOAuthActivity.class.getSimpleName();
    private static final int PICK_ACCOUNT_REQUEST = 1;
    private TextView statusText;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_music_oauth);
        statusText = (TextView) findViewById(R.id.google_music_auth_status);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        String accountName = sharedPref.getString(GOOGLE_ACCOUNT_NAME, null);
        if (accountName != null)
            statusText.setText("Already logged in as " + accountName);
    }

    public void performOAuth (View view) {
        Intent intent = AccountManager.newChooseAccountIntent(null, null,
                new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE  }, true,
                "Pick Google Music account", null,
                null, null);
        startActivityForResult(intent, PICK_ACCOUNT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == PICK_ACCOUNT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                new PerformOAuth().execute(accountName);
            }
        }
    }


    private class PerformOAuth extends AsyncTask<String, Void, String> {
        private String mAuthToken;

        protected String doInBackground(String... params) {
            String accountName = params[0];
            try {
                mAuthToken = GoogleAuthUtil.getToken(getApplicationContext(), accountName, "oauth2:https://www.googleapis.com/auth/skyjam");
                Log.d(TAG, "Token retrieved: " + mAuthToken);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(GOOGLE_ACCOUNT_NAME, accountName);
                editor.putString(GOOGLE_MUSIC_TOKEN, mAuthToken);
                editor.commit();

            } catch (IOException e) {
                Log.e(TAG, "Network is busy. Try again later...", e);
            } catch (GoogleAuthException e) {
                Log.e(TAG, "Fatal auth exception", e);
            }
            return mAuthToken;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }
}
