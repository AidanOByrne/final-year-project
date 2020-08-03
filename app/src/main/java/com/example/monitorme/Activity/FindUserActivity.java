package com.example.monitorme.Activity;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorme.Adapter.UserListAdapter;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;
import com.example.monitorme.Utils.CountryToPhonePrefix;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FindUserActivity extends AppCompatActivity {

    // create recycler view variables
    // array list
    // adapter
    // layout manager
    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    // create array lists
    ArrayList<UserObject> userList, contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        // new array list to name
        contactList= new ArrayList<>();
        userList= new ArrayList<>();

        // get image views by id
        ImageView mCreate = findViewById(R.id.create);
        ImageView mBack = findViewById(R.id.back);
        // set on click listener then call necessary method
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call create chat method
                createChat();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call finish method
                finish();
            }
        });

        // call other methods on activity create
        initializeRecyclerView();
        getContactList();
    }

    public void createChat(){
        // get key save to string value
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        // get necessary database references
        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");

        // hash map to put the values and there key  to database
        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("timestamp", ServerValue.TIMESTAMP);
        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);

        // boolean value to be used later
        Boolean validChat = false;
        //connected to userobject array list connected to the file object / UserObject
        // for each user in user list that is selected
        // change boolean to true value
        // put users + user id into new chat map
        // set value of the time stamp
        for(UserObject mUser : userList){
            if(mUser.getSelected()){
                validChat = true;
                newChatMap.put("users/" + mUser.getUid(), true);
                userDb.child(mUser.getUid()).child("chat").child(key).setValue(ServerValue.TIMESTAMP);
            }
        }

        // check if boolean value has changed
        // if so now we update the table with the values gotten above in new chat map
        // set time stamp
        if(validChat){
            chatInfoDb.updateChildren(newChatMap);
            userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(ServerValue.TIMESTAMP);
        }
        finish();
    }

    private void getContactList(){

        // declare country prefix
        String ISOPrefix = getCountryISO();

        // cursor provides read write access to a databse query

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()){
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(!String.valueOf(phone.charAt(0)).equals("+"))
                phone = ISOPrefix + phone;

            UserObject mContact = new UserObject("", name, phone, " ", " ");
            contactList.add(mContact);
            getUserDetails(mContact);
        }
    }
    private void getUserDetails(UserObject mContact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String  phone = "",
                            name = "",
                            image = "",
                            status = "Hi there! I'm on whatsApp Clone.";
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        if(childSnapshot.child("phone").getValue()!=null)
                            phone = childSnapshot.child("phone").getValue().toString();
                        if(childSnapshot.child("name").getValue()!=null)
                            name = childSnapshot.child("name").getValue().toString();
                        if(childSnapshot.child("image").getValue()!=null)
                            image = childSnapshot.child("image").getValue().toString();
                        if(childSnapshot.child("status").getValue()!=null)
                            status = childSnapshot.child("status").getValue().toString();


                        UserObject mUser = new UserObject(childSnapshot.getKey(), name, phone, image, status);
                        if (name.equals(phone))
                            for(UserObject mContactIterator : contactList){
                                if(mContactIterator.getPhone().equals(mUser.getPhone())){
                                    mUser.setName(mContactIterator.getName());
                                }
                            }

                        userList.add(mUser);
                        mUserListAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getCountryISO(){
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso()!=null)
            if (!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso = telephonyManager.getNetworkCountryIso().toString();

        return CountryToPhonePrefix.getPhone(iso);
    }


    private void initializeRecyclerView() {
        mUserList= findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                LinearLayout.VERTICAL);
        mUserList.addItemDecoration(dividerItemDecoration);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(this, userList);
        mUserList.setAdapter(mUserListAdapter);
    }
}