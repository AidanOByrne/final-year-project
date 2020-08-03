package com.example.monitorme.Activity;
/*
References for project
https://www.programcreek.com/java-api-examples/?class=android.view.ViewTreeObserver&method=OnGlobalLayoutListener
https://developer.android.com/reference/android/view/animation/Interpolator
https://developer.android.com/reference/android/graphics/Bitmap
https://developer.android.com/guide/components/activities/tasks-and-back-stack
https://developers.google.com/maps/documentation/android-sdk/overview
https://developers.google.com/maps/documentation/android-sdk/map
https://firebase.google.com/docs/reference/android/com/google/firebase/auth/PhoneAuthProvider.OnVerificationStateChangedCallbacks
https://stackoverflow.com/questions/16670203/set-flash-mode-in-android
https://stackoverflow.com/questions/6068803/how-to-turn-on-front-flash-light-programmatically-in-android
https://www.youtube.com/watch?v=gkctZFUH-L4
https://developer.android.com/reference/android/telephony/TelephonyManager
https://willowtreeapps.com/ideas/android-fundamentals-working-with-the-recyclerview-adapter-and-viewholder-pattern
https://stackoverflow.com/questions/30615400/android-recyclerview-adapter-oncreateviewholder-working/30615637

 */
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

    // tag for log messages
    private static final String TAG = "MainActivity";
    // support fragment manager to be called later
    FragmentManager fm = getSupportFragmentManager();

    // pager adapter to be called later
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // page viewer
    private ViewPager mViewPager;

    // floating action buttons to be called later
    FloatingActionButton fab, mapBtn;

    // appbar layout
    AppBarLayout mAppBar;
    // height
    int appBarHeight;

    TabLayout tabLayout;

    // Maps permissions
    //private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // call get persmissions
        getPermissions();

        // initiliase one signal for noticaication sending
        OneSignal.startInit(this).init();
        //set subscription to the online library
        OneSignal.setSubscription(true);
        // set the notification key for the user so we can use it later to send notification when message sent to a user
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        // frseco image viewer initialise
        Fresco.initialize(this);

        // call method
        getUserInfo();

        // get toolbar from xml by id
        Toolbar toolbar = findViewById(R.id.toolbar);
        // set text color
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        // set the toolbar in xml file
        setSupportActionBar(toolbar);

        // assign support fragment manager to vsalue to be used
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // get page viewer by id from xml file
        mViewPager = findViewById(R.id.container);
        //set support fragment manager to page viewer
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // get app bar based on id from xml file
        mAppBar = findViewById(R.id.appbar);

        // setting tree observer that determines hight of each fragment, keeps evertyhting looking similar and consistant
        mAppBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                appBarHeight = mAppBar.getMeasuredHeight(); //height is ready
                if(mSectionsPagerAdapter.getChatListFragment()!= null)
                    ((ChatListFragment) mSectionsPagerAdapter.getChatListFragment()).updatePaddingTop();

            }
        });

        // find tab view by id in xml file
        tabLayout = findViewById(R.id.tabs);
        // call method
        setCameraTabWidth();
        // hides or shows the toolbar depending on where user is,
        // camera view has no tab bar chat view does
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition() == 0){
                    // hide toolbar
                    hideToolbar();
                    // hide floating action button
                    hideFab();
                    // hide map btn
                    hideMapBtn();
                }else{
                    // else show
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

        // set on page listener to change tab
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        // add on tab listenr to change page
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        //set current item to 1 which is the chat rooms page
        mViewPager.setCurrentItem(1);
         // get fab by id
        fab = findViewById(R.id.fab);

        // change page when clicked
        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), FindUserActivity.class)));

        // find map by id
        mapBtn = findViewById(R.id.mapBtn);

        // change when clicked
        mapBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MapsActivity.class)));

    }
    //declare user object
    UserObject mUser;
    private void getUserInfo() {
        // get user id
        mUser = new UserObject(FirebaseAuth.getInstance().getUid());
        //parse the data snap shot infor intp user object based on user id in users table
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

    // set camera width, called earlier
    void setCameraTabWidth(){
        // index value 0, used earlier on line 177 we set it to 1 to be chats screen
        // here we assing index value so it can be changed when clicked
        LinearLayout layout = ((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(1));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        // set width
        layoutParams.weight = 1.5f;

        // set layout prarams as layout params
        layout.setLayoutParams(layoutParams);
    }

    private void hideToolbar() {
        // when called it subtracts its hieght from the y values making it - the height a value below 0
        mAppBar.animate().translationY(-appBarHeight);
        // Scale down animation
    }
    // set the hieght back to nutral 0, so it visible
    private void showToolbar() {
        mAppBar.animate().translationY(0);
    }

    // hide floating action button when called
    void hideFab() {
        if(fab == null)
            return;
        // Scale down animation
        // shrink to not be visible
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator()); // decelerate shrink
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

        // set the shrink animation
        fab.startAnimation(shrink);
    }

    // copy of hide fab
    void hideMapBtn() {
        if(mapBtn == null)
            return;
        // Scale up animation
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

    // reverse of hide
    // we now allerate
    // reverse values
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
    // copy of show fab
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

    // getter
    public int getAppBarHeight() {
        return appBarHeight;
    }

    // new array list from chat object now
    ArrayList<ChatObject> chatList = new ArrayList<>();
    // return values from chat object save in array
    public ArrayList<ChatObject> getChatList() {
        return chatList;
    }
    // setter
    public void setChatList(ArrayList<ChatObject> chatList) {
        this.chatList = chatList;
    }
    // bitmap null
    Bitmap bitmap = null;
    public void setBitmapToSend(Bitmap bitmapToSend){
        this.bitmap = bitmapToSend;
    }
    public Bitmap getBitmapToSend() {
        return bitmap;
    }

    // create dialog to be called
    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    // get ride of dialog
    public void  dismissProgressDialog(){
        if(mDialog!=null)
            mDialog.dismiss();
    }
    // clears the fragment manager to reposition the user in the stakc
    // meaning it will change the stack and screen
    public void clearBackStack(){
        while(fm.getBackStackEntryCount()>0)
            onBackPressed();
    }

    // open new activity
    // edit profile actvity
    public void openEditProfileActivity(){
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("userObject", mUser);
        startActivity(intent);
    }
    //
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
    // logout of application
    private void logout(){
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return;
    }

    // inflater for menu in res file
    // holds the top tab and the drop down menu with edit profile
    // and logout button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    // calls the right method based on item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // edit profile
        if (id == R.id.action_edit_profile) {
            openEditProfileActivity();
            return true;
        }
        // logout
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // used to get the correct page,
    // map is not used but i kept it in
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Fragment cameraViewFragment, chatListFragment, mapFragment;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                // assign positions based on cases to be called
                // map fragment not in use
                // called with floating action button
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
        // count of items
        public int getCount() {
            return 2;
        }

        // retrun chat list fragment
        public Fragment getChatListFragment() {
            return chatListFragment;
        }

        // not used
        public Fragment getMapFragment(){ return mapFragment; }
    }

    // get permissions based on sdk version
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    @Override
    // changes the page viewer to home screen or to close the screen depending on where the user is
    public void onBackPressed() {
        super.onBackPressed();

        if(fm.getBackStackEntryCount()==0)
            mViewPager.setCurrentItem(1);
    }

    // maps permissions
    // thsi is all old code that i might use later
    //
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
