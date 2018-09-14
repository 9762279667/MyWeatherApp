package com.bajajallianz.myweatherapp.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bajajallianz.myweatherapp.R;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

public class WeatherProcess {

    public interface IOnWeatherReport {
        public void onWeather(ArrayList<Weather> list);
    }

    private Context mContext;
    private TextView description, temperation;
    private ImageView weatherBackground;
    private ProgressDialog pDialog;

    public WeatherProcess(Context mContext) {
        this.mContext = mContext;
    }

    public WeatherProcess setUI(TextView description, TextView temperation, ImageView weatherBackground, ProgressDialog pDialog) {
        this.description = description;
        this.temperation = temperation;
        this.weatherBackground = weatherBackground;
        this.pDialog = pDialog;
        return this;
    }

    private ArrayList<Weather> sortData(ArrayList<Weather> weatherList) {
        HashMap<String, Weather> dates = new HashMap<>();
        for (Weather weather : weatherList) {
            dates.put(weather.getDateTime(), weather);
//            dates.put(getDate(weather.getDateTime()), weather);
        }

        Set<String> keyset = dates.keySet();
        ArrayList<String> dateStr = Collections.list(Collections.enumeration(keyset));
        Collections.sort(dateStr, new Comparator<String>() {
//            DateFormat f = new SimpleDateFormat("MM/dd/yyyy '@'hh:mm a");

            @Override
            public int compare(String o1, String o2) {
//                try {
                return o1.compareTo(o2);
//                    return f.parse(o1).compareTo(f.parse(o2));
//                } catch (ParseException e) {
//                    throw new IllegalArgumentException(e);
//                }
            }
        });

        ArrayList<Weather> newWeatherList = new ArrayList<>();

        for (String det : dateStr) {
            Weather ws = dates.get(det);
            String[] ds = formatDate(ws.getDateTime());

            String dayOfTheWeek = "";
            try {
                dayOfTheWeek = (String) android.text.format.DateFormat.format("EEEE", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(ws.getDateTime())); // Thursday
            } catch (ParseException e) {
                e.printStackTrace();
            }

            ws.setDateTime(dayOfTheWeek + ", " + ds[0]);
            ws.setTime(ds[1] + " " + ds[2].toUpperCase());
            newWeatherList.add(ws);
        }

        return newWeatherList;
    }

    private String[] formatDate(String date) {
        try {
            Date d = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(date);
            String newDAte = (new SimpleDateFormat("dd/MM/yyyy hh:mm a")).format(d);
            String[] splited = newDAte.split("\\s+");
            return splited;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String capWordFirstLetter(String fullname) {
        String fname = "";
        String s2;
        StringTokenizer tokenizer = new StringTokenizer(fullname);
        while (tokenizer.hasMoreTokens()) {
            s2 = tokenizer.nextToken().toLowerCase();
            if (fname.length() == 0)
                fname += s2.substring(0, 1).toUpperCase() + s2.substring(1);
            else
                fname += " " + s2.substring(0, 1).toUpperCase() + s2.substring(1);
        }

        return fname;
    }

    public void processWeather(JSONObject response, ArrayList<Weather> weatherList, IOnWeatherReport weatherListener) {
        try {

            JSONArray listArr = (JSONArray) response.getJSONArray("list");
            for (int i = 0; i < listArr.length(); i++) {
                JSONObject jObj = listArr.getJSONObject(i);
                JSONObject tempObj = ((JSONObject) jObj.getJSONObject("main"));
                String temp = tempObj.getString("temp");
                String tempMin = tempObj.getString("temp_min");
                String tempMax = tempObj.getString("temp_max");
                String desc = ((JSONObject) jObj.getJSONArray("weather").get(0)).getString("description");
                String date = jObj.getString("dt_txt");
//                String[] splited = date.split("\\s+");
                weatherList.add(new Weather(date, temp + (char) 0x00B0 + "C", tempMin + (char) 0x00B0 + "C", tempMax + (char) 0x00B0 + "C", capWordFirstLetter(desc), ""));
            }

            weatherList = sortData(weatherList);

            weatherListener.onWeather(weatherList);


            // Parsing json object response
            // response will be a json object
//            JSONObject jsonObj = (JSONObject) listArr.getJSONArray("weather").get(0);
            // display weather description into the "description textview"
//            description.setText(jsonObj.getString("description"));
            // display the temperature
//            temperature.setText(response.getJSONObject("main").getString("temp") + (char) 0x00B0 + "C");

            int backgroundImage = R.mipmap.rainy_wallpaper;

            //choose the image to set as background according to weather condition
//            if (jsonObj.getString("main").equals("Clouds")) {
//                backgroundImage = R.mipmap.clouds_wallpaper;
//            } else if (jsonObj.getString("main").equals("Rain")) {
//                backgroundImage = R.mipmap.rainy_wallpaper;
//            } else if (jsonObj.getString("main").equals("Snow")) {
//                backgroundImage = R.mipmap.snow_wallpaper;
//            }

            // load image from link and display it on background
            // We'll use the Glide library
            Glide
                    .with(mContext)
                    .load(backgroundImage)
                    .centerCrop()
                    .crossFade()
                    .into(weatherBackground);

            // hide the loading Dialog
            pDialog.dismiss();


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Error , try again ! ", Toast.LENGTH_LONG).show();
            pDialog.dismiss();

        }


    }

}
