package com.example.fabrice.parking;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Gestion_distance {

    public  HashMap<Integer, Double> liste_distance = new HashMap<>();

    public  APIgoogle getNewThread()
    {
        return new APIgoogle();
    }

    public class APIgoogle extends AsyncTask<Double, Void, Double> {

        private String url2 = null;
        private String resultat ="";
        private int id;
        double distance = 0;

        @Override
        protected Double doInBackground(Double... params)
        {
            // URL de l'apie de distance


            id = Integer.parseInt(params[0].toString().split("\\.")[0]);
            HttpURLConnection connection = null;
            this.url2 = "https://maps.googleapis.com/maps/api/directions/json?origin=" + MainActivity.maPosition.getLatitude() + "," + MainActivity.maPosition.getLongitude() + "&destination=" + params[1] + "," + params[2] + "&key=AIzaSyAfAZz5zrP4aviEiFhNVai_oZHmrGeJOac";
            InputStream flux = null;
            try
            {
                connection = (HttpURLConnection) new URL(url2).openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                flux = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(flux));
                String ligne;

                while ((ligne=reader.readLine()) != null)
                {
                    resultat = resultat + ligne + "\n";
                }


                JSONObject jsObj = new JSONObject(resultat);
                JSONArray jsarr = jsObj.getJSONArray("routes");
                jsObj = jsarr.getJSONObject(0);
                jsarr = jsObj.getJSONArray("legs");
                jsObj = jsarr.getJSONObject(0);
                distance = jsObj.getJSONObject("distance").getDouble("value");

            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
            finally
            {
                try
                {
                    flux.close();
                }
                catch (Throwable t)
                {

                }
                try
                {
                    connection.disconnect();
                }
                catch (Throwable t)
                {

                }
            }
            liste_distance.put(id, distance);
            return distance;

        }

    }

}
