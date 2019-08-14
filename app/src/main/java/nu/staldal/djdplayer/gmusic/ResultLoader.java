package nu.staldal.djdplayer.gmusic;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.model.Track;
import com.github.felixgail.gplaymusic.model.responses.Result;
import com.github.felixgail.gplaymusic.util.TokenProvider;

import java.io.IOException;
import java.util.List;

import svarzee.gps.gpsoauth.AuthToken;

public class ResultLoader extends AsyncTaskLoader<List<Track>> {
    private static final String TAG = ResultLoader.class.getSimpleName();
    private AuthToken authToken;
    private GPlayMusic api;
    private List<Track> tracks;

    public ResultLoader(Context context, String token) {
        super(context);
        if (token != null) {
            authToken = TokenProvider.provideToken(token);
            Log.d(TAG, "GPlayMusic token retrieved: " + authToken);
        }
        else {
            Log.e(TAG, "Token not found");
        }
    }

    @Override
    public List<Track> loadInBackground() {
        if (api == null) {
            api = new GPlayMusic.Builder().setAuthToken(authToken).build();
            Log.d(TAG, "GPlayMusic instantied");
        }
        else
            Log.d(TAG, "Reusing existing GPlayMusic instance");

        List<Track> tracks = null;
        try {
            tracks = api.getPromotedTracks();
        } catch (IOException e) {
            Log.e(TAG, "Loading GPlayMusic results failed due to transient network error", e);
        }
        return tracks;
    }

    @Override
    protected void onStartLoading() {
        try {
            if (tracks != null) {
                deliverResult(tracks);
            }
            if (takeContentChanged() || tracks == null) {
                forceLoad();
            }

            Log.d(TAG, "onStartLoading() ");
        } catch (Exception e) {
            Log.d(TAG, "Failed to start loading", e);
        }
    }
}
