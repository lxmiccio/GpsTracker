package com.smarttracker.model;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smarttracker.R;
import com.smarttracker.model.db.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TrackAdapter extends ArrayAdapter<Track> {

    private ArrayList<Track> mTracks;
    private SparseBooleanArray mSelectedTracks;

    private DatabaseHelper mDb;

    public TrackAdapter(ArrayList<Track> data, Context context) {
        super(context, R.layout.track_row, data);

        mTracks = data;
        mSelectedTracks = new SparseBooleanArray();

        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.track_row, null);

            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.created_at = convertView.findViewById(R.id.created_at);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Track track = getItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault());
        viewHolder.name.setText(track.getName());
        viewHolder.created_at.setText(dateFormat.format(track.getCreatedAt()));

        return convertView;
    }

    @Override
    public void remove(Track track) {
        mTracks.remove(track);
        notifyDataSetChanged();

        mDb.deleteTrack(track.getId());
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedTracks.get(position));
    }

    public void removeSelection() {
        mSelectedTracks = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedTracks.put(position, value);
        } else {
            mSelectedTracks.delete(position);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Track> getTracks() {
        return mTracks;
    }

    public SparseBooleanArray getSelectedTracks() {
        return mSelectedTracks;
    }

    private class ViewHolder {
        public TextView name;
        public TextView created_at;
    }
}
