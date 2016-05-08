package com.h.almog.simpletolive.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.h.almog.simpletolive.PlaceListAdapter;
import com.h.almog.simpletolive.fragments.FavoritesFragment;
import com.h.almog.simpletolive.fragments.MapsFragment;
import com.h.almog.simpletolive.module.Place;

// THE RECEIVER WILL ADD PLACE TO FAVORITE LIST.

public class AddToFavReceiver extends BroadcastReceiver {
    private PlaceListAdapter adapter;
    private boolean remove;

    public AddToFavReceiver(PlaceListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Place place = intent.getParcelableExtra(FavoritesFragment.INTENT_KEY_ADD_TO_FAV);
        remove = intent.getBooleanExtra(MapsFragment.REMOVE_FROM_FAV, false);
        if (!remove){
            adapter.addPlace(place);
        }else {
            adapter.removePlace(place);
        }
    }
}
