package com.h.almog.simpletolive.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.google.android.gms.maps.model.LatLng;
import com.h.almog.simpletolive.service.GpsService;
import com.h.almog.simpletolive.PlaceListAdapter;
import com.h.almog.simpletolive.R;
import com.h.almog.simpletolive.service.SearchTaskService;
import com.h.almog.simpletolive.fragments.FavoritesFragment;
import com.h.almog.simpletolive.fragments.MapsFragment;
import com.h.almog.simpletolive.fragments.PlacesListFragment;
import com.h.almog.simpletolive.reciver.ChargeConnection;

public class MainActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener,
        TabLayout.OnTabSelectedListener,
        PlaceListAdapter.OnChangeTabSelected,
        GpsService.OnSetMyLocation,
        GpsService.OnSetOfflineMode,
        SearchTaskService.OnDismissDialogSearch,
        GpsService.OnDismissDialogLocation, OnShowcaseEventListener {

    private TabLayout tabLayout;

    // ==> TAB KEYS FOR TAB LAYOUT
    public static final int FRAG_FAVORITES = 0;
    public static final int FRAG_PLACES_LIST = 1;
    public static final int FRAG_MAP = 2;

    //Shared preference keys.
//    public static final String LOCATION_FOUND_KEY = "locationFoundKey"; //
    public static final String LAT_KEY = "latKey";
    public static final String LONGITUDE_KEY = "longitudeKey";
    public static final String OFFLINE_MODE_KEY = "offline";
    public static final String IS_TABLET = "isTabletSP";

    // Intent key - to SearchTaskService
    public static final String QUERY_KEY = "queryKey"; // Intent key - to SearchTaskService

    //SAVED INSTANCE STATE CODES
    private static final String CURRENT_TAB = "currentTab";
    private static final String TRY_FIND_LOCATION_AGAIN = "tryFindLocationAgain";
    private static final String GOT_LOCATION = "gotLocation";

    // TAG for map fragment(use only in tabletMode);
    public static final String MAP_FRAG_TAG = "mapFragTag";

    // Location

    private boolean gotLocation = false;

    private boolean offlineMode = false;
    private ViewPager viewPager;
    private  MyViewPagerAdapter adapter;
    private ChargeConnection chargeConnection;
    private boolean isTablet;
    private FragmentManager manager;
    private SearchView searchView;

    // gps Service;
    private Intent gpsServiceIntent;
    private GpsService myGpsService;

    private ProgressDialog progDialogLocation;
    private ProgressDialog progDialogSearch;

    private Intent searchServiceIntent;
    private SearchTaskService mySearchService;

    private boolean bindGps = false;
    private boolean bindSearch = false;
    private TextPaint paint;
    private Toolbar toolbar;

    private CoordinatorLayout coordinatorLayout;

    private View locationView;
    // Shared Preferences key
    private static final String FIRST_ENTER_SP = "firstAppEnter";



    private boolean firstAppEnter = false;

    private Button locButtonSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout_main_activity);


        //initialize viewPager

        manager = getSupportFragmentManager();
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new MyViewPagerAdapter(manager);
        View containerMap = findViewById(R.id.containerMapTablet);

        // Tablet mode
        if (containerMap != null){
            isTablet = true;
        }


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putBoolean(IS_TABLET, isTablet).apply();

        if (isTablet){
            viewPager.setOffscreenPageLimit(2);
            if (savedInstanceState == null){
                manager.beginTransaction().add(R.id.containerMapTablet, new MapsFragment(), MAP_FRAG_TAG).commit();
            }else{
                // use the same fragment map if is tablet.
                MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
                manager.beginTransaction().replace(R.id.containerMapTablet, mapsFragment, MAP_FRAG_TAG).commit();
            }
        }else{
            viewPager.setOffscreenPageLimit(3);
        }



        viewPager.setAdapter(adapter);
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);


        if (savedInstanceState == null){
            viewPager.setCurrentItem(FRAG_PLACES_LIST);
        }else{
            offlineMode = savedInstanceState.getBoolean(OFFLINE_MODE_KEY);
            viewPager.setCurrentItem(savedInstanceState.getInt(CURRENT_TAB));
            gotLocation = savedInstanceState.getBoolean(GOT_LOCATION);
        }

        //initialize custom toolbar
        toolbar = (Toolbar) findViewById(R.id.action_bar);

        if (toolbar != null){
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
        locButtonSearch = (Button) findViewById(R.id.actionBarNavigate);


        locButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSearchTaskService("");
            }
        });
        searchView = (SearchView) findViewById(R.id.actionBarSearch);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);

        isThePhoneInCharge(); // make a listener to charge sensor

        gpsServiceIntent = new Intent(MainActivity.this, GpsService.class);
        searchServiceIntent = new Intent(MainActivity.this, SearchTaskService.class);


    }

    // get the tool bar place items for first introductions.

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        // first enter?
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        firstAppEnter = sp.getBoolean(FIRST_ENTER_SP, true);
        if (firstAppEnter){
            firstAppEnter();
        }
    }

    // make the ability to connecting between main activity to gps service

    private ServiceConnection mConnectionGps = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            GpsService.LocalBinder binder = (GpsService.LocalBinder) service;
            myGpsService = binder.getServiceInstance(); //Get instance of your service!
            myGpsService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!
            bindGps = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindGps = false;
        }
    };

    private ServiceConnection mConnectionSearch = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "GpsServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            SearchTaskService.LocalBinder binderSearch = (SearchTaskService.LocalBinder) service;
            mySearchService = binderSearch.getServiceInstance(); //Get instance of your service!
            mySearchService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!
            bindSearch = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "GpsServiceDisconnected called", Toast.LENGTH_SHORT).show();
            bindSearch = false;
        }
    };

    // ==> If is the first time you get in to the app
    // get introduction how to use the app
    private void firstAppEnter() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int radius = sp.getInt(getResources().getString(R.string.seekBarKey), -1);
        if (radius == -1) {
            sp.edit().putInt(getResources().getString(R.string.seekBarKey), 2000).apply();
        }
        final int[] location = new int[2];
        locButtonSearch.getLocationOnScreen(location);
        Target homeTarget = new Target() {
            @Override
            public Point getPoint() {
                // Get approximate position of home icon's center
                int actionBarSize = toolbar.getHeight();
                int x = actionBarSize / 2 + location[0] - 40;
                int y = actionBarSize / 2 + location[1] - 35;
                return new Point(x, y);
            }
        };
        int textSize = 30;
        int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                textSize, getResources().getDisplayMetrics());
        paint = new TextPaint();
        paint.setTextSize(pixels);
        paint.setTypeface(Typeface.SERIF);
        paint.setColor(Color.WHITE);

        new ShowcaseView.Builder(this, true)
                .doNotBlockTouches().setShowcaseEventListener(this).setContentTextPaint(paint)
                .setContentText("Click here and find all the places around you").hideOnTouchOutside()
                .setTarget(homeTarget)
                .build();
        sp.edit().putBoolean(FIRST_ENTER_SP, false).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean offlineMode = sharedPreferences.getBoolean(OFFLINE_MODE_KEY, false);

        // check if there is permission to connect to gps and network.  marshmallow version
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // fix land scape mode initalize offline mode..
            if (!offlineMode){
                boolean isInternetConnection = isInternetConnection();
                if (isInternetConnection) {
                    if (!gotLocation) {
                        findLocation();
                    }
                }else {
                    Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    offlineMode();
                }
            }
        } else { // permission denied.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1); // request for permission.
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    public void findLocation(){
        progDialogLocation = new ProgressDialog(this);
        progDialogLocation.setMessage(getResources().getString(R.string.looking_for_your_location));
        if (!firstAppEnter){
            progDialogLocation.show();
        }
        startService(gpsServiceIntent);
        bindService(gpsServiceIntent, mConnectionGps, Context.BIND_AUTO_CREATE); //Binding to the service!
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionBarNavigate:
                sendSearchTaskService("");
                break;
            case R.id.settings:
                Intent in = new Intent(this, PrefsActivity.class);
                startActivity(in);
                break;
            case R.id.findLocation:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().putBoolean(OFFLINE_MODE_KEY, false).apply();
                findLocation();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        query = query.replace(" ", "+");
        // hide keyboard after searching
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
        sendSearchTaskService(query);
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }



    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle(getResources().getString(R.string.exit_dialog_title));
        alertDialog.setMessage(getResources().getString(R.string.exit_dialog_message));
        // On pressing Settings button
        alertDialog.setPositiveButton((getResources().getString(R.string.exit_dialog_positive_btn)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();

            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton((getResources().getString(R.string.exit_dialog_negative_btn)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setCancelable(false);
        // Showing Alert Message
        alertDialog.show();
    }

    // The method send explicitly intent to SearchTaskService intent.
    public void sendSearchTaskService(String query){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gotLocation = sharedPreferences.getBoolean(GpsService.LOCATION_FOUND_KEY, false);
        if (!offlineMode && gotLocation){
            if (!isFinishing()){
                progDialogSearch = new ProgressDialog(this);
                progDialogSearch.setMessage("Loading data from web..");
                progDialogSearch.show();

            }
            searchServiceIntent.putExtra(QUERY_KEY, query);
            startService(searchServiceIntent);
            bindService(searchServiceIntent, mConnectionSearch, Context.BIND_AUTO_CREATE); //Binding to the service!
        }else {
            if (offlineMode){
                Toast.makeText(this, this.getResources().getString(R.string.offline_mode_search) , Toast.LENGTH_SHORT).show();
            }else{
                if (!gotLocation){
                    Toast.makeText(this, getResources().getString(R.string.not_found_location), Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    // chack the ability to the internet connection.

    private boolean isInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()){
            // Internet connect
            return true;
        }// Internet disconnect.
        return false;
    }




    private void offlineMode() {
        if (progDialogLocation.isShowing())
            progDialogLocation.dismiss();
        offlineMode = true;
        Toast.makeText(MainActivity.this, getResources().getString(R.string.offline_mode), Toast.LENGTH_SHORT).show();
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Offline mode - this is your last Search", Snackbar.LENGTH_INDEFINITE );

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(GpsService.LOCATION_FOUND_KEY, false);
        edit.apply();
        viewPager.setCurrentItem(FRAG_PLACES_LIST);
        Intent in = new Intent(PlacesListFragment.ACTION_OFFLINE_MODE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(in); // show you the last Search
    }



    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    // permission request result.

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(this, GpsService.class);
                    startService(intent);
                }else{
                    offlineMode();
                }
                break;

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_TAB, viewPager.getCurrentItem());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gotLocation = sharedPreferences.getBoolean(GpsService.LOCATION_FOUND_KEY, false);
        outState.putBoolean(GOT_LOCATION, gotLocation);
        outState.putBoolean(OFFLINE_MODE_KEY, offlineMode);
        if (isTablet){
            MapsFragment mapsFragment = (MapsFragment) manager.findFragmentByTag(MAP_FRAG_TAG);
            getSupportFragmentManager().putFragment(outState, "mContent", mapsFragment);

        }

    }


    @Override
    protected void onDestroy() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove(OFFLINE_MODE_KEY);
        sp.edit().remove(SearchTaskService.NEXT_RESULT_KEY);
        sp.edit().remove(PlacesListFragment.MORE_RESULT_KEY_INTENT);
        sp.edit().apply();
        unregisterReceiver(chargeConnection);
        if(bindGps){
            unbindService(mConnectionGps);
        }

        if (bindSearch){
            unbindService(mConnectionSearch);
        }
        if ( progDialogLocation != null && progDialogLocation.isShowing() ){
            progDialogLocation.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void dismissDialogLocation() {
        if (progDialogLocation.isShowing())
            progDialogLocation.dismiss();
    }


    // the method implements from the adapter to change the tab by the action.

    @Override
    public void setChangeTab(int change) {
        viewPager.setCurrentItem(change);
    }

    // register global receiver and listen if the charging is on.
    public void isThePhoneInCharge(){
        chargeConnection = new ChargeConnection();
        IntentFilter filterChargeConnected = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        registerReceiver(chargeConnection, filterChargeConnected);
        IntentFilter filterChargeDisconnected = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(chargeConnection, filterChargeDisconnected);


    }


    // interface method call by gps service
    @Override
    public void setMyLocation(LatLng myLocation) {
        if (!firstAppEnter && progDialogLocation != null && progDialogLocation.isShowing())
            progDialogLocation.dismiss();
        gotLocation = true;
        if (this.isTablet){
            MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MainActivity.MAP_FRAG_TAG);
            mapsFragment.setMyLocation(myLocation);
        }else {
            Intent in = new Intent(MapsFragment.ACTION_ADD_LOCATION);
            in.putExtra(MapsFragment.KEY_LOCATION_COORDS, myLocation);
            LocalBroadcastManager.getInstance(this).sendBroadcast(in);

        }
    }




    @Override
    public void startSearchAround() {
        sendSearchTaskService("");
    }

    @Override
    public void setOfflineMode() {
        if (progDialogLocation != null && progDialogLocation.isShowing())
            progDialogLocation.dismiss();
        offlineMode();
    }

    @Override
    public void dismissDialogSearch() {
        if (progDialogSearch.isShowing())
            progDialogSearch.dismiss();
    }




    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {

    }
    // initialize the second showcase view

    @Override
    public void onShowcaseViewDidHide(final ShowcaseView showcaseView) {
        final int[] location = new int[2];
        searchView.getLocationOnScreen(location);
        Target homeTarget = new Target() {
            @Override
            public Point getPoint() {
                // Get approximate position of home icon's center
                int actionBarSize = toolbar.getHeight();
                int x = actionBarSize / 2 + location[0];
                int y = actionBarSize / 2 + location[1];
                return new Point(x, y);
            }
        };
        new ShowcaseView.Builder(this, true)
                .doNotBlockTouches().setContentTextPaint(paint)
                .setContentText("Click here and search any place you want.").hideOnTouchOutside()
                .setTarget(homeTarget)
                .build();
        firstAppEnter = false;
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

    }

    // view pager adapter


    private class MyViewPagerAdapter extends FragmentPagerAdapter {

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case FRAG_FAVORITES:
                    return new FavoritesFragment();
                case FRAG_PLACES_LIST:
                    return new PlacesListFragment();
                case FRAG_MAP:
                    return new MapsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            if (isTablet){
                return 2;
            }
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case FRAG_PLACES_LIST:
                    return getResources().getString(R.string.tab_places_frag);
                case FRAG_FAVORITES:
                    return getResources().getString(R.string.tab_favorites_frag);
                case FRAG_MAP:
                    return getResources().getString(R.string.tab_map_frag);
            }
            return null;
        }
    }
}
