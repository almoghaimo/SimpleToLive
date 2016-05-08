package com.h.almog.simpletolive.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

/**
 * Created by Almog on 03/04/2016.
 */
public class PlacesProvider extends ContentProvider {
    public final static String AUTHORITY = "com.h.almog.simpletolive.provider.PlacesProvider";
    private PlacesDBHelper dbOpenHelper;
    private SQLiteDatabase db;


    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);



    public static class Favorites{
        public static final String TABLE_FAVORITES_NAME = "favoritesTable";

    }

    @Override
    public boolean onCreate() {
        boolean ret = true;
        dbOpenHelper = new PlacesDBHelper(getContext());
        db = dbOpenHelper.getWritableDatabase();

        if (db == null) {
            ret = false;
        }

        if (db.isReadOnly()) {
            db.close();
            db = null;
            ret = false;
        }
        return ret;
    }

    protected String getTableName(Uri uri){
        List<String> pathSegments = uri.getPathSegments();
        return pathSegments.get(0);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
        Context context = getContext();
        if (context != null)
            cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        long id = db.insert(getTableName(uri), null, values);
        Context context = getContext();
        if (context != null)
            context.getContentResolver().notifyChange(uri,null);
        if (id > 0){
            return ContentUris.withAppendedId(uri, id);
        }else {
            return null;
        }

    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int result = db.delete(getTableName(uri), selection, selectionArgs);
        Context context = getContext();
        if (context != null)
            context.getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count = db.update(getTableName(uri), values, selection, selectionArgs);
        Context context = getContext();
        if (context != null)
            context.getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }



}
