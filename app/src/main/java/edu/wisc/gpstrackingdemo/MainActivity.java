package edu.wisc.gpstrackingdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity
{
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    private int FAST_UPDATE_INTERVAL() {
        return 5;
    }

    private int DEFAULT_UPDATE_INTERVAL() {
        return 30;
    }
//importing textview and switches
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address,
    tv_wayPointCounts;
    Button btn_newWaypoint, btn_showWayPointList;
    Switch sw_locationupdates, sw_gps;

    boolean updateOn = false;

    Location currentLocation;

    List<Location> savedLocations;


    //Location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;

    LocationCallBack locationCallBack;
    //Google's API for location services. The majority of the app function comes from this class
    FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //give each UI variable a value
      tv_lat = findViewById(R.id.tv_lat);
      tv_lon = findViewById(R.id.tv_lon);
      tv_altitude = findViewById(R.id.tv_altitude);
      tv_accuracy = findViewById(R.id.tv_accuracy);
      tv_speed = findViewById(R.id.tv_speed);
      tv_sensor = findViewById(R.id.tv_sensor);
      tv_updates = findViewById(R.id.tv_updates);
      tv_address = findViewById(R.id.tv_address);
      sw_gps = findViewById(R.id.sw_gps);
      sw_locationupdates = findViewById(R.id.sw_locationsupdates);
      btn_newWaypoint = findViewById(R.id.btn_newWayPoint);
      btn_showWayPointList = findViewById(R.id.btn_showWayPointList);


      // set all properties of LocationRequest

        locationRequest = LocationRequest.create(); 

        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL());

        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL());

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback()
        {
          @Override
          public void onLocationResult(LocationResult locationResult)
          {
              super.onLocationResult(locationResult);

              //save the location

              updateUIValues(locationResult.getLastLocation());
          }





        };


       btn_newWaypoint.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            // get the gps location

            //add the new location to the global list
              MyApplication myApplication = (MyApplication)getApplicationContext();
              savedLocations = myApplication.getMyLocations();
              savedLocations.add(currentLocation);
          }


        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (sw_gps.isChecked())
                {
                    //most accurate - use GPS
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                }
                else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");
                }
            }
        });
        updateGPS();
    } // end onCreate method

private void startLocationUpdates()
{
    tv_updates.setText("Location is being tracked");
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    updateGPS();
}
    private void stopLocationUpdates()
    {
        tv_updates.setText("Location is NOT being tracked");
        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not tracking location");
        tv_address.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case PERMISSIONS_FINE_LOCATION:
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                updateGPS();
            } else {
                Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

    private void updateGPS()
    {
      //get permissions from the user to track GPS
        //get the current location from the fused client
        // update the UI - ie set all the properties in their associated text view items

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location)
                {
                   // we got permissions. Put the value of location. XXX into the UI Components.
                   updateUIValues(location);
                   currentLocation = location;
                }

            });
        }
        else
        {
            //permission not provided yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
             requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location)
    {
      //update all of the text view objects with a new location.
      tv_lat.setText(String.valueOf((location.getLatitude())));
      tv_lon.setText(String.valueOf((location.getLatitude())));
      tv_accuracy.setText(String.valueOf(location.getAccuracy()));
      if (location.hasAltitude()) {
        tv_altitude.setText(String.valueOf(location.getAltitude()));
     }
       else {
        tv_altitude.setText(("Not available"));
     }

     if (location.hasSpeed()) {
        tv_speed.setText((String.valueOf(location.getSpeed())));

      }
    else {
        tv_speed.setText(("Not available"));
      }

   Geocoder geocoder = new Geocoder(MainActivity.this);

try
{
List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
tv_address.setText(addresses.get(0).getAddressLine(0));



}
catch (Exception e)
{
tv_address.setText("Unable to get a street address");
}

    }

}