package com.example.monitorme.maps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.monitorme.Activity.MainActivity;
import com.example.monitorme.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    // create google maps
    private GoogleMap mMap;

    // tag for log
    private static final String TAG = "MapsActivity";

    // image view button
    ImageView mBack;

    // permissions for location tracking
    final static int PERMISSION = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_FINE_LOCATION};

    // make marker, location manager, string, database reference for later
    MarkerOptions mo;
    Marker marker;
    LocationManager locationManager;

    String userId = "";

    FirebaseAuth mAuth;
    FirebaseDatabase mDB = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        // set location manager to get the location from systems
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // set title for marker and latitude longitude for later
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My current location");

        // depends on what sdk is used in the project check build.gradle
        // then request permission for for location tracking
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION);
        } else requestLocation();
        if (!isLocationEnabled())
            // show alert
            showAlert(1);

        // back button
        mBack = findViewById(R.id.back);

        // bring you back to main page
        mBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });


    }

    // make alerts and assign the message, title and btn text depending on cases
    private void showAlert(int i) {
        String message, title, btnText;
        if (i == 1) {
            message = "Your location settings is set to 'Off'. \n Please enable to use this section";
            title = "Enable Location";
            btnText = "Location Settings";
        } else {
            message = "Please allow the app to access location";
            title = "Permission access";
            btnText = "Grant";
        }

        // create dialog for alert dialog
        // set cancelable so it gets ride of it
        // set title, set message, set button and create intent to the button
        // request permissions on button
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (i == 1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        } else
                            requestPermissions(PERMISSIONS, PERMISSION);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        // show dialog
        dialog.show();
    }

    // checks if location is enabled
    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
    }

    // request location
    private void requestLocation() {
        // create criteria
        Criteria criteria = new Criteria();
        // set accuracy to the criteria
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // set power
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        // attach location manager with criteria
        String provider = locationManager.getBestProvider(criteria, true);
        // auto generated when request permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // set intervals to location listener for updates
        locationManager.requestLocationUpdates(provider, 100, 10, (android.location.LocationListener) this);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    // check if permission was granted or not
    private boolean isPermissionGranted() {
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.v("my Log", "Permission granted");
            return true;
        }else{
            Log.v("my Log", "Permission denied");
            return false;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    // when map loading get the marker and map
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        marker = mMap.addMarker(mo);

        /*
        This is default code and some of my own,
        the default code when you make a map function sets a marker on sydney,
        i took this idea and made my own marker that follows the user.

        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if(locationManager != null){
            double latitude = Location.getLatitude();
            double longitude= Location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here" + latitude + longitude);
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
            googleMap.addMarker(markerOptions);
            Log.v(TAG, "onMapReady: This is the position " +latitude +longitude);
        }

         */
    }

    // on location change get position based on longitude or latitude
    // set zoom level to auto zoom on location
    // create new reference and save the vales to the Database
    @Override
    public void onLocationChanged(Location location) {
        LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        marker.setPosition(myCoordinates);
        float zoomLevel = 15.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, zoomLevel));

        final DatabaseReference mRef = mDB.getReference().child("user").child(userId).child("location");

        mRef.setValue(myCoordinates);

    }

    // auto generated but dont need to be used
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}