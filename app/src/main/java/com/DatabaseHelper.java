package com;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gpstracker.TrackPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "GpsTracker";

    // Table Names
    private static final String TABLE_COORDINATE = "coordinates";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_BEARING = "bearing";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_CREATED_AT = "created_at";

    // Coordinates table create statement
    private static final String CREATE_TABLE_COORDINATE = "CREATE TABLE "
            + TABLE_COORDINATE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_ALTITUDE + " DOUBLE,"
            + KEY_BEARING + " FLOAT,"
            + KEY_LATITUDE + " DOUBLE,"
            + KEY_LONGITUDE + " DOUBLE,"
            + KEY_SPEED + " FLOAT,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_COORDINATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE);

        // Create new tables
        onCreate(db);
    }

    /*
     * Create a coordinate
     */
    public long createCoordinate(TrackPoint point) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ALTITUDE, point.getAltitude());
        values.put(KEY_BEARING, point.getBearing());
        values.put(KEY_LATITUDE, point.getLatitude());
        values.put(KEY_LONGITUDE, point.getLongitude());
        values.put(KEY_SPEED, point.getSpeed());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long id = db.insert(TABLE_COORDINATE, null, values);

        return id;
    }

    /*
     * Get all coordinates
     */
    public ArrayList<TrackPoint> getAllCoordinates() {
        ArrayList<TrackPoint> coordinates = new ArrayList<TrackPoint>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_COORDINATE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(cursor.moveToFirst()) {
            do {
                double altitude = cursor.getDouble(cursor.getColumnIndex(KEY_ALTITUDE));
                float bearing = cursor.getFloat(cursor.getColumnIndex(KEY_BEARING));
                double latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
                float speed = cursor.getFloat(cursor.getColumnIndex(KEY_SPEED));
//                Date time = new Date(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));

                TrackPoint coordinate = new TrackPoint(altitude, bearing, latitude, longitude, speed, 2/*time.getTime()*/);
                coordinates.add(coordinate);

                Log.d("DB", "Latitude is " + latitude + ", longitude is " + longitude);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return coordinates;
    }

    /*
     * Get coordinates count
     */
    public int getCoordinatesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_COORDINATE;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /*
     * Get current datetime
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
