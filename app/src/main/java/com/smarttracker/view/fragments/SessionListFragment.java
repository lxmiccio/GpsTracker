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
import android.widget.TextView;

import com.smarttracker.R;
import com.smarttracker.model.Session;
import com.smarttracker.model.SessionAdapter;
import com.smarttracker.model.Track;
import com.smarttracker.model.db.DatabaseHelper;
import com.smarttracker.view.activities.MainActivity;

import java.util.ArrayList;

public class SessionListFragment extends Fragment {

    public final static String TAG = "SessionListFragment";

    private TextView mTrackName;

    private SessionAdapter mSessionAdapter;
    private ListView mListView;

    private Track mTrack;
    private DatabaseHelper mDb;

    private RacingFragment mRacingFragment;

    public SessionListFragment() {
        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.session_list_fragment, container, false);

        mTrackName = view.findViewById(R.id.name);
        mTrackName.setText(mTrack.getName());

        ArrayList<Session> sessions = mDb.getSessionsByTrackId(mTrack.getId());
        mSessionAdapter = new SessionAdapter(sessions, MainActivity.getContext());

        mListView = view.findViewById(R.id.session_list);
        mListView.setAdapter(mSessionAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemClickListener(mItemClickListener);
        mListView.setMultiChoiceModeListener(mMultiChoiceModeListener);

        return view;
    }

    public void setTrack(Track track) {
        mTrack = track;
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ArrayList<Session> sessions = mSessionAdapter.getSessions();
            Session session = sessions.get(position);

            mRacingFragment = RacingFragment.getInstance();
            mRacingFragment.setReferenceSessionTrack(mTrack);
            mRacingFragment.setReferenceSession(session);

            FragmentManager supportFragmentManager = getFragmentManager();
            FragmentTransaction supportFragmentTransaction = supportFragmentManager.beginTransaction();

            // Add fragment to BackStack so that when the back button is pressed, the inner fragment is removed
            supportFragmentTransaction.replace(R.id.fragment_container, mRacingFragment, RacingFragment.TAG)
                    .addToBackStack(RacingFragment.TAG)
                    .commit();
        }
    };

    private AbsListView.MultiChoiceModeListener mMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
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
                    // Get the selected Sessions
                    SparseBooleanArray selected = mSessionAdapter.getSelectedSessions();

                    // Looping through all the selected Session
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            Session selectedItem = mSessionAdapter.getItem(selected.keyAt(i));

                            // Remove the selected Session
                            mSessionAdapter.remove(selectedItem);
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
            mSessionAdapter.removeSelection();
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Get the number of selected Sessions
            final int checkedCount = mListView.getCheckedItemCount();

            // Set the title according to the number of selected Sessions
            String text = " " + (checkedCount == 1 ? getString(R.string.selected_element) : getString(R.string.selected_elements));
            mode.setTitle(checkedCount + text);

            // Call toggleSelection method from ListViewAdapter Class
            mSessionAdapter.toggleSelection(position);
        }
    };
}
