package com.h.almog.simpletolive.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.h.almog.simpletolive.PlaceListAdapter;
import com.h.almog.simpletolive.database.LastPlacesDBHandler;
import com.h.almog.simpletolive.module.Place;

import java.util.ArrayList;

public class OfflineReceiver extends BroadcastReceiver {
    public static final int OFFLINE_MODE = 2;
    private PlaceListAdapter adapter;
    private LastPlacesDBHandler handler;




    public OfflineReceiver(PlaceListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        handler = new LastPlacesDBHandler(context);
        ArrayList<Place> places = handler.getPlacesArray();
        adapter.refresh(places,OFFLINE_MODE);
    }
}
