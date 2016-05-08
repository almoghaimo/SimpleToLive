package com.h.almog.simpletolive.fragments;


import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.h.almog.simpletolive.PlaceListAdapter;
import com.h.almog.simpletolive.R;
import com.h.almog.simpletolive.service.SearchTaskService;
import com.h.almog.simpletolive.database.LastPlacesDBHandler;
import com.h.almog.simpletolive.reciver.OfflineReceiver;
import com.h.almog.simpletolive.reciver.SearchReceiver;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlacesListFragment extends Fragment {
    public static final int CONTEXT_MENU_FAVORITES = 0;
    public static final int CONTEXT_MENU_SHARE = 1;
    //    private static final String ADAPTER_KEY = "adapterKey";
    public static final String MORE_RESULT_KEY_INTENT = "moreResultKeyIntent";

    private LastPlacesDBHandler handlerLastPlaces;
    private PlaceListAdapter adapter;
    private RecyclerView placesList;
//    private ArrayList<Place> places;

    public static final String ACTION_OFFLINE_MODE = "com.h.almog.simpletolive.ACTION_OFFLINE_MODE";

    public static final int LAST_PLACES_LIST_CODE = 0;


    public PlacesListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            handlerLastPlaces = new LastPlacesDBHandler(getContext());
            adapter = new PlaceListAdapter(getContext(), LAST_PLACES_LIST_CODE, handlerLastPlaces.getPlacesArray());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_places_list, container, false);
        placesList = (RecyclerView) v.findViewById(R.id.recyclePlaceList);
        placesList.setHasFixedSize(true);
        placesList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter == null)
            adapter = new PlaceListAdapter(getContext(), LAST_PLACES_LIST_CODE, null);
        placesList.setAdapter(adapter);


        SearchReceiver searchReceiver = new SearchReceiver(adapter);
        IntentFilter searchInFilter = new IntentFilter(SearchTaskService.ACTION_SEARCH);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchReceiver, searchInFilter);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        OfflineReceiver offlineReceiver = new OfflineReceiver(adapter);
        IntentFilter offlineInFilter = new IntentFilter(ACTION_OFFLINE_MODE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(offlineReceiver, offlineInFilter);

        registerForContextMenu(placesList);
        if (handlerLastPlaces == null)
            handlerLastPlaces = new LastPlacesDBHandler(getContext());
        setHasOptionsMenu(true);


        // ==> if the user scroll to the end of the recycle view.
        //==> check if there is more places to show for him.
        placesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                if(dy > 0) //check for scroll down
                {

                    boolean lastItemDisplay = isLastItemDisplaying(recyclerView);
                    if (lastItemDisplay){
                        sp.edit().putBoolean(MORE_RESULT_KEY_INTENT, true).apply();
                        Intent in = new Intent(getContext(), SearchTaskService.class);
                        in.putExtra(MORE_RESULT_KEY_INTENT, true);
                        getContext().startService(in);
                    }
                }
            }
        });



        return v;
    }

    private boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1)
                return true;
        }
        return false;
    }
//
//    public void setOldSearch() {
//        LastPlacesDBHandler handlerLastPlaces = new LastPlacesDBHandler(getContext());
//        ArrayList<Place> places = handlerLastPlaces.getPlacesArray();
//        adapter.addPlaces(places);
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll:
                handlerLastPlaces.deleteAllPlaces();
                adapter.clean();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    public void offline(Activity activity) {
//        handlerLastPlaces = new LastPlacesDBHandler(activity);
//        ArrayList<Place> places = handlerLastPlaces.getPlacesArray();
//        if (adapter == null)
//            adapter = new PlaceListAdapter(activity, 2, places);
//        adapter.refresh(places,2);
//    }
}
