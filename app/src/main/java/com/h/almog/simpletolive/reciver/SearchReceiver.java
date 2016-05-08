package com.h.almog.simpletolive.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.h.almog.simpletolive.PlaceListAdapter;
import com.h.almog.simpletolive.service.SearchTaskService;
import com.h.almog.simpletolive.module.Place;

import java.util.ArrayList;

public class SearchReceiver extends BroadcastReceiver {
    private PlaceListAdapter adapter;

    public SearchReceiver(PlaceListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        ArrayList<Place> places = intent.getParcelableArrayListExtra(SearchTaskService.PLACES_KEY);
        int requestCode = intent.getIntExtra(SearchTaskService.SEARCH_BY_KEY, -1);
        boolean addMorePlaces = intent.getBooleanExtra(SearchTaskService.ADD_PLACES_KEY, false);
        if (places == null || places.size() == 0){
            Toast.makeText(context, "nothing to show", Toast.LENGTH_SHORT).show();
        }else{
            if (addMorePlaces){

            if (requestCode != -1){
                if (requestCode == SearchTaskService.ADD_MORE_RESULT){
                    adapter.addMorePlaces(places, requestCode);
                }else{
                    adapter.addMorePlaces(places, requestCode);
                }

                }
            }else {
                if (requestCode != -1){
                    adapter.refresh(places, requestCode);

                }
            }

        }

    }


}

