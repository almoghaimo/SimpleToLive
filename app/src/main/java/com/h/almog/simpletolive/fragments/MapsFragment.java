package com.h.almog.simpletolive.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.h.almog.simpletolive.asynctask.GetDirectionsTask;
import com.h.almog.simpletolive.asynctask.GetInfoByPlaceIdTask;
import com.h.almog.simpletolive.R;
import com.h.almog.simpletolive.activities.MainActivity;
import com.h.almog.simpletolive.database.FavoritesDBHandler;
import com.h.almog.simpletolive.module.Place;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLoadedCallback,
        GetDirectionsTask.OnGetPolyline, GetInfoByPlaceIdTask.OnGetJsonResponse, View.OnClickListener {
    public static final String KEY_LOCATION_INTENT_PLACE = "keyLocationIntent";
    public static final String ACTION_ADD_LOCATION = "com.h.almog.simpletolive.ACTION_ADD_TO_LOCATION";
    private static final float MAP_ZOOM = 15;
    public static final String KEY_LOCATION_COORDS = "keyLocationCoords";
    private static final String MYLOCATION_KEY = "myLocationKey";
    public static final String REMOVE_FROM_FAV = "removeFromFav";
    private GoogleMap gMap;
    private LocationReceiver locationReceiver;
    private LatLng myLocation;
    private Polyline mPoly;
    private SupportMapFragment mGoogleMap;
    private boolean makeNewMap = false;
    private FragmentActivity fragmentActivity;
    private CoordinatorLayout coordinatorLayout;


    private ImageView placeIcon;
    private TextView txtName, txtAddress, txtDistance, txtPhoneNumber, txtRating;

    private  LinearLayout layoutScrollView;
    private Context context;

    //buttons

    private Button web, navigate, star;
    private String mWebsite;

    private FavoritesDBHandler favoritesDBHandler;
    private boolean favorite = false;
    private Place place;

    private ArrayList<Bitmap> currentPhotos;

    // saved instance state key
    private static final String CURRENT_PHOTOS_KEY = "currentPhotosKey"; // current photos.
    private static final String SAVE_TITLE_KEY = "titleKey";
    private static final String SAVE_RATING_KEY = "ratingKey";
    private static final String SAVE_DISTANCE_KEY = "distanceKey";
    private static final String SAVE_PHONE_KEY = "phoneKey";
    private static final String SAVE_ADDRESS_KEY = "addressKey";
    private static final String SAVE_LAST_PLACE_KEY = "lastPlacekey";
    private boolean landScapeMOde = false;

    private Marker myLocationMarker;
    private Marker lastMarker;
    private static final String SAVE_FAV_KEY = "saveKey";
    private static final String SAVE_WEB_KEY = "webKey";

    public MapsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        makeNewMap = false;

        placeIcon = (ImageView) v.findViewById(R.id.iconPlaceMapFrag);
        txtName = (TextView) v.findViewById(R.id.txtPlaceNameMap);
        txtAddress = (TextView) v.findViewById(R.id.txtAddressMap);
        txtDistance = (TextView) v.findViewById(R.id.txtDistanceMap);
        txtPhoneNumber = (TextView) v.findViewById(R.id.txtPhoneNumber);
        txtRating = (TextView) v.findViewById(R.id.txtRating);

        layoutScrollView = (LinearLayout) v.findViewById(R.id.linearScrollView);
        coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id
                .coordinatorLayout);

        web = (Button) v.findViewById(R.id.btnWeb);
        navigate = (Button) v.findViewById(R.id.btnNavigate);
        star = (Button) v.findViewById(R.id.btnFavorite);

        web.setOnClickListener(this);
        star.setOnClickListener(this);

        currentPhotos = new ArrayList<>();
        if (savedInstanceState == null) {
            mGoogleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFrag));
            mGoogleMap.getMapAsync(this);
        } else {
            if (mGoogleMap == null)
                mGoogleMap = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFrag);


            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isTablet = sp.getBoolean(MainActivity.IS_TABLET, false);
            if (isTablet) {
                mGoogleMap.getMapAsync(this);
                myLocation = savedInstanceState.getParcelable(MYLOCATION_KEY);
            }
            if (landScapeMOde)
                landScapeMOde = false;
            else
                landScapeMOde = true;
            ArrayList<Bitmap> currentPhotosNow = savedInstanceState.getParcelableArrayList(CURRENT_PHOTOS_KEY);
            for (int i = 0; i < currentPhotosNow.size(); i++) {
                putBitmap(currentPhotosNow.get(i), i);
            }
            txtName.setText(savedInstanceState.getString(SAVE_TITLE_KEY));
            txtRating.setText(savedInstanceState.getString(SAVE_RATING_KEY));
            txtDistance.setText(savedInstanceState.getString(SAVE_DISTANCE_KEY));
            txtAddress.setText(savedInstanceState.getString(SAVE_ADDRESS_KEY));
            txtPhoneNumber.setText(savedInstanceState.getString(SAVE_PHONE_KEY));
            this.place = savedInstanceState.getParcelable(SAVE_LAST_PLACE_KEY);
            this.mWebsite = savedInstanceState.getString(SAVE_WEB_KEY);
            this.favorite = savedInstanceState.getBoolean(SAVE_FAV_KEY);
        }
        mGoogleMap.setRetainInstance(true);

        locationReceiver = new LocationReceiver();
        IntentFilter inLocFilter = new IntentFilter(ACTION_ADD_LOCATION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationReceiver, inLocFilter);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentActivity = (FragmentActivity) context;
        this.context = context;
    }
    // ==>  Google map initialize.

    @Override
    public void onMapReady(GoogleMap mMap) {
        gMap = mMap;
        gMap.setOnMarkerClickListener(this);
    }


    public void setLocation(final Place place) {
        this.place = place;
        star.setBackgroundResource(R.drawable.star_empty);
        favorite = false;

        GetInfoByPlaceIdTask getInfoByPlaceIdTask = new GetInfoByPlaceIdTask(this);
        getInfoByPlaceIdTask.execute(place.getId());
        LatLng coordinate = place.getCoords();
        if (gMap != null) {
            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (this.lastMarker != null){
                this.lastMarker.remove();
            }
            this.lastMarker = gMap.addMarker(new MarkerOptions().position(coordinate).title(place.getName()));
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, MAP_ZOOM));
        }

        txtName.setText(place.getName());
        if (context != null)
            txtName.setTextColor(context.getResources().getColor(R.color.title_text_color));// R.color.title_text_color);
        String str = context.getResources().getString(R.string.km);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        double doubleDistance = place.getDistance();
        boolean km = sp.getBoolean(context.getResources().getString(R.string.settings_km_or_miles_key), true);
        if (!km) {
            doubleDistance *= 0.62137;
            if (isAdded()){
                str = context.getResources().getString(R.string.miles);
            }else {
                str = "Miles";
            }
        }
        NumberFormat formatter = new DecimalFormat("#.##");
        String strDistance = formatter.format(doubleDistance); // Creates a string containing "x.xx"
        txtDistance.setText(strDistance + " " + str);


        navigate.setOnClickListener(this);
        if (isAdded()){
            favoritesDBHandler = new FavoritesDBHandler(getContext());
        }else {
            favoritesDBHandler = new FavoritesDBHandler(context);
        }
        ArrayList<Place> favPlaces;
        favPlaces = favoritesDBHandler.getPlacesArray();
        for (int i = 0; i < favPlaces.size(); i++) {
            if (place.getId().equals(favPlaces.get(i).getId())){
                star.setBackgroundResource(R.drawable.star_full);
                favorite = true;
                return;
            }
        }


    }


    public void setMyLocation(LatLng myLocation) {
        if (myLocationMarker != null){
            myLocationMarker.remove();
        }
        this.myLocation = myLocation;
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location);
        if (gMap != null) {
            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (isAdded()) {
                myLocationMarker = gMap.addMarker(new MarkerOptions().position(this.myLocation).title(getResources().getString(R.string.my_location)).icon(icon));
                myLocationMarker.showInfoWindow();
            }else{
                myLocationMarker = gMap.addMarker(new MarkerOptions().position(myLocation).title("My location").icon(icon));
            }
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.myLocation, MAP_ZOOM));
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (myLocation != null && gMap != null) {

            gMap.clear();
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location);
            gMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()));
            if (isAdded()) {
                gMap.addMarker(new MarkerOptions().position(this.myLocation).title(getResources().getString(R.string.my_location)).icon(icon)).showInfoWindow();
            }else{
                gMap.addMarker(new MarkerOptions().position(myLocation).title("My location").icon(icon));
            }
            GetDirectionsTask getDirectionsTask = new GetDirectionsTask(this);
            getDirectionsTask.execute(myLocation, marker.getPosition());
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (myLocation != null) {
            outState.putParcelable(MYLOCATION_KEY, myLocation);
        }
        outState.putParcelableArrayList(CURRENT_PHOTOS_KEY, currentPhotos);
        outState.putString(SAVE_TITLE_KEY, txtName.getText().toString());
        outState.putString(SAVE_RATING_KEY, txtRating.getText().toString());
        outState.putString(SAVE_DISTANCE_KEY, txtDistance.getText().toString());
        outState.putString(SAVE_ADDRESS_KEY, txtAddress.getText().toString());
        outState.putString(SAVE_PHONE_KEY, txtPhoneNumber.getText().toString());
        outState.putParcelable(SAVE_LAST_PLACE_KEY, this.place);
        outState.putString(SAVE_WEB_KEY, this.mWebsite);
        outState.putBoolean(SAVE_FAV_KEY, this.favorite);

    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void getPolyline(ArrayList<LatLng> polyline) {
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(polyline.get(0), 15)); // zoom
        gMap.addMarker(new MarkerOptions().position(polyline.get(0))
                .title("Origin"));
        mPoly = gMap.addPolyline(new PolylineOptions().addAll(polyline).color(Color.MAGENTA));
    }

    @Override
    public void getJsonResponse(String json) {
        mWebsite = null;
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject result = obj.getJSONObject("result");
            String internationalNumber, formattedPhonenUmber, website;
            try{
                internationalNumber = result.getString("international_phone_number");
                formattedPhonenUmber = result.getString("formatted_phone_number");
                txtPhoneNumber.setText(context.getResources().getString(R.string.phone_number) + formattedPhonenUmber + " , " + internationalNumber +" (int) ");
                txtPhoneNumber.setMaxLines(1);

            }catch (JSONException e) {
                e.printStackTrace();
            }try{
                website = result.getString("website");
                mWebsite = website;

            }catch (JSONException e) {
                e.printStackTrace();
            }
            String address = result.getString("vicinity");
            txtAddress.setText(address);
            ArrayList<String> strPhotos = new ArrayList<>();
            JSONArray photos = new JSONArray();
            try {
                photos = result.getJSONArray("photos");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (photos.length() != 0){
                layoutScrollView.setVisibility(View.VISIBLE);
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject photoObj = photos.getJSONObject(i);
                    strPhotos.add(photoObj.getString("photo_reference"));
                }
                imgThread(strPhotos);
            }else{
                layoutScrollView.setVisibility(View.GONE);
            }
            float rating = (float) result.getDouble("rating");
            float totalPeopleRated = (float) result.getDouble("user_ratings_total");
            txtRating.setText("rating: " + rating + "/5 " + "(" + totalPeopleRated + " people)" );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        layoutScrollView.removeAllViews();
        layoutScrollView.removeAllViewsInLayout();

    }


    private static final String IMG_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=700&maxheight=700&photoreference=%1$s&key=AIzaSyAVHdXLunth3pJSQCYVBNmrwa2Pc3CTlyE";

    public void imgThread(final ArrayList<String> strPhotos){
        currentPhotos = new ArrayList<>();
        // Determine the number of cores on the device
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        // Construct thread pool passing in configuration options
        // int minPoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit,
        // BlockingQueue<Runnable> workQueue
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES*2,
                NUMBER_OF_CORES*2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
        // Executes a task on a thread in the thread pool
        executor.execute(new Runnable() {
            public void run() {
                for (int i = 0; i < strPhotos.size(); i++) {

                    try {
                        Bitmap b = Picasso.with(context).load(String.format(IMG_URL, strPhotos.get(i))).placeholder(R.drawable.loading).error(R.drawable.no_photo)
                                .resize(800, 450).onlyScaleDown().centerCrop().get();
                        putBitmap(b, i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Do some long running operation in background
                    // on a worker thread in the thread pool!
                }
            }
        });

        executor.shutdown();
    }

    private void putBitmap(final Bitmap bitmap, final int id) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                ImageView imageView;
                if (getContext() == null){
                    imageView = new ImageView(context);
                }else {
                    imageView = new ImageView(getContext());
                }
                imageView.setId(id);
                imageView.setPadding(5, 0, 2, 0);
                if (!landScapeMOde){
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(800, 450));
                }else{
//                    setScaledImage(imageView, id);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(800, 450));
                }
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setImageBitmap(bitmap);
                layoutScrollView.addView(imageView);
                currentPhotos.add(bitmap);
            }
        });
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnWeb:
                if (mWebsite != null){
                    if (!mWebsite.startsWith("https://") && !mWebsite.startsWith("http://")){
                        mWebsite = "http://" + mWebsite;
                    }
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(mWebsite));
                    if (i.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(i);
                    }
                }else {
                    Toast.makeText(context, "Cant find website",  Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnFavorite:
                if (!favorite){
                    star.setBackgroundResource(R.drawable.star_full);
                    favorite = true;
                    if (this.place != null){
                        if (favoritesDBHandler != null && favoritesDBHandler.insert(place)) {
                            Intent in = new Intent(FavoritesFragment.ACTION_ADD_TO_FAV);
                            in.putExtra(FavoritesFragment.INTENT_KEY_ADD_TO_FAV, place);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(in);
                            Toast.makeText(context, place.getName() + "This place is a favorite place.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    if (favoritesDBHandler != null) {
                        favoritesDBHandler.remove(place.getId());
                        Intent in = new Intent(FavoritesFragment.ACTION_ADD_TO_FAV);
                        in.putExtra(FavoritesFragment.INTENT_KEY_ADD_TO_FAV, place);
                        in.putExtra(REMOVE_FROM_FAV, true);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(in);
                        Toast.makeText(context, place.getName() + "The place is no longer a favorite spot anymore.", Toast.LENGTH_SHORT).show();
                    }
                    star.setBackgroundResource(R.drawable.star_empty);
                    favorite = false;
                }
                break;
            case R.id.btnNavigate:
                if (this.place != null){ // save it in saved instance state
                    LatLng coordinate = place.getCoords();
                    Uri gmmIntentUri = Uri.parse("geo:" + coordinate.latitude + "," + coordinate.longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(mapIntent);

                    }
                }

                break;
        }

    }


    public class LocationReceiver extends BroadcastReceiver {
        public LocationReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.

            Place place = intent.getParcelableExtra(MapsFragment.KEY_LOCATION_INTENT_PLACE);
            if (place != null)
                setLocation(place);
            LatLng latLng = intent.getParcelableExtra(MapsFragment.KEY_LOCATION_COORDS);
            if (latLng != null)
                setMyLocation(latLng);
        }
    }

}

