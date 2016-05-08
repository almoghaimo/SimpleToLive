package com.h.almog.simpletolive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.h.almog.simpletolive.activities.MainActivity;
import com.h.almog.simpletolive.database.FavoritesDBHandler;
import com.h.almog.simpletolive.database.LastPlacesDBHandler;
import com.h.almog.simpletolive.fragments.FavoritesFragment;
import com.h.almog.simpletolive.fragments.MapsFragment;
import com.h.almog.simpletolive.fragments.PlacesListFragment;
import com.h.almog.simpletolive.module.Place;
import com.h.almog.simpletolive.reciver.OfflineReceiver;
import com.h.almog.simpletolive.service.SearchTaskService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


/**
 * Created by Almog on 20/03/2016.
 */

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceHolder> {
    private Context context;
    private ArrayList<Place> places;
    private Handler handler;
    private LastPlacesDBHandler lastPlacesDBHandler;
    private int typeOfSearchCode; // search is by text or by location.
    private int listCode; // is it favorites list adapter or lastPlaces list adapter.
    private static final String BUNDLE_KEY_IMAGE = "image";
    private static final String BUNDLE_KEY_HOLDER = "holder";
    private OnChangeTabSelected tabLlistener;


    public PlaceListAdapter(Context context, int listCode,ArrayList<Place> places) {
        this.context = context;
        if (places == null)
            places = new ArrayList<>();
        this.places = places;
        lastPlacesDBHandler = new LastPlacesDBHandler(context);
        this.listCode = listCode;
        tabLlistener = (OnChangeTabSelected) context;
    }


    public void mHandleMessage(){
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bitmap b = msg.getData().getParcelable(BUNDLE_KEY_IMAGE);

                    PlaceHolder holder = (PlaceHolder) msg.getData().getSerializable(BUNDLE_KEY_HOLDER);
                    if (holder != null)
                        holder.bindImage(b);

                return true;
            }
        });
    }
    @Override
    public PlaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceHolder(v);
    }

    @Override
    public void onBindViewHolder(PlaceHolder holder, int position) {
        Place p = places.get(position);
        holder.bindPlace(p);
        if (typeOfSearchCode != OfflineReceiver.OFFLINE_MODE && p.getBitMapImage() == null) {
            ImgDownloadThread t = new ImgDownloadThread(holder, p.getImageURL());
            mHandleMessage();
            t.start();
        }
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void addPlace(Place place){
        places.add(place);
        notifyItemInserted(places.size());
    }
    public void removePlace(Place place){
        notifyDataSetChanged();
        places.remove(place);
    }

    public void addPlaces(ArrayList<Place> places){
        this.places.clear();
        this.places.addAll(places);
        this.notifyDataSetChanged();
    }
    public void addMorePlaces(ArrayList<Place> places, int requestCode){
        this.typeOfSearchCode = requestCode;
        this.places.addAll(places);
        this.notifyDataSetChanged();
    }

    public void refresh(ArrayList<Place> places, int requestCode) {
        this.typeOfSearchCode = requestCode;
        this.places.clear();
        this.places.addAll(places);
        this.notifyDataSetChanged();
        tabLlistener.setChangeTab(MainActivity.FRAG_PLACES_LIST);

    }

    private void remove(int adapterPosition) {
        places.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }

    public void clean() {
        this.places.clear();
        this.notifyDataSetChanged();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder implements Serializable, View.OnClickListener, View.OnCreateContextMenuListener, View.OnLongClickListener {
        private ImageView icon;
        private TextView name,address, distance;
        private int position;
        private FavoritesDBHandler favoritesDBHandler;

        public PlaceHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.txtTitlePlace);
            address = (TextView) itemView.findViewById(R.id.txtAddressPlace);
            distance = (TextView) itemView.findViewById(R.id.txtDistance);
            icon = (ImageView) itemView.findViewById(R.id.iconPlaceMapFrag);
            favoritesDBHandler = new FavoritesDBHandler(context);
            itemView.setOnClickListener(this);

        }

        public void bindPlace(final Place place){
            position = getAdapterPosition();
            name.setText(place.getName());
            address.setText(place.getAddress());
            name.setTextColor(context.getResources().getColor(R.color.title_text_color));// R.color.title_text_color);
            double doubleDistance = place.getDistance();
            String str = context.getResources().getString(R.string.km);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean km = sp.getBoolean(context.getResources().getString(R.string.settings_km_or_miles_key), true);
            if (doubleDistance >= 0);{
                if (!km) {
                    doubleDistance *= 0.62137;
                    str = context.getResources().getString(R.string.miles);
                }
                NumberFormat formatter = new DecimalFormat("#.##");
                String strDistance = formatter.format(doubleDistance); // Creates a string containing "x.xx"
                distance.setText(strDistance + " " + str);
            }

            Bitmap bm = place.getBitMapImage();
            icon.setImageResource(R.drawable.loading);
            if (bm == null) {
                if (typeOfSearchCode == OfflineReceiver.OFFLINE_MODE) {
                    icon.setImageResource(R.drawable.no_photo);
                }
            } else {
                icon.setImageBitmap(bm);
            }

            if (listCode == PlacesListFragment.LAST_PLACES_LIST_CODE) // the context menu will open just if the list is the list in search fragment.
                itemView.setOnCreateContextMenuListener(this);
            else
                itemView.setOnLongClickListener(this);
        }

        public void bindImage(Bitmap b) {
            if (b != null) {
                icon.setImageBitmap(b);
                Place p = places.get(position);
                p.setBitMapImage(b);
                lastPlacesDBHandler.updateIMG(p.getId(), b);
            }else{
                icon.setImageResource(R.drawable.no_photo);

            }
        }


        @Override
        public void onClick(View v) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isTablet = sp.getBoolean(MainActivity.IS_TABLET, false);
            Place place = places.get(getAdapterPosition());
            if (isTablet){
                MapsFragment mapsFragment = (MapsFragment) ((FragmentActivity)context).getSupportFragmentManager().findFragmentByTag(MainActivity.MAP_FRAG_TAG);
                mapsFragment.setLocation(place);
            }else {
                tabLlistener.setChangeTab(MainActivity.FRAG_MAP);
                Intent in = new Intent(MapsFragment.ACTION_ADD_LOCATION);
                in.putExtra(MapsFragment.KEY_LOCATION_INTENT_PLACE, place);
                LocalBroadcastManager.getInstance(context).sendBroadcast(in);
            }
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo; // get the info about the object that clicked in the list.
            final Place place = places.get(getAdapterPosition());
            menu.setHeaderTitle(place.getName()); // The title of the context menu is the movie title.
            menu.add(1, PlacesListFragment.CONTEXT_MENU_FAVORITES, 1, R.string.context_menu_add_to_fav).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (favoritesDBHandler.insert(place)) {
                        tabLlistener.setChangeTab(MainActivity.FRAG_FAVORITES);
                        Intent in = new Intent(FavoritesFragment.ACTION_ADD_TO_FAV);
                        in.putExtra(FavoritesFragment.INTENT_KEY_ADD_TO_FAV, place);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(in);
                    } else {
                        Toast.makeText(context, R.string.item_exicst_in_fav, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            menu.add(1, PlacesListFragment.CONTEXT_MENU_SHARE, 2, R.string.context_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    createShareIntent(place);
                    return true;
                }
            });}


        private void createShareIntent(Place place){
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.share_title );
            shareIntent.putExtra(Intent.EXTRA_TEXT, place.getName());
            if (place.getBitMapImage() != null) {
                String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), place.getBitMapImage(), "title", "description");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
            }

            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "send"));

        }



        @Override
        public boolean onLongClick(View v) {
            Place place = places.get(getAdapterPosition());
            remove(getAdapterPosition());
            favoritesDBHandler.remove(place.getId());
            return true;
        }
    }



    public interface OnChangeTabSelected{
            public void setChangeTab(int change);
    }

    private class ImgDownloadThread extends Thread {
        private String urlString;
        private PlaceHolder holder;
        private static final String IMG_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&maxheight=400&photoreference=%1$s&key=AIzaSyAVHdXLunth3pJSQCYVBNmrwa2Pc3CTlyE";

        public ImgDownloadThread(PlaceHolder holder, String urlString){
            this.urlString = urlString;
            this.holder = holder;
        }

        @Override
        public void run() {
            Bitmap b;
            try {
                if (typeOfSearchCode == SearchTaskService.CODE_By_TEXT) {
                    if (urlString != null && !urlString.equals("")) {
                        b = Picasso.with(context).load(String.format(IMG_URL, urlString)).placeholder(R.drawable.no_photo).error(R.drawable.no_photo)
                                .resize(160, 190).onlyScaleDown().centerCrop().get();
                    } else {
                        b = null;
                    }
                }else {
                    if (urlString != null && !urlString.equals("")) {
                        b = Picasso.with(context).load(urlString).placeholder(R.drawable.loading).error(R.drawable.no_photo)
                            .resize(160, 180).onlyScaleDown().centerCrop().get();
                    }else {
                        b = null;
                    }
                }
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putParcelable(BUNDLE_KEY_IMAGE, b);
                bundle.putSerializable(BUNDLE_KEY_HOLDER, holder);
                msg.setData(bundle);
                msg.sendToTarget();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}

