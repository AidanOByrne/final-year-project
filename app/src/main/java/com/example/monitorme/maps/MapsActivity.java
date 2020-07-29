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

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
//import com.google.android.gms.location.LocationListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    private static final String TAG = "MapsActivity";

    ImageView mBack;

    final static int PERMISSION = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_FINE_LOCATION};
    MarkerOptions mo , fo;
    Marker marker, fMarker;
    LocationManager locationManager;

    String userId = "",
            chatId = "";


    FirebaseAuth mAuth;
    FirebaseDatabase mDB = FirebaseDatabase.getInstance();

    private DatabaseReference mLocationDB;

    final StringBuffer buffer = new StringBuffer("");

    Object userKeys = new Object();



    /*
    FirebaseAuth mAuth;
    DatabaseReference mUserDatabase;
    mAuth = FirebaseAuth.getInstance();
    userId = mAuth.getCurrentUser().getUid();

    mUserDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My current location");
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION);
        } else requestLocation();
        if (!isLocationEnabled())
            showAlert(1);

        //back button
        mBack = findViewById(R.id.back);

        mBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });


    }

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
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
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
        locationManager.requestLocationUpdates(provider, 100, 10, (android.location.LocationListener) this);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        marker = mMap.addMarker(mo);

        friendsLocation();


        /*
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

    private void friendsLocation() {

        final DatabaseReference fRef = mDB.getReference().child("user").child(userId).child("chat");
        Log.v("CoUpdates", "first reference : " + fRef);

        fRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot chatId : snapshot.getChildren()){
                    //Log.v( "CoUpdates", "ChatID ?? " + chatId);

                    String key = chatId.getKey();
                    //Log.v( "CoUpdates", "ChatKey ?? " + key);
                    final DatabaseReference cfRef = (DatabaseReference) mDB.getReference().child("chat").child(key).child("info").child("users");

                    List<DataSnapshot> userList = new ArrayList<>();

                    cfRef.addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //Log.v("CoUpdates", "The snapshot " + snapshot);

                            //Log.v("CoUpdates", "The snapshot value " + snapshot.getValue());

                            userList.add(snapshot);
                            //Log.v("CoUpdates", "The user list  " + userList);

                            for(DataSnapshot userS : snapshot.getChildren()){

                                //Log.v("CoUpdates", "The user snapshot stage " + userS);

                                String s = userS.getKey().replace("[", "").replace("]", "");

                                //Log.v("CoUpdates", "The string " + s);

                                //String regex = "";
                                //String regex1 = "";
                                Iterable userI = Collections.singleton(Objects.requireNonNull(userS.getKey()).replace(userId, ""));
                                //Log.v("CoUpdates", "The user list first stage " + userI);

                                List<String> userListI = new ArrayList<>();
                                userListI.add(userI.toString().replace("[", "").replace("]", ""));

                                boolean equals = s.equals(userId);

                                final DatabaseReference fUser = mDB.getReference().child("user").child(String.valueOf(userListI).replace("[", "").replace("]", "")).child("location");
                                //Log.v("CoUpdates", "The user list second stage " + fUser);

                                fUser.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Log.v("CoUpdates" , "Snapshot of the data " + snapshot);
                                        if("location"!= null){
                                            DatabaseReference newLat = fUser.child("latitude");
                                            Log.v("CoUpdates", "Latitude save " + newLat);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                /*
                                userList.remove(userId);
                                for(int i = 0; i < userList.size(); i++){

                                }
                                */

                                //Log.v("CoUpdates", "The user list second stage " + userI);
                                //Log.v("CoUpdates", "The user list fourth stage " + s);
                                //Log.v("CoUpdates", "The user list fifth stage " + userListI);

                                //userList.add(userS);
                                //userList.remove(userId);
                                //Log.v("CoUpdates", "The user list sixth three " + userList);


                                if(snapshot.getKey().equals("users")){

                                    buffer.append(snapshot.getValue());
                                    //Log.v("CoUpdates", "The snapshot key and value " + userS);



                                    //Log.v("CoUpdates", "The snapshot key " + snapshot.getChildrenCount());


                                    //DatabaseReference userK = mDB.getReference().child("user").child(userKeys).child("location");
                                    //Log.v("CoUpdates", "The snapshot key " + userK);

                                    /*
                                    userK.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            //Log.v("CoUpdates", "The snap shot of location : " + snapshot);
                                            for(DataSnapshot userL : snapshot.getChildren()) {

                                                //Log.v("CoUpdates", "The snap shot of latitude and longitude : " + snapshot);
                                                //Log.v("CoUpdates", "The snap shot of L&L : " + userL);
                                                


                                                if (snapshot.getKey().equals("users")) {

                                                }

                                            }
                                            //LatLng latL = (LatLng) snapshot.getValue();
                                            //Log.v("CoUpdates", "The snap shot of location : " + latL);


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    */

                                    //LatLng

                                    /*
                                    fMarker.setPosition(userKeys);

                                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(userKeys));

                                    fMarker = mMap.addMarker(fo);


                                    /*
                                    int i = 0;
                                    final DatabaseReference userKeyLocation = mDB.getReference().child("user").child(userKeys[i]).child("location");
                                    Log.v("CoUpdates", "The reference for the location of the users" + userKeyLocation);


                                     */
                                }

                            }

                            /*
                            String mUserID = buffer.toString();
                            userList.add(mUserID);
                            for(int i = 0; i<userList.size(); i++){
                                Log.v("CoUpdates" , "This is the array users key " + userList.get(i));
                            }
                            //Log.v("CoUpdates", "The snapshot value " + mUserID);


                            Object userO = snapshot.getValue();


                            /*
                            long child = snapshot.getChildrenCount();
                            boolean child1 = snapshot.hasChild(userId);

                            userList.clear();

                            userList.remove(userId);

                            Log.v("CoUpdates", "The snapshot user list " + userList);

                            //Log.v("CoUpdates", "The snapshot user list " + userLis);

                            Log.v("CoUpdates", "The child snapshot " + child);
                            Log.v("CoUpdates", "The child snapshot user id " + child1);

                            final Object value = snapshot.getValue();
                            Log.v("CoUpdates", "The snapshot user value " + value);

                            /*
                            int i = 0;

                            Spliterator<DataSnapshot> valueI = snapshot.getChildren().spliterator();
                            Log.v("CoUpdates", "Spliterator " + valueI);

                            Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                            int length = (int) snapshot.child("value").getChildrenCount();
                            String[] sampleString = new String[length];

                            while(i < length){
                                sampleString[i] = iterator.next().getValue().toString();
                                Log.v("CoUpdates", "The iterator values " + sampleString[i]);
                                Log.v("CoUpdates", "The iterator values " + length);
                                i++;
                            }
                            /*
                            userList.clear();
                            userList.add(snapshot);
                            Log.v("CoUpdates", "The snapshot " + snapshot);
                            Object usersI = snapshot.child("info").child("users").getValue();
                            Log.v("CoUpdates" , "Fuck me " + usersI);
                            Log.v("CoUpdates" , "Fuck me 1 " + userList.size());



                            /*
                            Log.v("CoUpdates", "The info " + usersI);
                            //String[] x = usersI.split(“(....)=(?=);
                            String myRegex = "[{,=}]";

                            userArray.add(usersI);

                            Log.v("CoUpdates", "User array " + userArray.size());
                            Log.v("CoUpdates", "User array " + userArray);

                            for(int i = 0; i < userArray.size(); i++){
                                userArray.remove(myRegex);
                                //userArray.r
                            }

                            //userArray.size();
                            //String separate = ((String) usersI).replace("=true" , "");
                            //String separate1 = ((String) separate).replace("=true," , "");


                            //Log.v("CoUpdates" , "This is new object " + separate2);
                            //Object sUser = separate.subSequence(1, 28);
                            //Log.v("CoUpdates" , "This is new object " + sUser);

                            /*
                            for (int i =0 ; i < separate.length; i++){
                                Log.v("CoUpdates", "The snapshot " + separate[i] );
                                final DatabaseReference lRef = mDB.getReference().child("user").child(separate[i]);
                                Log.v("CoUpdates", "The snapshot " + lRef );
                                //for (int j = 0; j< separate.length; j++){

                                //}
                            }

                            final DatabaseReference lRef = mDB.getReference().child("user");

                            /*
                            for (DataSnapshot cUsers : snapshot.child("users")){
                                Log.v("CoUpdates", "Chat Info " + cUsers);
                                ListIterator userD = (ListIterator) snapshot.getChildren();
                                //Iterable<DataSnapshot> userA = snapshot.child("users");
                                //ArrayList cUsersValues = (ArrayList) cUsers.getChildren();


                                Log.v("CoUpdates", "Chat Info " + userD);
                                //Map fItems = cUsersValues.

                                //ArrayList usersKey = (ArrayList) cUsers.getChildren();
                                //Log.v("CoUpdates", "Users in chat " + cUsersValues);



                                //Object usersValue = snapshot.;
                                //Log.v("CoUpdates", "Users value in chat " + usersValue);
                                //final DatabaseReference usersValue = mDB.getReference().child()
                            }

                             */
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        final DatabaseReference fRef1 = mDB.getReference().child("chat").child(userId);

        //Log.v("CoUpdates", "this is the query we are trying to make " + fRef + " " + fRef1);
        Query fQuery = fRef.limitToFirst(20);

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //HashMap<Double> data = (HashMap<Double>) snapshot.getValue();
                //double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("latitude").getValue()).toString());
                //double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("longitude").getValue()).toString());
                //LatLng location = new LatLng(latitude, longitude);

                //String chatID = String.valueOf(fRef.getKey());
                //Log.v("CoUpdates", "chatId  " + fRef);
                //Log.v("CoUpdates", "chatId query " + fQuery);

                //Log.v("CoUpdates", "This is the value of something " + location);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*
        fRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //LatLng fCoordinates = LatLng.valueOf(snapshot.getValue());
             //   Query mQuery = mDB.
                String childValue = String.valueOf(snapshot.getValue());
                String cObject = String.valueOf(snapshot.toString());
                HashMap<String, Double> data = (HashMap<String, Double>) snapshot.getValue();
                double latitude = data.get("latitude");

                Log.v("CoUpdates", "This is the value of something " + childValue + latitude);

                if (snapshot.getValue() != null) {
                    final DatabaseReference fRef2 = mDB.getReference().child(childValue);
                    final DatabaseReference fRef4 = mDB.getReference().child("chat").child("users");

                    Log.v("CoUpdates", "General " + fRef4);

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

         */


    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        marker.setPosition(myCoordinates);
        float zoomLevel = 15.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, zoomLevel));

        //Log.v(" CoUpdates" , "Coordinates " + myCoordinates);

        //Log.v("CoUpdates", "These coordinates are being uploaded to firebase " + myCoordinates);

        // save location into the current user section
        final DatabaseReference mRef = mDB.getReference().child("user").child(userId).child("location");

        mRef.setValue(myCoordinates);


        //friends location


        /// add them to firebase
        //mAuth = FirebaseAuth.getInstance();
        //userId = mAuth.getCurrentUser().getUid();

        /*

        final DatabaseReference fRef = mDB.getReference().child("user").child(userId).child("chat");

        fRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //LatLng fCoordinates = LatLng.valueOf(snapshot.getValue());
                //   Query mQuery = mDB.
                String childValue = String.valueOf(snapshot.getValue());
                Log.v("CoUpdates", "This is the value of something " + childValue);


                if (snapshot.getValue() != null) {
                    final DatabaseReference fRef2 = mDB.getReference().child(childValue);

                    final DatabaseReference mRef = mDB.getReference().child("chat").child(childValue).child("location");

                    mRef.setValue(myCoordinates);

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

         */


    }

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