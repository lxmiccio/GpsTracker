package com.gpstracker;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class SessionListFragment extends Fragment {

    private SessionAdapter mSessionAdapter;
    private ListView mListView;

    private Track mTrack;
    private DatabaseHelper mDb;

    private TrackFragment mTrackFragment;

    public SessionListFragment() {
        // Required empty public constructor
        mDb = DatabaseHelper.getInstance();
    }

    public static SessionListFragment getInstance() {
        SessionListFragment fragment = new SessionListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_session_list, container, false);

        ArrayList<Session> sessions = mDb.getSessionsByTrack(mTrack.getId());
        mSessionAdapter = new SessionAdapter(sessions, MainActivity.getContext());
        mListView = view.findViewById(R.id.session_list);
        mListView.setAdapter(mSessionAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<Session> sessions = mSessionAdapter.getSessions();
                Session session = sessions.get(position);

                mTrackFragment = TrackFragment.getInstance();
                //mTrackFragment.setTrack(track);

                // Add fragment to BackStack so that when the back button is pressed, the inner fragment is removed
                FragmentActivity fragmentActivity = (FragmentActivity) getActivity();
                fragmentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mTrackFragment)
                        .addToBackStack("TrackFragment").commit();
            }
        });

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
                        SparseBooleanArray selected = mSessionAdapter.getSelectedIds();

                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                Session selectedItem = mSessionAdapter.getItem(selected.keyAt(i));

                                // Remove selected items following the ids
                                mSessionAdapter.remove(selectedItem);
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
                mSessionAdapter.removeSelection();
            }

            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {

                // Capture total checked items
                final int checkedCount = mListView.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                mSessionAdapter.toggleSelection(position);
            }
        });

        return view;
    }

    public void setTrack(Track track) {
        mTrack = track;
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
