package com.gpstracker;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class GpsSimulator extends Application {
    private GpsSimulatorThread mRunnable;
    private Thread mThread;

    public GpsSimulator() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void startSimulation(Context context) {
        mRunnable = new GpsSimulatorThread(context);
        mThread = new Thread(mRunnable);
        mThread.start();
    }

    public void stopSimulation() {
        mRunnable.setFinished(true);
        mThread.interrupt();
    }
}

class GpsSimulatorThread implements Runnable {
    private volatile boolean mFinished;
    private Context mContext;

    public GpsSimulatorThread(Context context) {
        mFinished = false;
        mContext = context;
    }

    public void setFinished(boolean finished) {
        mFinished = finished;
    }

    @Override
    public void run() {
        double i = 20.0000;
        while (!Thread.currentThread().isInterrupted() && !mFinished) {
            Location location = new Location("Simulator");
            location.setLatitude(i);
            location.setLongitude(i);
            i += 0.01;
            location.setAltitude(500.0000);

            Bundle bundle = new Bundle();
            bundle.putParcelable("location", location);

            Intent intent = new Intent("GPSSimulatorLocation");
            intent.putExtra("Location", bundle);

                LocalBroadcastManager.getInstance(MainActivity.getContext()).sendBroadcast(intent);

                try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }
};