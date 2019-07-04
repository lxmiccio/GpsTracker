package com.gpstracker;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TrackAdapter extends ArrayAdapter<Track> {

    private ArrayList<Track> mDataSet;
    private SparseBooleanArray mSelectedItems;

    private DatabaseHelper mDb;

    public TrackAdapter(ArrayList<Track> data, Context context) {
        super(context, R.layout.track_row, data);

        mDataSet = data;
        mSelectedItems = new SparseBooleanArray();

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
            viewHolder.length = convertView.findViewById(R.id.length);
            viewHolder.started_at = convertView.findViewById(R.id.started_at);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Track track = getItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        viewHolder.name.setText(track.getName());
        viewHolder.length.setText(String.valueOf(track.getLength()) + " m");
        viewHolder.started_at.setText(dateFormat.format(track.getStartingDate()));

        return convertView;
    }

    @Override
    public void remove(Track track) {
        mDataSet.remove(track);
        notifyDataSetChanged();

        mDb.deleteTrack(track.getId());
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItems.get(position));
    }

    public void removeSelection() {
        mSelectedItems = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItems.put(position, value);
        else
            mSelectedItems.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItems.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItems;
    }

    public ArrayList<Track> getTracks() {
        return mDataSet;
    }

    private class ViewHolder {
        public TextView name;
        public TextView length;
        public TextView started_at;
    }
}
