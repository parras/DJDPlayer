package nu.staldal.djdplayer.gmusic;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.preference.PreferenceManager;

import com.github.felixgail.gplaymusic.model.Track;

import java.util.ArrayList;
import java.util.List;

import nu.staldal.djdplayer.R;

import static nu.staldal.djdplayer.SettingsActivity.GOOGLE_ACCOUNT_NAME;
import static nu.staldal.djdplayer.SettingsActivity.GOOGLE_MUSIC_TOKEN;

public class GoogleMusicFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Track>> {
    private static final String TAG = GoogleMusicFragment.class.getSimpleName();
    private ArrayAdapter<Track> adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(getActivity());
        listView.setId(android.R.id.list);
        listView.setFastScrollEnabled(true);
        listView.setTextFilterEnabled(true);

        registerForContextMenu(listView);

        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ArrayAdapter<Track>(getActivity(), R.layout.track_list_item);
        setListAdapter(adapter);
        Log.d(TAG, "Created ArrayAdapter for results");

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<List<Track>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Creating loader");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = sharedPref.getString(GOOGLE_MUSIC_TOKEN, null);
        if (token != null)
            return new ResultLoader(getActivity(), token);
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Track>> loader, List<Track> data) {
        adapter.addAll(data);
        Log.d(TAG, "Added results to adapter");
    }

    @Override
    public void onLoaderReset(Loader<List<Track>> loader) {

    }
}
