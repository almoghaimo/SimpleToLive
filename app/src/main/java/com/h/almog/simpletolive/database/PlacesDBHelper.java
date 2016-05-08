package com.h.almog.simpletolive.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Almog on 29/03/2016.
 */
public class PlacesDBHelper extends SQLiteOpenHelper {
    // DataBase name.
    private static final String DB_NAME = "places_db.db";
    //DataBase version.
    private static final int DB_VERSION = 1;


    public PlacesDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlLastPlacesTable = String.format("CREATE TABLE %1$s(%2$s TEXT PRIMARY KEY, %3$s TEXT, %4$s TEXT, %5$s TEXT, %6$s INTEGER, %7$s BLOB, %8$s REAL, %9$s REAL)",
                LastPlacesDBHandler.TABLE_LAST_PLACES_NAME,  LastPlacesDBHandler.COL_ID, LastPlacesDBHandler.COL_NAME,
                LastPlacesDBHandler.COL_ADDRESS, LastPlacesDBHandler.COL_DISTANCE, LastPlacesDBHandler.COL_IS_OPEN,
                LastPlacesDBHandler.COL_IMG, LastPlacesDBHandler.COL_LAT, LastPlacesDBHandler.COL_LNG);

        db.execSQL(sqlLastPlacesTable);

        String sqlFavoritesTable = String.format("CREATE TABLE %1$s(%2$s TEXT PRIMARY KEY," +
                        " %3$s TEXT, %4$s TEXT,%5$s REAL, %6$s INTEGER," +
                        " %7$s BLOB, %8$s REAL, %9$s REAL)",
                FavoritesDBHandler.TABLE_FAVORITES_NAME, FavoritesDBHandler.COL_ID, FavoritesDBHandler.COL_NAME,
                FavoritesDBHandler.COL_ADDRESS, FavoritesDBHandler.COL_DISTANCE, FavoritesDBHandler.IS_OPEN,
                FavoritesDBHandler.COL_IMG, FavoritesDBHandler.COL_LAT, FavoritesDBHandler.COL_LNG);
        db.execSQL(sqlFavoritesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LastPlacesDBHandler.TABLE_LAST_PLACES_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesDBHandler.TABLE_FAVORITES_NAME);
    }



}
