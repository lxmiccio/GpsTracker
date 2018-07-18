package com.gpstracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper mInstance = null;

    // Database Version. Remember to change DATABASE_VERSION when adding or changing tables, otherwise db won't update
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "GpsTracker";

    // Table Names
    private static final String TABLE_TRACK = "tracks";
    private static final String TABLE_COORDINATE = "coordinates";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    private static final String KEY_STARTED_AT = "started_at";
    private static final String KEY_FINISHED_AT = "finished_at";
    private static final String KEY_TRACK_ID = "track_id";

    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_BEARING = "bearing";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPEED = "speed";

    // Coordinates table create statement
    private static final String CREATE_TABLE_TRACK = "CREATE TABLE "
            + TABLE_TRACK + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_STARTED_AT + " DATETIME,"
            + KEY_FINISHED_AT + " DATETIME,"
            + KEY_CREATED_AT + " DATETIME)";

    // Coordinates table create statement
    private static final String CREATE_TABLE_COORDINATE = "CREATE TABLE "
            + TABLE_COORDINATE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_ALTITUDE + " DOUBLE,"
            + KEY_BEARING + " FLOAT,"
            + KEY_LATITUDE + " DOUBLE,"
            + KEY_LONGITUDE + " DOUBLE,"
            + KEY_SPEED + " FLOAT,"
            + KEY_CREATED_AT + " DATETIME,"
            + KEY_TRACK_ID + " INTEGER,"
            + " FOREIGN KEY (" + KEY_TRACK_ID + ") REFERENCES " + TABLE_TRACK + " (" + KEY_ID + "))";

    protected DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance() {
        if(mInstance == null) {
            mInstance = new DatabaseHelper(MainActivity.getContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_TRACK);
        db.execSQL(CREATE_TABLE_COORDINATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE);

        // Create new tables
        onCreate(db);
    }

    /*
     * Create a coordinate
     */
    public long createTrack() {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STARTED_AT, getCurrentDateTime());
        values.put(KEY_FINISHED_AT, getCurrentDateTime());
        values.put(KEY_CREATED_AT, getCurrentDateTime());

        // insert row
        long id = db.insert(TABLE_TRACK, null, values);

        return id;
    }

    public void updateTrack() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_TRACK;
        Cursor cursor = db.rawQuery(selectQuery, null);

        long trackId = -1;
        // looping through all rows and adding to list
        if(cursor.moveToFirst()) {
            do {
                trackId = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            } while (cursor.moveToNext());
        }
        cursor.close();

        db.execSQL("UPDATE " + TABLE_TRACK + " SET " + KEY_FINISHED_AT + " = '" + getCurrentDateTime() + "' WHERE id = " + trackId);
    }

    /*
     * Create a coordinate
     */
    public long createCoordinate(TrackPoint point, long trackId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ALTITUDE, point.getAltitude());
        values.put(KEY_BEARING, point.getBearing());
        values.put(KEY_LATITUDE, point.getLatitude());
        values.put(KEY_LONGITUDE, point.getLongitude());
        values.put(KEY_SPEED, point.getSpeed());
        values.put(KEY_CREATED_AT, getCurrentDateTime());
        values.put(KEY_TRACK_ID, trackId);

        // insert row
        long id = db.insert(TABLE_COORDINATE, null, values);

        return id;
    }

    /*
     * Get all coordinates
     */
    public ArrayList<TrackPoint> getAllCoordinates() {
        ArrayList<TrackPoint> coordinates = new ArrayList();

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
                Date date = getDateTime(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));

                TrackPoint coordinate = new TrackPoint(altitude, bearing, latitude, longitude, speed, date.getTime());
                coordinates.add(coordinate);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return coordinates;
    }

    /*
     * Get all coordinates
     */
    public long getCurrentTrackId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_TRACK;
        Cursor cursor = db.rawQuery(selectQuery, null);

        long trackId = -1;
        // looping through all rows and adding to list
        if(cursor.moveToFirst()) {
            do {
                trackId = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return trackId;
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
     * Delete all coordinates
     */
    public void deleteAllCoordinates() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_COORDINATE);
        db.close();
    }

    /*
     * Get current datetime in String
     */
    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    /*
     * Get current datetime in Date
     */
    private Date getDateTime(String dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return dateFormat.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }
}
