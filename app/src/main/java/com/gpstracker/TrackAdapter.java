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
    private SparseBooleanArray mSelectedItemsIds;

    public TrackAdapter(ArrayList<Track> data, Context context) {
        super(context, R.layout.track_row, data);

        mDataSet = data;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.track_row, null);

            viewHolder = new ViewHolder();
            viewHolder.started_at = convertView.findViewById(R.id.started_at);
            viewHolder.finished_at = convertView.findViewById(R.id.finished_at);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Track track = getItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        viewHolder.started_at.setText(dateFormat.format(track.getStartingDate()));
        viewHolder.finished_at.setText(dateFormat.format(track.getEndingDate()));

        return convertView;
    }

    @Override
    public void remove(Track track) {
        mDataSet.remove(track);
        notifyDataSetChanged();

        DatabaseHelper.getInstance().deleteTrack(track.getId());
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    private class ViewHolder {
        public TextView started_at;
        public TextView finished_at;
    }
}
