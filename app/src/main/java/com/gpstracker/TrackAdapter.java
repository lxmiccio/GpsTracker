package com.gpstracker;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackAdapter extends ArrayAdapter<Track> {

    private ArrayList<Track> mDataSet;
    private Context mContext;

    private SparseBooleanArray mSelectedItemsIds;

    private class ViewHolder {
        public TextView started_at;
        public TextView finished_at;
    }

    public TrackAdapter(ArrayList<Track> data, Context context) {
            super(context, R.layout.track_row, data);

        mDataSet = data;
        mContext = context;

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
        viewHolder.started_at.setText(track.getStartingDate().toString());
        viewHolder.finished_at.setText(track.getEndingDate().toString());
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
}
