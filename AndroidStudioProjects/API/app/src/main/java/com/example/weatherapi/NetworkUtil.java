package com.example.weatherapi;

import android.net.Uri;
import android.util.Log;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtil {

    private final static String TAG = "Network";

    private final static String url = "http://dataservice.accuweather.com/forecasts/v1/daily/1day/295863";

    private final static String API_KEY = "TDm9Vvt4JFiyFEuHrxLgrGWczhQ4brIy";

    private final static String Param = "apikey";

    private final static String PARAM_METRIC = "metric";

    private final static String METRIC_VALUE = "true";

    public static URL buildWeather(){
        Uri builtUri = Uri.parse(url).buildUpon()
                .appendQueryParameter(Param, API_KEY)
                .appendQueryParameter(PARAM_METRIC, METRIC_VALUE)
                .build();

        URL Nurl = null;
        try{
            Nurl = new URL(builtUri.toString());
        } catch (MalformedURLException m){
            m.printStackTrace();
        }
        Log.i(TAG, "BuildWeather for ulr: " + Nurl);

        return Nurl;
    }

    public static String getResponseFromHttp(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try{
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if(hasInput){
                return scanner.next();
            }
            else
            {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }

    }

}
