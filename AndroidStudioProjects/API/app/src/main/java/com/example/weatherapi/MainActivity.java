package com.example.weatherapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<Weather> weatherArrayList = new ArrayList<>();
    URL weatherUrl = NetworkUtil.buildWeather();

    Button add;
    Button read;
    Button clear;
    TextView current_date;
    TextView minTemp;


    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);


        add = findViewById(R.id.ButtonAdd);
        read = findViewById(R.id.ReadButton);
        clear = findViewById(R.id.ClearData);
        current_date = findViewById(R.id.dateId);
        minTemp = findViewById(R.id.tempID);


        add.setOnClickListener(this);
        read.setOnClickListener(this);
        clear.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Weather weather = new Weather();


        switch (view.getId()){


            case R.id.ClearData:

                Log.i("LOG","-----Clear Table------");
                database.execSQL("delete from " + DBHelper.TABLE_CONTACTS);
                minTemp.setText("");
                current_date.setText("");
                break;


            case R.id.ButtonAdd:
                new FetchWeatherDetails().execute(weatherUrl);
                Log.i(TAG, "onCreate: weatherUrl: " + weatherUrl);
                break;


            case R.id.ReadButton:
                Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);

                if(cursor.moveToLast()){
                    int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                    int date = cursor.getColumnIndex(DBHelper.KEY_DATE);
                    int temp = cursor.getColumnIndex(DBHelper.KEY_TEMP);
                    weather.setMinTemp(cursor.getString(temp));
                    minTemp.setText(cursor.getString(temp));
                    weather.setDate(cursor.getString(date));
                    current_date.setText(cursor.getString(date));
                    do{
                        Log.d("mLog", "ID = " + cursor.getInt(idIndex)
                        + ", date = " + cursor.getString(date)
                                + ", minTemp = " + cursor.getString(temp));
                    }while (cursor.moveToNext());
                }
                else {
                    Log.d("mLog", "0 rows");
                }
                cursor.close();
                break;
        }
        dbHelper.close();
    }

    public void InsertData(String date, String minTemp){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues newCon = new ContentValues();
        newCon.put(dbHelper.KEY_DATE, date);
        newCon.put(DBHelper.KEY_TEMP, minTemp);
        database.insert(DBHelper.TABLE_CONTACTS, null, newCon);
    }


    private class FetchWeatherDetails extends AsyncTask<URL, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... urls) {

            URL weatherUrl = urls[0];
            String weatherSearchResult = null;

            try{
                weatherSearchResult = NetworkUtil.getResponseFromHttp(weatherUrl);
            }catch (IOException e){
                e.printStackTrace();
            }
            Log.i(TAG, "doInBackground: weatherResultSearch: " + weatherSearchResult);
            return weatherSearchResult;
        }

        @Override
        protected void onPostExecute(String weatherSearchWeather) {

            if(weatherSearchWeather != null && !weatherSearchWeather.equals("")){
                weatherArrayList = parseJSON(weatherSearchWeather);
            }
            super.onPostExecute(weatherSearchWeather);
        }
    }

    private ArrayList<Weather> parseJSON(String weatherSearchWeather) {
        if (weatherArrayList != null){
            weatherArrayList.clear();
        }

        if (weatherSearchWeather != null){
            try{
                JSONObject rootObject = new JSONObject(weatherSearchWeather);
                JSONArray results = rootObject.getJSONArray("DailyForecasts");

                for (int i=0; i <results.length(); i++){

                    JSONObject resultObj = results.getJSONObject(i);

                    String date = resultObj.getString("Date");


                    Log.i(TAG, "parseJSON date: " + date);

                    JSONObject temperature = resultObj.getJSONObject("Temperature");
                    String minTemperature = temperature.getJSONObject("Minimum").getString("Value");

                    Log.i(TAG, "parseJSON minimum: " + minTemperature);

                    String maxTemperatur = temperature.getJSONObject("Maximum").getString("Value");


                    Log.i(TAG, "parseJSON maximum: " + maxTemperatur);


                    InsertData(date, minTemperature);

                }
                return weatherArrayList;

            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        return null;
    }

}
