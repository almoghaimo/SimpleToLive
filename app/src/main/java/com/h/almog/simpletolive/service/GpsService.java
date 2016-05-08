package com.h.almog.simpletolive.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.h.almog.simpletolive.R;
import com.h.almog.simpletolive.activities.MainActivity;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GpsService extends IntentService implements
        LocationListener{
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    // Location
    private Timer gpsTimer;
    private Timer networkTimer;
    private boolean gotLocation = false; // the boolean will be true if location founded.
    private LocationManager locationManager;
    private static final long MIN_DISTANCE_FOR_UPDATE =300; //300 meters;
    private static final long MIN_TIME_FOR_UPDATE = 1000; // 30 sec;
    private String providerName;
    private boolean tryAgain = true; // if try Again is false the app will enter to offline mode.
    private boolean offlineMode = false;
    public GpsService() {
        super("GpsService");
    }

    public static final String LOCATION_FOUND_KEY = "locationFoundKey"; //
    public static final String LAT_KEY = "latKey";
    public static final String LONGITUDE_KEY = "longitudeKey";


    private final IBinder mBinder = new LocalBinder();
    private Activity activity;
    private OnSetMyLocation callbacksMyLocation;
    private OnSetOfflineMode callbacksOfflineMode;
    private OnDismissDialogLocation callbacksDismissDialog;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    //returns the instance of the service
    public class LocalBinder extends Binder {
        public GpsService getServiceInstance(){
            return GpsService.this;
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!gotLocation && tryAgain) {
            providerName = LocationManager.GPS_PROVIDER;
            try {
                locationManager.requestLocationUpdates(providerName, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
            }catch (SecurityException ex) {

            }
            // create timer object
            gpsTimer = new Timer("gpsProvider");
            // create TimerTask implementation - it will run on a new thread!
            TimerTask gpsTask = new TimerTask() {
                @Override
                public void run() {

                    // if we do not have a location yet
                    try {
                        // remove old location provider(gps)
                        locationManager.removeUpdates(GpsService.this);
                        providerName = LocationManager.NETWORK_PROVIDER;
                        // start listening to location again on the main thread
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    locationManager.requestLocationUpdates(providerName, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, GpsService.this);
                                } catch (SecurityException e) {
                                    Log.e("Location security", e.getMessage());
                                }
                            }
                        });
                    } catch (SecurityException e) {
                        Log.e("Location security", e.getMessage());
                    }

                }

                ;
            };
            // schedule the timer to run the task after 5 seconds from now
            gpsTimer.schedule(gpsTask, new Date(System.currentTimeMillis() + 5000));

            networkTimer = new Timer("networkProvider");
            TimerTask networkTask = new TimerTask() {
                @Override
                public void run() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (!gotLocation) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                                // Setting Dialog Title
                                alertDialog.setTitle("GPS settings");
                                alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
                                // On pressing Settings button
                                alertDialog.setPositiveButton(("Setting"), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        activity.startActivity(intent);
                                    }
                                });
                                // on pressing cancel button
                                alertDialog.setNegativeButton(("Cancel"), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        tryFindLocationAgain();
                                    }
                                });
                                alertDialog.setCancelable(false);
                                // Showing Alert Message

                                alertDialog.show();
                            }
                        }
                    });
                }
            };
            networkTimer.schedule(networkTask, new Date(System.currentTimeMillis() + 10000));

        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.callbacksMyLocation = (OnSetMyLocation) activity;
        this.callbacksOfflineMode = (OnSetOfflineMode) activity;
        this.callbacksDismissDialog = (OnDismissDialogLocation) activity;
        this.activity = activity;
    }


    @Override
    public void onLocationChanged(Location location) {
        // we got a new location
        gotLocation = true;
        // cancel the timers
        gpsTimer.cancel();
        networkTimer.cancel();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(LAT_KEY, location.getLatitude() + "");
        edit.putString(LONGITUDE_KEY, location.getLongitude() + "");
        edit.putBoolean(LOCATION_FOUND_KEY, true);
        edit.putBoolean(MainActivity.OFFLINE_MODE_KEY, false);
        edit.apply();


        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Toast.makeText(this, getResources().getString(R.string.location_found), Toast.LENGTH_SHORT).show();

        //SHOW EVERYTHING AROUND YOU WHEN THE APP FOUND YOUR LOCATION

        if (callbacksMyLocation != null){
            callbacksMyLocation.setMyLocation(myLatLng);
//            callbacksMyLocation.startSearchAround();
        }
        if (this.callbacksDismissDialog != null){
            this.callbacksDismissDialog.dismissDialogLocation();
        }

    }




    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    // ==> THE METHOD ASKING YOU IF TRY FIND LOCATION AGAIN
    private void tryFindLocationAgain() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Title
        alertDialog.setTitle(getResources().getString(R.string.location_again_title));
        // Setting Dialog Message
        alertDialog.setMessage(getResources().getString(R.string.location_again_meesage));
        // On pressing Settings button
        alertDialog.setPositiveButton(getResources().getString(R.string.location_again_positive_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent in = new Intent();
                onHandleIntent(in);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton(getResources().getString(R.string.location_again_negative_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                gpsTimer.cancel();
                networkTimer.cancel();
                offlineMode = true;
                callbacksOfflineMode.setOfflineMode();
            }
        });
        // Showing Alert Message
        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    public interface OnSetOfflineMode {
        void setOfflineMode();
    }

    public interface OnDismissDialogLocation {
        void dismissDialogLocation();
    }

    public interface OnSetMyLocation {
        void setMyLocation(LatLng myLocation);
        void startSearchAround();
    }

}
