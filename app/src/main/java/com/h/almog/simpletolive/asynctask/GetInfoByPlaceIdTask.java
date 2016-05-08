package com.h.almog.simpletolive.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Almog on 19/04/2016.
 */
public class GetInfoByPlaceIdTask extends AsyncTask<String, Void, String> {
    private final static String urlPlaceId = "https://maps.googleapis.com/maps/api/place/details/json?placeid=%1$s&key=AIzaSyAVHdXLunth3pJSQCYVBNmrwa2Pc3CTlyE";
    private OnGetJsonResponse listener;
    private Context context;

    public GetInfoByPlaceIdTask(OnGetJsonResponse listener) {
        this.listener = listener;
    }


    @Override
    protected String doInBackground(String... params) {
        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            URL url = new URL(String.format(urlPlaceId, params[0]));
            connection = (HttpsURLConnection) url.openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.d("GIBPlaceId", "internet Connection problems");
                return "ERROR WITH CONNECTION";
            }
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connection != null){
                connection.disconnect();
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return builder.toString();
    }

    @Override
    protected void onPostExecute(String json) {
        if (!json.startsWith("ERROR")){
            listener.getJsonResponse(json);
        }
    }

    public interface OnGetJsonResponse{
        public void getJsonResponse(String json);
    }
}
