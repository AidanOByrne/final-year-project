package com.example.monitorme.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.monitorme.Fragment.Camera.CameraViewFragment;
import com.example.monitorme.Fragment.Camera.DisplayImageFragment;
import com.example.monitorme.Fragment.ChatListFragment;
import com.example.monitorme.Login.AuthenticationActivity;
import com.example.monitorme.Object.ChatObject;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;
import com.example.monitorme.maps.MapsActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;

/**
 * Main Activity, controller for the main fragments of the project.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    FragmentManager fm = getSupportFragmentManager();

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;


    FloatingActionButton fab, mapBtn;

    AppBarLayout mAppBar;
    int appBarHeight;

    TabLayout tabLayout;

    // Maps permissions
    //private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissions();

        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        Fresco.initialize(this);

        getUserInfo();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mAppBar = findViewById(R.id.appbar);
        mAppBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                appBarHeight = mAppBar.getMeasuredHeight(); //height is ready
                if(mSectionsPagerAdapter.getChatListFragment()!= null)
                    ((ChatListFragment) mSectionsPagerAdapter.getChatListFragment()).updatePaddingTop();

            }
        });


        tabLayout = findViewById(R.id.tabs);
        setCameraTabWidth();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition() == 0){
                    hideToolbar();
                    hideFab();
                    hideMapBtn();
                }else{
                    showToolbar();
                    showFab();
                    showMapBtn();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mViewPager.setCurrentItem(1);

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), FindUserActivity.class)));

        mapBtn = findViewById(R.id.mapBtn);

        mapBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MapsActivity.class)));

        //checkMapServices();
    }


    UserObject mUser;
    private void getUserInfo() {
        mUser = new UserObject(FirebaseAuth.getInstance().getUid());
        FirebaseDatabase.getInstance().getReference()
                .child("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUser.parseObject(dataSnapshot);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    public UserObject getUser() {
        return mUser;
    }

    void setCameraTabWidth(){
        LinearLayout layout = ((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(1));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.weight = 1.5f;
        layout.setLayoutParams(layoutParams);
    }

    private void hideToolbar() {
        mAppBar.animate().translationY(-appBarHeight);
        // Scale down animation
    }
    private void showToolbar() {
        mAppBar.animate().translationY(0);
    }
    void hideFab() {
        if(fab == null)
            return;
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                fab.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fab.startAnimation(shrink);
    }
    void hideMapBtn() {
        if(mapBtn == null)
            return;
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mapBtn.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mapBtn.startAnimation(shrink);
    }

    void showFab(){
        if(fab == null)
            return;
        fab.setVisibility(View.VISIBLE);
        ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        expand.setDuration(100);     // animation duration in milliseconds
        expand.setInterpolator(new AccelerateInterpolator());
        expand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fab.startAnimation(expand);
    }

    private void showMapBtn() {
        if(mapBtn == null)
            return;
        mapBtn.setVisibility(View.VISIBLE);
        ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        expand.setDuration(100);     // animation duration in milliseconds
        expand.setInterpolator(new AccelerateInterpolator());
        expand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mapBtn.startAnimation(expand);
    }

    public int getAppBarHeight() {
        return appBarHeight;
    }

    ArrayList<ChatObject> chatList = new ArrayList<>();
    public ArrayList<ChatObject> getChatList() {
        return chatList;
    }
    public void setChatList(ArrayList<ChatObject> chatList) {
        this.chatList = chatList;
    }

    Bitmap bitmap = null;
    public void setBitmapToSend(Bitmap bitmapToSend){
        this.bitmap = bitmapToSend;
    }
    public Bitmap getBitmapToSend() {
        return bitmap;
    }

    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    public void  dismissProgressDialog(){
        if(mDialog!=null)
            mDialog.dismiss();
    }


    public void clearBackStack(){
        while(fm.getBackStackEntryCount()>0)
            onBackPressed();
    }

    public void openEditProfileActivity(){
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("userObject", mUser);
        startActivity(intent);
    }
    public void openDisplayImageFragment(){
        fm.beginTransaction()
                .replace(R.id.fragmentHolder, DisplayImageFragment.newInstance(), "DisplayImageFragment")
                .addToBackStack(null)
                .commit();
    }
    public void openChooseReceiverFragment(){
        fm.beginTransaction()
                .replace(R.id.fragmentHolder, ChatListFragment.newInstance(false), "ChooseReceiverFragment")
                .addToBackStack(null)
                .commit();
    }
    private void logout(){
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_profile) {
            openEditProfileActivity();
            return true;
        }
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Fragment cameraViewFragment, chatListFragment, mapFragment;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    if (cameraViewFragment == null)
                        cameraViewFragment = new CameraViewFragment();
                    return cameraViewFragment;
                case 1:
                    if (chatListFragment == null)
                        chatListFragment = ChatListFragment.newInstance(true);
                    return chatListFragment;
                case 2:
                    if(mapFragment == null)
                        mapFragment = new Fragment();
                    return mapFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        public Fragment getChatListFragment() {
            return chatListFragment;
        }

        public Fragment getMapFragment(){ return mapFragment; }
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(fm.getBackStackEntryCount()==0)
            mViewPager.setCurrentItem(1);
    }

    // maps permissions
    /*
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //getChatrooms();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    //getChatrooms();
                    Log.d("Permission", "User has permission to use maps.");
                    Toast.makeText(this, "You have permission to make map requests", Toast.LENGTH_SHORT).show();
                }
                else{
                    getLocationPermission();
                    Toast.makeText(this, "You do not have permission to make map requests", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
    */

}
