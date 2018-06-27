package com.gpstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CoordinateAdapter extends ArrayAdapter<TrackPoint> {

    private ArrayList<TrackPoint> mDataSet;
    Context mContext;

    private class ViewHolder {
        public TextView latitude;
        public TextView longitude;
    }

    public CoordinateAdapter(ArrayList<TrackPoint> data, Context context) {
        super(context, R.layout.coordinate_row, data);
        this.mDataSet = data;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.coordinate_row, null);

            viewHolder = new ViewHolder();
            viewHolder.latitude = convertView.findViewById(R.id.latitude);
            viewHolder.longitude = convertView.findViewById(R.id.longitude);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TrackPoint point = getItem(position);
        viewHolder.latitude.setText(String.valueOf(point.getLatitude()));
        viewHolder.longitude.setText(String.valueOf(point.getLongitude()));
        return convertView;
    }
}
