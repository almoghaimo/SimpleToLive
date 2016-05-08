package com.h.almog.simpletolive.module;

import android.graphics.Bitmap;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Almog on 19/03/2016.
 */
public class Place implements android.os.Parcelable{
    public static final int IS_OPEN = 1;
    public static final int IS_NOT_OPEN = 0;
    private String id;
    private String name, address;
    private double distance;
    private String imageURL;
    private Bitmap bitMapImage;
    private boolean openNow;
    private LatLng coords;

    public Place(String id, String name, String address, String imgURL, LatLng coords, double distance) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.imageURL = imgURL;
        this.coords = coords;
        this.distance = distance;
    }

    public Place(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        distance = in.readDouble();
        imageURL = in.readString();
        openNow = in.readByte() != 0;
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };


    public Place(String id, String name, String address, float distance, int isOpen, Bitmap bm, LatLng coords) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.distance = distance;
        if (isOpen == IS_OPEN){
            this.openNow = true;
        }else{
            this.openNow = false;
        }
        this.bitMapImage = bm;
        this.coords = coords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }

    public Bitmap getBitMapImage() {
        return bitMapImage;
    }

    public void setBitMapImage(Bitmap bitMapImage) {
        this.bitMapImage = bitMapImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(distance);
        dest.writeString(imageURL);
        dest.writeByte((byte) (openNow ? 1 : 0));
    }

    public String getId() {
        return id;
    }

    public LatLng getCoords() {
        return coords;
    }

    public void setCoords(LatLng coords) {
        this.coords = coords;
    }
}
