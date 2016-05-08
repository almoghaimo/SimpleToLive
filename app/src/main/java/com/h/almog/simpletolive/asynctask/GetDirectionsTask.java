package com.h.almog.simpletolive.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Almog on 17/04/2016.
 */
public class GetDirectionsTask extends AsyncTask<LatLng, Void, String> {
    private OnGetPolyline listener;

    public GetDirectionsTask(OnGetPolyline listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(LatLng... params) {
        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        String points = "";
        LatLng origin = params[0];
        LatLng destination = params[1];
        if (origin != null && destination != null){
            String urlString = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%1$s,%2$s&destination=%3$s,%4$s",
                    origin.latitude, origin.longitude, destination.latitude, destination.longitude);
            try {
                URL url = new URL(urlString);
                connection = (HttpsURLConnection) url.openConnection();

                if (connection.getResponseCode() != 200) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            try {
                JSONObject root = new JSONObject(builder.toString());
                JSONArray routes = root.getJSONArray("routes");
                JSONObject route1 = routes.getJSONObject(0);
                JSONObject polyline = route1.getJSONObject("overview_polyline");
                points = polyline.getString("points");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return points;
    }

    @Override
    protected void onPostExecute(String points) {
        ArrayList<LatLng> polyline = decodePoly(points);
        if (polyline.size() != 0){
            listener.getPolyline(polyline);
        }
    }

    public interface OnGetPolyline {
        public void getPolyline(ArrayList<LatLng> points);
    }

    // A method who get the decode pointer as string and return it like ArrayList<LatLng>.

    private ArrayList<LatLng> decodePoly(String encoded) {

        Log.i("Location", "String received: " + encoded);
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

        for (int i = 0; i < poly.size(); i++) {
            Log.i("Location", "Point sent: Latitude: " + poly.get(i).latitude + " Longitude: " + poly.get(i).longitude);
        }
        return poly;
    }
}