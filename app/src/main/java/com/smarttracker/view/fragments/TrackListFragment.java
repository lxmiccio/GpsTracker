package com.smarttracker.view.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.smarttracker.R;
import com.smarttracker.model.Track;
import com.smarttracker.model.TrackAdapter;
import com.smarttracker.model.db.DatabaseHelper;
import com.smarttracker.view.activities.MainActivity;

import java.util.ArrayList;

public class TrackListFragment extends Fragment {

    public final static String TAG = "TrackListFragment";

    private TrackAdapter mTrackAdapter;
    private ListView mListView;

    private SessionListFragment mSessionListFragment;

    private DatabaseHelper mDb;

    public TrackListFragment() {
        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.track_list_fragment, container, false);

        ArrayList<Track> tracks = mDb.getAllTracks();
        mTrackAdapter = new TrackAdapter(tracks, MainActivity.getContext());

        mListView = view.findViewById(R.id.track_list);
        mListView.setAdapter(mTrackAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemClickListener(mItemClickListener);
        mListView.setMultiChoiceModeListener(mMultiChoiceModeListener);

        return view;
    }

    protected AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ArrayList<Track> tracks = mTrackAdapter.getTracks();
            Track track = tracks.get(position);

            mSessionListFragment = new SessionListFragment();
            mSessionListFragment.setTrack(track);

            FragmentManager supportFragmentManager = getFragmentManager();
            FragmentTransaction supportFragmentTransaction = supportFragmentManager.beginTransaction();

            // Add fragment to BackStack so that when the back button is pressed, the inner fragment is removed
            supportFragmentTransaction.replace(R.id.fragment_container, mSessionListFragment)
                    .addToBackStack(SessionListFragment.TAG)
                    .commit();
        }
    };

    protected AbsListView.MultiChoiceModeListener mMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.list_multiselection_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    // Get the selected Tracks
                    SparseBooleanArray selected = mTrackAdapter.getSelectedTracks();

                    // Looping through all the selected Tracks
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            Track selectedItem = mTrackAdapter.getItem(selected.keyAt(i));

                            // Remove the selected Track
                            mTrackAdapter.remove(selectedItem);
                        }
                    }

                    // Close the menu
                    mode.finish();

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            mTrackAdapter.removeSelection();
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Get the number of selected Tracks
            final int checkedCount = mListView.getCheckedItemCount();

            // Set the title according to the number of selected Tracks
            String text = " " + (checkedCount == 1 ? getString(R.string.selected_element) : getString(R.string.selected_elements));
            mode.setTitle(checkedCount + text);

            // Call toggleSelection method from ListViewAdapter Class
            mTrackAdapter.toggleSelection(position);
        }
    };
}
