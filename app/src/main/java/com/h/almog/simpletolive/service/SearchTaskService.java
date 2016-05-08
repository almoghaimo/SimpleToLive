package com.h.almog.simpletolive.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.h.almog.simpletolive.R;
import com.h.almog.simpletolive.activities.MainActivity;
import com.h.almog.simpletolive.database.LastPlacesDBHandler;
import com.h.almog.simpletolive.fragments.PlacesListFragment;
import com.h.almog.simpletolive.module.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class SearchTaskService extends IntentService {
    private static final String SEARCH_BY_LOCATION = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%1$s,%2$s&radius=%3$s&key=%4$s";
    private static final String SEARCH_WITH_TXT_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%1$s&location=%2$s,%3$s&radius=%4$s&key=%5$s";
    public static final String NEXT_RESULT_KEY = "nextResultKey";
    private static final String IS_SEARCH_BY_LOCATION = "isSearchByLocation";
    public static final String ADD_PLACES_KEY = "addPlacesKey";
    private static final String NEXT_20 ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=%1$s&key=%2$s";
    public static final int CODE_By_TEXT = 0;
    public static final int CODE_By_LOCATION = 1;
    public static final int ADD_MORE_RESULT = 2;
    public static final String ACTION_SEARCH = "com.h.almog.simpletolive.ACTION_SEARCH";
    public static final String PLACES_KEY = "placesKeyIntent";
    public static final String SEARCH_BY_KEY = "searchKeyIntent";

    //    private OnPlaceTaskResult listener;
    private boolean isSearchByLocation = false;
    private  ArrayList<Place> places = null;
    //    private Handler handler;
    private LatLng myCoordinate;
//    private  SharedPreferences sp;

    private final IBinder mBinder = new LocalBinder();
    private Activity activity;
    private OnDismissDialogSearch callbacksDialog;


    public SearchTaskService() {
        super("SearchTaskService");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    //returns the instance of the service
    public class LocalBinder extends Binder {
        public SearchTaskService getServiceInstance(){
            return SearchTaskService.this;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String query = intent.getStringExtra(MainActivity.QUERY_KEY);
        if (query != null){
            if (query.equals("")){
                isSearchByLocation = true;
            }else {
                isSearchByLocation = false;
            }
        }else {
            isSearchByLocation = false;
        }


        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        boolean nextTwentyResults = intent.getBooleanExtra(PlacesListFragment.MORE_RESULT_KEY_INTENT, false);
        String latitude = sp.getString(MainActivity.LAT_KEY, null);
        String longitude = sp.getString(MainActivity.LONGITUDE_KEY, null);
        double mLatitudeDouble, mLongitudeDouble;
        try {
            URL url = null;
            String API_GOOGLE_SEARCH_KEY = "AIzaSyAVHdXLunth3pJSQCYVBNmrwa2Pc3CTlyE";
            if (nextTwentyResults){
                String nextTwenty = sp.getString(NEXT_RESULT_KEY, null);
                if (nextTwenty != null){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.more_places), Toast.LENGTH_SHORT).show();
                        }
                    });
                    url = new URL(String.format(NEXT_20, nextTwenty, API_GOOGLE_SEARCH_KEY));
                    if (latitude != null || longitude != null) {
                        try {
                            mLatitudeDouble = Double.parseDouble(latitude);
                            mLongitudeDouble = Double.parseDouble(longitude);
                            myCoordinate = new LatLng(mLatitudeDouble, mLongitudeDouble);
                        } catch (NumberFormatException e) {
                            e.getMessage();
                        }
                    }
                }else{
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_more_places), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }else {


                if (latitude != null || longitude != null) {
                    try {
                        mLatitudeDouble = Double.parseDouble(latitude);
                        mLongitudeDouble = Double.parseDouble(longitude);
                        myCoordinate = new LatLng(mLatitudeDouble, mLongitudeDouble);
                    } catch (NumberFormatException e) {
                        e.getMessage();
                    }
                    int radius = sp.getInt(getResources().getString(R.string.seekBarKey), -1);
                    if (radius == -1){
                        radius = 2000;
                        sp.edit().putInt(getResources().getString(R.string.seekBarKey), 2000);
                    }
                    if (radius == 0 ){
                        radius = 1;
                    }
                    if (query.equals("")) {

                        url = new URL(String.format(SEARCH_BY_LOCATION, latitude, longitude, radius , API_GOOGLE_SEARCH_KEY));
                        isSearchByLocation = true;
                        sp.edit().putBoolean(IS_SEARCH_BY_LOCATION, true).apply();
                    } else {
                        sp.edit().putBoolean(IS_SEARCH_BY_LOCATION, false).apply();
                        url = new URL(String.format(SEARCH_WITH_TXT_URL, query, latitude, longitude, radius, API_GOOGLE_SEARCH_KEY));
                    }
                } else {
                    return;
                }

            }
            if (url == null){
                return;
            }
            connection = (HttpsURLConnection) url.openConnection();
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
            try {
                places = new ArrayList<>();
                JSONObject obj = new JSONObject(builder.toString());
                try {
                    String next20 = obj.getString("next_page_token");
                    sp.edit().putString(NEXT_RESULT_KEY, next20).apply();
                }catch (JSONException e) {
                    sp.edit().remove(SearchTaskService.NEXT_RESULT_KEY).apply();
                    e.printStackTrace();
                }
                JSONArray results = obj.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject place = results.getJSONObject(i);
                    String id = place.getString("place_id");
                    String name = place.getString("name");
                    String address = "";
                    String imgUrl = "";
                    LatLng coordsPlace = null;
                    JSONObject geometry = place.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    coordsPlace = new LatLng(lat, lng);
                    try {
                        if (nextTwentyResults){
                            address = place.getString("vicinity");
                            JSONArray photos = place.getJSONArray("photos");
                            if (sp.getBoolean(IS_SEARCH_BY_LOCATION, true)){
                                imgUrl = place.getString("icon");
                            }else{
                                JSONObject photo = photos.getJSONObject(0);
                                imgUrl = photo.getString("photo_reference");
                            }
                        }else{

                            if (isSearchByLocation){
                                address = place.getString("vicinity");
                                imgUrl = place.getString("icon");
                            }else{
                                address = place.getString("formatted_address");
                                JSONArray photos = place.getJSONArray("photos");

                                JSONObject photo = photos.getJSONObject(0);
                                imgUrl = photo.getString("photo_reference");
                            }
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    double distance = -1;
                    if (coordsPlace != null && myCoordinate != null){
                        distance = CalculationByDistance(coordsPlace, myCoordinate);
                    }

                    places.add(new Place(id, name, address, imgUrl, coordsPlace, distance));
                }
                LastPlacesDBHandler handler = new LastPlacesDBHandler(this);
                handler.deleteAllPlaces();
                for (int i = 0; i < places.size(); i++) {
                    handler.insertOnePlace(places.get(i));
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null){
                connection.disconnect();
            }if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Intent in = new Intent(ACTION_SEARCH);
        in.putExtra(PLACES_KEY, places);
        if (nextTwentyResults){
            if (sp.getBoolean(IS_SEARCH_BY_LOCATION, true)) {
                in.putExtra(SEARCH_BY_KEY, CODE_By_LOCATION);
            }else{
                in.putExtra(SEARCH_BY_KEY, CODE_By_TEXT);
            }
            in.putExtra(ADD_PLACES_KEY, true);
        }else{
            if (isSearchByLocation) {
                in.putExtra(SEARCH_BY_KEY, CODE_By_LOCATION);
            }else{
                in.putExtra(SEARCH_BY_KEY, CODE_By_TEXT);
            }
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        if (callbacksDialog != null)
            callbacksDialog.dismissDialogSearch();

    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.callbacksDialog = (OnDismissDialogSearch) activity;
        this.activity = activity;
    }


    // ==> the distance between two coordinate

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;  // radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        return Radius * c;
    }


    public interface OnDismissDialogSearch {
        public void dismissDialogSearch();
    }


}



