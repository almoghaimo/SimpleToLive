package com.h.almog.simpletolive.fragments;


import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h.almog.simpletolive.PlaceListAdapter;
import com.h.almog.simpletolive.R;
import com.h.almog.simpletolive.database.FavoritesDBHandler;
import com.h.almog.simpletolive.module.Place;
import com.h.almog.simpletolive.reciver.AddToFavReceiver;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment {
    public static final String INTENT_KEY_ADD_TO_FAV = "intentKeyAddToFav";
    public static final String ACTION_ADD_TO_FAV = "com.h.almog.simpletolive.ACTION_ADD_TO_FAV";
    public static final int FAV_LIST_CODE = 1;
    private PlaceListAdapter adapter;
    private RecyclerView favPlacesList;
    private ArrayList<Place> favPlaces;
    private FavoritesDBHandler favoritesDBHandler;
    private ArrayList<Place> places;

    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);
        favPlacesList = (RecyclerView) v.findViewById(R.id.favRecycleView);
        favoritesDBHandler = new FavoritesDBHandler(getContext());
        places = favoritesDBHandler.getPlacesArray();
        adapter = new PlaceListAdapter(getContext(), FAV_LIST_CODE, places);
        favPlacesList.setLayoutManager(new LinearLayoutManager(getContext()));
        favPlacesList.setAdapter(adapter);
        AddToFavReceiver addToFavReceiver = new AddToFavReceiver(adapter);
        IntentFilter addToFavInFilter = new IntentFilter(ACTION_ADD_TO_FAV);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(addToFavReceiver, addToFavInFilter);
        return v;
    }

}
