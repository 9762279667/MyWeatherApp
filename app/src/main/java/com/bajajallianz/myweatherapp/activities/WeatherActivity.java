package com.bajajallianz.myweatherapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bajajallianz.myweatherapp.R;
import com.bajajallianz.myweatherapp.permission.PermissionRequest;
import com.bajajallianz.myweatherapp.utils.GPSTracker;
import com.bajajallianz.myweatherapp.utils.Weather;
import com.bajajallianz.myweatherapp.utils.WeatherAdapter;
import com.bajajallianz.myweatherapp.utils.WeatherController;
import com.bajajallianz.myweatherapp.utils.WeatherProcess;
import com.canelmas.let.AskPermission;
import com.canelmas.let.DeniedPermission;
import com.canelmas.let.RuntimePermissionListener;
import com.canelmas.let.RuntimePermissionRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;

public class WeatherActivity extends AppCompatActivity implements RuntimePermissionListener {

    // we"ll make HTTP request to this URL to retrieve weather conditions
//    private final String weatherWebserviceURL = "http://api.openweathermap.org/data/2.5/weather?q=pune&appid=2156e2dd5b92590ab69c0ae1b2d24586&units=metric";
    private final String pincodeURL1 = "http://api.openweathermap.org/data/2.5/forecast?zip=";
    private final String pincodeURL2 = ",in&appid=2156e2dd5b92590ab69c0ae1b2d24586&units=metric";
    private final String geoURL1 = "http://api.openweathermap.org/data/2.5/forecast?lat=";
    private final String geoURL2 = "&lon=";
    private final String geoURL3 = "&appid=2156e2dd5b92590ab69c0ae1b2d24586&units=metric";


    //the loading Dialog
    private ProgressDialog pDialog;
    // Textview to show temperature and description
    private TextView temperature, description;
    // background image
    private ImageView weatherBackground;
    // JSON object that contains weather information
    private JSONObject jsonObj;
    private GPSTracker tracker;
    private WeatherAdapter mAdapter;
    private ArrayList<Weather> weathersList = new ArrayList<>();
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // link the XML layout to this JAVA class
        setContentView(R.layout.activity_weather);
        initComponents();
        initGPS();
        displayWeatherList();
    }

    private void initComponents() {

        temperature = (TextView) findViewById(R.id.id_temperature);
        description = (TextView) findViewById(R.id.id_description);
        weatherBackground = (ImageView) findViewById(R.id.id_weatherbackground);
        final EditText pincodeET = (EditText) findViewById(R.id.id_weather_pincode_et);
        ((Button) findViewById(R.id.id_weather_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeWeatherRequest(pincodeET.getText().toString().trim(), null, null);
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }


    private void displayWeatherList() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new WeatherAdapter(weathersList);
        recyclerView.setAdapter(mAdapter);
    }

    private WeatherProcess.IOnWeatherReport weatherListener = new WeatherProcess.IOnWeatherReport() {
        @Override
        public void onWeather(ArrayList<Weather> list) {
            mAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Userd to ask the permissions
     */
    @AskPermission({ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})
    private void initGPS() {
        tracker = GPSTracker.getInstance(this);
        tracker.getLocation(new Runnable() {
            @Override
            public void run() {
            }
        });

        if (tracker != null) {
            makeWeatherRequest(null, "" + tracker.getLatitude(), "" + tracker.getLongitude());
        }
    }

    private void makeWeatherRequest(String pincode, String lat, String lon) {

        // prepare the loading Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait while retrieving the weather condition ...");
        pDialog.setCancelable(false);

        // Check if Internet is working
        if (!isNetworkAvailable(this)) {
            // Show a message to the user to check his Internet
            Toast.makeText(this, "Please check your Internet connection", Toast.LENGTH_LONG).show();
        } else {

            pDialog.show();

            String url = null;

            if (pincode != null)
                url = pincodeURL1 + pincode + pincodeURL2;
            else if (lat != null & lon != null)
                url = geoURL1 + lat + geoURL2 + lon + geoURL3;

            // make HTTP request to retrieve the weather
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    new WeatherProcess(WeatherActivity.this).setUI(description, temperature, weatherBackground, pDialog).processWeather(response, weathersList, weatherListener);
                }


            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("tag", "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Error while loading ... ", Toast.LENGTH_SHORT).show();
                    // hide the progress dialog
                    pDialog.dismiss();
                }
            });

            // Adding request to request queue
            WeatherController.getInstance(this).addToRequestQueue(jsonObjReq);


        }

    }

    ////////////////////check internet connection
    @AskPermission(ACCESS_NETWORK_STATE)
    public boolean isNetworkAvailable(final Context context) {
        try {
            final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        } catch (SecurityException e) {
            return false;
        }
    }


    /**
     * Used for Marshmallow permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionRequest.getInstance(WeatherActivity.this).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onShowPermissionRationale(List<String> permissions, final RuntimePermissionRequest request) {
        PermissionRequest.getInstance(WeatherActivity.this).onShowPermissionRationale(permissions, request);
    }

    @Override
    public void onPermissionDenied(List<DeniedPermission> results) {
        PermissionRequest.getInstance(WeatherActivity.this).onPermissionDenied(results);
    }

}
