package com.surge.surgepredictor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    String urlFixed = "http://8056b731.ngrok.io/surge/uber-surge/", url = "http://8056b731.ngrok.io/surge/uber-surge/";
    URL url1;
    HttpURLConnection urlConnection;
    static String TAG = "tag";
    static Button button, button2, button3;
    private int COUNT = 2;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker MarkerOne;
    private Marker MarkerTwo;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    LocationManager locationManager;
    static String time, date, setTime;
    String result = null;
    static String resultMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.)
//                        .setContentTitle("My notification")
//                        .setContentText("Hello World!");

        button = (Button) findViewById(R.id.timepick);
        button2 = (Button) findViewById(R.id.datepick);
        button3 = (Button) findViewById(R.id.button);
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String HOUR = String.valueOf(hour);
        String MINUTE= String.valueOf(minute);
        if(hour<10) {
            HOUR = "0" + String.valueOf(hour);
        }else{
            HOUR = String.valueOf(hour);
        }
        if(minute<10) {
            MINUTE = "0"+ String.valueOf(minute);
        }
        else{
            MINUTE = String.valueOf(minute);
        }
        time = HOUR + ":" + MINUTE;
        setTime = time;
        button.setText(time);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String sday = String.valueOf(day), smonth = String.valueOf(month), syear = String.valueOf(year);
        if(day<10)
            sday = "0" + sday;
        if(month<10)
            smonth = "0" + smonth;
        date = sday+"/"+smonth+"/"+syear;
        button2.setText(date);
        button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                DialogFragment time = new FragmentTime();
                time.show(getFragmentManager(), "Time");
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment date = new FragmentDate();
                date.show(getFragmentManager(), "Date");
            }
        });

        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                predict();
            }
        });
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation("gps");
        if(location!=null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Point A"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (COUNT == 3) {
                    COUNT = 1;
                    mMap.clear();

                    //MarkerTwo.remove();
                    //MarkerOne.remove();
                }
                if (COUNT == 1) {
                    MarkerOne = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Point A")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    COUNT++;

                } else if (COUNT == 2) {
                    COUNT++;
                    MarkerTwo = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Point B")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                }

            }
        });
    }

    public void predict(){
        String params;
        String minutes[] = time.split(":");
        int roundoff = Integer.parseInt(minutes[1]);
        if(roundoff%10<=5)
            roundoff = roundoff - roundoff%10;
        if(roundoff%10>5)
            roundoff = roundoff + (10 - roundoff%10);
        if(roundoff == 60) {
            roundoff = 0;
            int a = Integer.parseInt(minutes[0]) + 1;
            minutes[0] = String.valueOf(a);
        }
        if(roundoff == 0)
            minutes[1] = "00";
        else
            minutes[1] = Integer.toString(roundoff);

        time = minutes[0]+":"+minutes[1];
        Log.d(TAG, time);
        params = "?time="+time.replace(":","%3A")+"&day="+date.replace("/","%2F");
        url = urlFixed+params;
        Log.d(TAG, url);
        NetworkCall newReq = new NetworkCall();
        new NetworkCall().execute();
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        MarkerOne = mMap.addMarker(options);
        COUNT++;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }


    private class NetworkCall extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            try {
                url1 = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                urlConnection = (HttpURLConnection) url1.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = readStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }


            int i = 0;
            Log.d(TAG, "esting");
            if(result != null){
                result = result.replaceAll("\\s+", "");
                result = result.replaceAll("\\[", "").replaceAll("\\]","");

                String values[] = result.split(",");
                Log.d(TAG,values[0]);
                float minimumSurge = Float.parseFloat(values[0]);
                int minSurgeTime = 0;
                for(i=0; i<7;i++){
                    if(minimumSurge>Float.parseFloat(values[i])) {
                        minimumSurge = Float.parseFloat(values[i]);
                        minSurgeTime = i;
                    }
                }
                if(values[minSurgeTime] == values[3])
                    minimumSurge = 3;
                String resultTime = calculateSurgeTime(time, minSurgeTime);
//                Date date = new Date();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

                resultMessage =  "The expected surge at "+setTime+" is "+values[3]+". You can book a cab at "+ resultTime +" when the expected surge would be " + minimumSurge + "x. Would you like me to notify you 10 minutes in advance?";
                Log.d("Result",resultMessage);

            }

            return null;
        }

        public String calculateSurgeTime(String s, int index){
            int arr[] = {30,20,10,0,10,20,30};
            String t[] =s.split(":");
            int hour = Integer.parseInt(t[0]);
            int min  = Integer.parseInt(t[1]);
            if(index < 3){
                if(min >= arr[index]){
                    min=min-arr[index];
                }
                else {
                    hour = hour - 1;
                    min = min + 60 - arr[index];
                }
            }
            else if(index > 3){
                if(min + arr[index] >= 60){
                    min = min+arr[index] - 60;
                    hour = hour +1;
                }
                else {
                    min=min+arr[index];
                }
            }
            String HOUR, MINUTES;
            if(hour<10)
                HOUR = "0" + hour;
            else
                HOUR = String.valueOf(hour);
            if(min<10)
                MINUTES = "0" + min;
            else
                MINUTES = String.valueOf(min);
            return HOUR+":"+MINUTES;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void results) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            SomeDialog newFragment = new SomeDialog ();
            newFragment.show(ft, "dialog");
            super.onPostExecute(results);
        }
    }


    public String readStream(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, result.toString());
        return result.toString();
    }


    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    public void TimeSelect() {

    }

    public static class FragmentTime extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));

        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int min) {
            String HOUR, MINUTE;

            if(hourOfDay<10) {
                HOUR = "0" + String.valueOf(hourOfDay);
            }else{
                HOUR = String.valueOf(hourOfDay);
            }
            if(min<10) {
                MINUTE = "0"+ String.valueOf(min);
            }
            else{
                MINUTE = String.valueOf(min);
            }
            time = HOUR+":"+MINUTE;
            setTime = HOUR+":"+MINUTE;
            Log.d(TAG, setTime);
            Log.d("tagare", setTime+"      "+time);
            button.setText(time);
        }
    }
    public static class FragmentDate extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(),this,year,month,day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String sday = String.valueOf(dayOfMonth), smonth = String.valueOf(monthOfYear), syear = String.valueOf(year);
            if(dayOfMonth<10)
                sday = "0" + sday;
            if(monthOfYear<10)
                smonth = "0" + smonth;
            date = sday+"/"+smonth+"/"+syear;
            button2.setText(date);
        }
    }
    public static class SomeDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Hey there!")
                    .setMessage(resultMessage)
                    .setNegativeButton("No, thanks!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing (will close dialog)
                        }
                    })
                    .setPositiveButton("Yeah!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do something
                        }
                    })
                    .create();
        }
    }
}
