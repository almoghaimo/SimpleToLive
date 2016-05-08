package com.h.almog.simpletolive.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.h.almog.simpletolive.module.Place;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Almog on 29/03/2016.
 */
public class FavoritesDBHandler {

    //1. Table name
    public static final String TABLE_FAVORITES_NAME = "favoritesTable";

    //2. Table id.
    public static final String COL_ID = "_id";
    //3. the names of the places.
    public static final String COL_NAME = "colName";
    //4. Address
    public static final String COL_ADDRESS = "colAddress";
    //5. Distance
    public static final String COL_DISTANCE = "colDistance";
    //6. IsOpen = true = 1, IsOpen = false = 0.
    public static final String IS_OPEN = "colIsOpen";
    //7. image.
    public static final String COL_IMG = "colImg";
    // 8.
    public static final String COL_LAT = "colLat";

    //9.
    public static final String COL_LNG = "colLng";

    public static final Uri CONTENT_URI_FAVORITES = Uri.parse("content://" + PlacesProvider.AUTHORITY + "/" + TABLE_FAVORITES_NAME);
    private Context context;

    public FavoritesDBHandler(Context context) {

        this.context = context;
    }

    // insert the place to the Fav.
    // return false if he is already in the favorites list.
    public boolean insert(Place place){

        if (isNotInFav(place.getId())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_ID, place.getId());
            contentValues.put(COL_NAME, place.getName());
            contentValues.put(COL_ADDRESS, place.getAddress());
            contentValues.put(COL_DISTANCE, place.getDistance());
            LatLng coords = place.getCoords();
            double lat, lng;
            if (coords != null) {
                contentValues.put(COL_LAT, coords.latitude);
                contentValues.put(COL_LNG, coords.longitude);
            }
            // check if the place is open now
            if (place.isOpenNow()) {
                contentValues.put(IS_OPEN, Place.IS_OPEN);
            } else {
                contentValues.put(IS_OPEN, Place.IS_NOT_OPEN);
            }

            Bitmap img = place.getBitMapImage();
            // if there is img make it byte array.
            if (img != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                contentValues.put(COL_IMG, byteArray);
            }
            try {
                context.getContentResolver().insert(CONTENT_URI_FAVORITES, contentValues);
            } catch (SQLiteException ex) {
                Log.e(",", ex.getMessage());
            }
        }else {
            return false;
        }
        return true;
    }

    public Cursor getAllFavPlacesCursor(){
        CursorLoader cursorLoader = new CursorLoader(context, CONTENT_URI_FAVORITES, null,
                null, null, null);
        Cursor c = cursorLoader.loadInBackground();
        return c;
    }

    private boolean isNotInFav(String id) {
        Cursor c = getAllFavPlacesCursor();
        String mID;
        while (c.moveToNext()){
            mID = c.getString(c.getColumnIndex(COL_ID));
            if (mID.equals(id)){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Place> getPlacesArray(){
        Cursor c = getAllFavPlacesCursor();
        ArrayList<Place> places = new ArrayList<>();


        while(c.moveToNext()){
            String id = c.getString(c.getColumnIndex(COL_ID));
            String name = c.getString(c.getColumnIndex(COL_NAME));
            String address = c.getString(c.getColumnIndex(COL_ADDRESS));
            float distance = c.getFloat(c.getColumnIndex(COL_DISTANCE));
            int isOpen = c.getInt(c.getColumnIndex(IS_OPEN));
            Bitmap bm;
            if (c.getBlob(c.getColumnIndex(COL_IMG)) != null){
                byte[] bitMapData = c.getBlob(c.getColumnIndex(COL_IMG));
                bm = BitmapFactory.decodeByteArray(bitMapData, 0, bitMapData.length);
            }else {
                bm = null;
            }
            double lat = c.getDouble(c.getColumnIndex(COL_LAT));
            double lng = c.getDouble(c.getColumnIndex(COL_LNG));
            LatLng coordinate = new LatLng(lat, lng);
            places.add(new Place(id, name, address, distance, isOpen, bm, coordinate));
        }
        return places;
    }
    public void deleteAllPlaces(){
        try {
            context.getContentResolver().delete(CONTENT_URI_FAVORITES, null, null);
        }catch (SQLiteException ex){
            Log.e(",", ex.getMessage());
        }
    }

    public void remove(String id) {
        try{
            context.getContentResolver().delete(CONTENT_URI_FAVORITES, COL_ID + "=?", new String[]{id});
        }catch (SQLiteException ex){
            Log.e(",", ex.getMessage());
        }

    }

}


