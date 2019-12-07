package com.gpstracker;

import android.content.Context;
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

import java.util.ArrayList;

public class TrackListFragment extends Fragment {

    public final static String TAG = "TrackListFragment";

    private TrackAdapter mTrackAdapter;
    private ListView mListView;

    private DatabaseHelper mDb;

    private SessionListFragment mSessionListFragment;

    public TrackListFragment() {
        // Required empty public constructor
    }

    public static TrackListFragment getInstance() {
        TrackListFragment fragment = new TrackListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.track_list_fragment, container, false);

        ArrayList<Track> tracks = mDb.getAllTracks();
        mTrackAdapter = new TrackAdapter(tracks, MainActivity.getContext());
        mListView = view.findViewById(R.id.track_list);
        mListView.setAdapter(mTrackAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<Track> tracks = mTrackAdapter.getTracks();
                Track track = tracks.get(position);

                mSessionListFragment = SessionListFragment.getInstance();
                mSessionListFragment.setTrack(track);

                FragmentManager supportFragmentManager = getFragmentManager();
                FragmentTransaction supportFragmentTransaction = supportFragmentManager.beginTransaction();

                supportFragmentTransaction.replace(R.id.fragment_container, mSessionListFragment)
                        .addToBackStack(SessionListFragment.TAG)
                        .commit();
            }
        });
        //mListView.setBackgroundResource(R.drawable.list_selected_item);

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
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
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = mTrackAdapter.getSelectedIds();

                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                Track selectedItem = mTrackAdapter.getItem(selected.keyAt(i));

                                // Remove selected items following the ids
                                mTrackAdapter.remove(selectedItem);
                            }
                        }
                        // Close CAB
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

                // Capture total checked items
                final int checkedCount = mListView.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                mTrackAdapter.toggleSelection(position);
            }
        });

        return view;
    }

    public void refresh() {
        ArrayList<Track> tracks = mDb.getAllTracks();
        mTrackAdapter = new TrackAdapter(tracks, MainActivity.getContext());
        mListView.setAdapter(mTrackAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
