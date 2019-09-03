package com.gpstracker;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SessionAdapter extends ArrayAdapter<Session> {

    private ArrayList<Session> mDataSet;
    private SparseBooleanArray mSelectedItems;

    private DatabaseHelper mDb;

    public SessionAdapter(ArrayList<Session> data, Context context) {
        super(context, R.layout.session_row, data);

        mDataSet = data;
        mSelectedItems = new SparseBooleanArray();

        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.session_row, null);

            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.length = convertView.findViewById(R.id.length);
            viewHolder.time = convertView.findViewById(R.id.time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Session session = getItem(position);

        viewHolder.name.setText(session.getName());
        viewHolder.length.setText(String.valueOf(session.getLength()) + " m");

        String minutes = String.valueOf(session.getDuration() / 60);
        minutes = String.format("%1$" + 2 + "s", minutes).replace(' ', '0');
        String seconds = String.valueOf(session.getDuration() % 60);
        seconds = String.format("%1$" + 2 + "s", seconds).replace(' ', '0');
        viewHolder.time.setText(minutes + ":" + seconds);

        return convertView;
    }

    @Override
    public void remove(Session session) {
        mDataSet.remove(session);
        notifyDataSetChanged();

        mDb.deleteSession(session.getId());
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

    public ArrayList<Session> getSessions() {
        return mDataSet;
    }

    private class ViewHolder {
        public TextView name;
        public TextView length;
        public TextView time;
    }
}