package com.bajajallianz.myweatherapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class GPSTracker extends Service implements LocationListener {

    public static final int REQ_CODE_ACTION_LOCATION_SOURCE_SETTINGS = 121;

    // saving the context for later use
    private final Context mContext;

    // if GPS is enabled
    boolean isGPSEnabled = false;
    // if Network is enabled
    boolean isNetworkEnabled = false;
    // if Location co-ordinates are available using GPS or Network
    public boolean isLocationAvailable = false;

    // Location and co-ordinates coordinates
    Location mLocation;
    double mLatitude;
    double mLongitude;

    // Minimum time fluctuation for next update (in milliseconds)
    private static final long TIME = 30000;
    // Minimum distance fluctuation for next update (in meters)
    private static final long DISTANCE = 20;

    // Declaring a Location Manager
    protected LocationManager mLocationManager;

    public static GPSTracker getInstance(Context context) {
        return new GPSTracker(context);
    }

    private GPSTracker(Context context) {
        this.mContext = context;
        mLocationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);
    }

    /**
     * Returs the Location
     *
     * @return Location or null if no location is found
     */
    public Location getLocation(Runnable exception) {
        try {

            // Getting GPS status
            isGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // If GPS enabled, get latitude/longitude using GPS Services
            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, TIME, DISTANCE, this);
                if (mLocationManager != null) {
                    mLocation = mLocationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (mLocation != null) {
                        mLatitude = mLocation.getLatitude();
                        mLongitude = mLocation.getLongitude();
                        isLocationAvailable = true; // setting a flag that
                        // location is available
                        return mLocation;
                    }
                }
            }

            // If we are reaching this part, it means GPS was not able to
            // fetch
            // any location
            // Getting network status
            isNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, TIME, DISTANCE, this);
                if (mLocationManager != null) {
                    mLocation = mLocationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (mLocation != null) {
                        mLatitude = mLocation.getLatitude();
                        mLongitude = mLocation.getLongitude();
                        isLocationAvailable = true; // setting a flag that
                        // location is available
                        return mLocation;
                    }
                }
            }
            // If reaching here means, we were not able to get location
            // neither
            // from GPS not Network,
            if (!isGPSEnabled) {
                if (exception != null)
                    exception.run();
                // so asking user to open GPS
                askUserToOpenGPS();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // if reaching here means, location was not available, so setting
        // the
        // flag as false
        isLocationAvailable = false;
        return null;
    }

    /**
     * Gives you complete address of the location
     *
     * @return complete address in String
     */
    public String getLocationAddress() {

        if (isLocationAvailable) {

            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
				 */
                addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            } catch (IOException e1) {
                e1.printStackTrace();
                return ("IO Exception trying to get address:" + e1);
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments "
                        + Double.toString(mLatitude) + " , "
                        + Double.toString(mLongitude)
                        + " passed to address service";
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available), city, and
				 * country name.
				 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ? address
                                .getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return "No address found by the service: Note to the developers, If no address is found by google itself, there is nothing you can do about it.";
            }
        } else {
            return "Location Not available";
        }

    }

    /**
     * get latitude
     *
     * @return latitude in double
     */
    public double getLatitude() {
        if (mLocation != null) {
            mLatitude = mLocation.getLatitude();
        }
        return mLatitude;
    }

    /**
     * get longitude
     *
     * @return longitude in double
     */
    public double getLongitude() {
        if (mLocation != null) {
            mLongitude = mLocation.getLongitude();
        }
        return mLongitude;
    }

    /**
     * close GPS to save battery
     */
    public void closeGPS() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * show settings to open GPS
     */
    public void askUserToOpenGPS() {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        mAlertDialog
                .setTitle("Location not available, Open GPS?")
                .setMessage("Activate GPS to use use location services?")
                .setPositiveButton("Open Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                // mContext.startActivity(intent);
                                try {
                                    ((Activity) mContext)
                                            .startActivityForResult(intent,
                                                    REQ_CODE_ACTION_LOCATION_SOURCE_SETTINGS);
                                } catch (Exception e) {
                                    mContext.startActivity(intent);
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }
                        }).show();
    }

    /**
     * Updating the location when location changes
     */
    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}

// private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 300;
// private static final long MIN_TIME_BW_UPDATES = 1;
//
// private boolean isGPSEnabled = false;
// private boolean isNetworkEnabled = false;
// private boolean canGetLocation = false;
//
// private LocationManager locationManager;
//
// private static Location location;
//
// private double latitude;
// private double longitude;
//
// private GPSTracker(Context context) {
// // if (canToggleGPS(context)) {
// // turnGPSOn(context);
//
// // }
// turnGpsOn(context);
// getLocation(context);
// }
//
// public static GPSTracker getInstance(Context context) {
// return new GPSTracker(context);
// }
//
// @Override
// public void onLocationChanged(Location location) {
// GPSTracker.location = location;
// stopUsingGPS();
// }
//
// @Override
// public void onProviderDisabled(String provider) {
// }
//
// @Override
// public void onProviderEnabled(String provider) {
// }
//
// @Override
// public void onStatusChanged(String provider, int status, Bundle extras) {
// }
//
// /**
// * Function to get the user's current location
// *
// * @return
// */
// public void getLocation(Context context) {
// try {
// locationManager = (LocationManager) context
// .getSystemService(Context.LOCATION_SERVICE);
//
// isGPSEnabled = locationManager
// .isProviderEnabled(LocationManager.GPS_PROVIDER);
// isNetworkEnabled = locationManager
// .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
// if (isGPSEnabled == false && isNetworkEnabled == false) {
// // .. no network provider is enabled
// } else {
// this.canGetLocation = true;
// if (isNetworkEnabled) {
// locationManager.requestLocationUpdates(
// LocationManager.NETWORK_PROVIDER,
// MIN_TIME_BW_UPDATES,
// MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
// if (locationManager != null) {
// location = locationManager
// .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
// if (location != null) {
// latitude = location.getLatitude();
// longitude = location.getLongitude();
// }
// }
// }
// // .. if GPS Enabled get lat/long using GPS Services
// if (isGPSEnabled) {
// if (location == null) {
// locationManager.requestLocationUpdates(
// LocationManager.GPS_PROVIDER,
// MIN_TIME_BW_UPDATES,
// MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
// if (locationManager != null) {
// location = locationManager
// .getLastKnownLocation(LocationManager.GPS_PROVIDER);
// if (location != null) {
// latitude = location.getLatitude();
// longitude = location.getLongitude();
// }
// }
// }
// }
// }
// } catch (Exception e) {
// e.printStackTrace();
// System.out.println("### GPSTracker Fail to get location");
// }
// }
//
// /**
// * Stop using GPS listener Calling this function will stop using GPS in
// your
// * app
// * */
// public void stopUsingGPS() {
// if (locationManager != null) {
// locationManager.removeUpdates(GPSTracker.this);
// }
// }
//
// /**
// * Function to get latitude
// * */
// public double getLatitude() {
// if (location != null) {
// latitude = location.getLatitude();
// }
// return latitude;
// }
//
// /**
// * Function to get longitude
// * */
// public double getLongitude() {
// if (location != null) {
// longitude = location.getLongitude();
// }
// return longitude;
// }
//
// /**
// * Function to get location
// * */
// public Location getLocation() {
// return location;
// }
//
// /**
// * Function to check GPS/wifi enabled
// *
// * @return boolean
// * */
// public boolean canGetLocation() {
// return this.canGetLocation;
// }
//
// private void turnGPSOn(Context context) {
// String provider = Settings.Secure.getString(
// context.getContentResolver(),
// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//
// if (!provider.contains("gps")) { // if gps is disabled
// final Intent poke = new Intent();
// poke.setClassName("com.android.settings",
// "com.android.settings.widget.SettingsAppWidgetProvider");
// poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
// poke.setData(Uri.parse("3"));
// context.sendBroadcast(poke);
// }
// }
//
// /**
// * Not Working
// *
// * @param context
// */
// private void turnGPSOff(Context context) {
// String provider = Settings.Secure.getString(
// context.getContentResolver(),
// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//
// if (provider.contains("gps")) { // if gps is enabled
// final Intent poke = new Intent();
// poke.setClassName("com.android.settings",
// "com.android.settings.widget.SettingsAppWidgetProvider");
// poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
// poke.setData(Uri.parse("3"));
// context.sendBroadcast(poke);
// }
// }
//
// /**
// * Not Working
// *
// * @param context
// */
// private boolean canToggleGPS(Context context) {
// PackageManager pacman = context.getPackageManager();
// PackageInfo pacInfo = null;
//
// try {
// pacInfo = pacman.getPackageInfo("com.android.settings",
// PackageManager.GET_RECEIVERS);
// } catch (NameNotFoundException e) {
// return false; // package not found
// }
//
// if (pacInfo != null) {
// for (ActivityInfo actInfo : pacInfo.receivers) {
// // test if recevier is exported. if so, we can toggle GPS.
// if (actInfo.name
// .equals("com.android.settings.widget.SettingsAppWidgetProvider")
// && actInfo.exported) {
// return true;
// }
// }
// }
//
// return false; // default
// }
//
// private String beforeEnable = "";
//
// public static void turnGpsOn(Context context) {
// Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
// intent.putExtra("enabled", true);
// context.sendBroadcast(intent);
//
// if (checkSIMAvailable(context)) {
// enableDataService(context, true);
// }
//
// }
//
// public static void turnGpsOff(Context context) {
// Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
// intent.putExtra("enabled", false);
// context.sendBroadcast(intent);
// }
//
// public static boolean checkSIMAvailable(Context context) {
// // gets the current TelephonyManager
// TelephonyManager tm = (TelephonyManager) context
// .getSystemService(Context.TELEPHONY_SERVICE);
// if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
// return true;
// // the phone has a sim card
// } else {
//
// Toast.makeText(context, "No SIM Available...", Toast.LENGTH_LONG)
// .show();
// return false;
// }
// }
//
// public static void enableDataService(Context context, boolean enable) {
// try {
// final ConnectivityManager conman = (ConnectivityManager) context
// .getSystemService(Context.CONNECTIVITY_SERVICE);
// final Class conmanClass = Class
// .forName(conman.getClass().getName());
// final Field iConnectivityManagerField = conmanClass
// .getDeclaredField("mService");
// iConnectivityManagerField.setAccessible(true);
// final Object iConnectivityManager = iConnectivityManagerField
// .get(conman);
// final Class iConnectivityManagerClass = Class
// .forName(iConnectivityManager.getClass().getName());
// final Method setMobileDataEnabledMethod = iConnectivityManagerClass
// .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
// setMobileDataEnabledMethod.setAccessible(true);
//
// setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
//
// /**
// * Not Working
// *
// * @param context
// */
// private void turnGpsOFF(Context context) {
// if (null == beforeEnable) {
// String str = Settings.Secure.getString(
// context.getContentResolver(),
// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
// if (null == str) {
// str = "";
// } else {
// String[] list = str.split(",");
// str = "";
// int j = 0;
// for (int i = 0; i < list.length; i++) {
// if (!list[i].equals(LocationManager.GPS_PROVIDER)) {
// if (j > 0) {
// str += ",";
// }
// str += list[i];
// j++;
// }
// }
// beforeEnable = str;
// }
// }
// try {
// Settings.Secure.putString(context.getContentResolver(),
// Settings.Secure.LOCATION_PROVIDERS_ALLOWED, beforeEnable);
// } catch (Exception e) {
// }
// }
// }