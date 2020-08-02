package com.example.monitorme.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.monitorme.Adapter.MediaAdapter;
import com.example.monitorme.Adapter.MessageAdapter;
import com.example.monitorme.Object.ChatObject;
import com.example.monitorme.Object.MessageObject;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;
import com.example.monitorme.Utils.SendMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    // here we declare the adapters involved in this activity
    // the adapters

    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    // declaration of the array lists involved in this activity
    // message list is connected to the message object
    // message object has the associated getters and setters needed for this activity.
    // will describe in detail later
    ArrayList<MessageObject> messageList;

    // key list is associated with getting key values from the database when called
    // will see the uasage later
    List<String> keyList = new ArrayList<String>();

    // declaration connection to the chat object.
    ChatObject mChatObject;

    // database reference to connect to the database later when called correctly.
    DatabaseReference mChatMessagesDb, mChatInfoDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // declares what xml file we are linked to in this activity

        // this line of code changes the chat object into state into that of a byte stream form
        // this can be then reformed or reverted into a copy of the object.
        // Deserialization is the form of changing it back into the copy of an object
        mChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");

        // here we use the database references to get the value of values we want in the database.
        // firstly we get the messages associated with the current chat that is located in the chat table .. reading it backwards
        // second we get the information associated with the current chat id again in the chat table
        // we are using the database references we declared earlier
        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");
        mChatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("info");

        // declaring different action buttons and image views we will use later to set on click listeners to
        // these are declared as constance and will not change their value

        // on click listeners allow "actions" to happen when these buttons are clicked
        // we declare the associated object e.g. Floating action button, image view, text view, button.
        // these objects are the same as those in activity_chat.xml
        // we then use the find view by id to grab the correct item from the xml page based on id
        FloatingActionButton mSend = findViewById(R.id.send);
        ImageView mAddMedia = findViewById(R.id.addMedia);
        ImageView mConfig = findViewById(R.id.config);
        ImageView mBack = findViewById(R.id.back);

        // now we call the necessary image views and action buttons to set on click listeners to them.
        // each on click listener will do something associated with that button or image view
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            // this on click listener calls the function sendMessage();
            public void onClick(View v) {
                sendMessage();
            }
        });
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            // this on click listener calls the function openGallery();
            public void onClick(View v) {
                openGallery();
            }
        });
        mConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            // this on click listener calls the function openConfig();
            public void onClick(View v) {
                openConfig();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            // this on click listener calls the function finish();
            public void onClick(View v) {
                finish();
            }
        });

        // here we are calling different functions/methods that will start when this view is "created" this is why we call them in the onCreate function
        initializeMessage();
        initializeMedia();
        getChatMessages();
        getChatInfo();
    }

    private void getChatInfo() {
        // here we are adding an add value event listener
        mChatInfoDb.addValueEventListener(new ValueEventListener() {
            @Override
            // this event listener will listen to see if any data has changed
            // if it has it will parse the information gained
            // from taking a data snapshot of the database based on the database reference we created earlier ... mChatInfoDb
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatObject.parseObject(dataSnapshot);
                // we then call the update chat info view function
                updateChatInfoViews();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // this is automatically created when u create the on data change ..
                // no need for it but later stages handy to have
            }
        });

    }
    private void getChatMessages() {
        // this event listener listens out for changes in location based on the given database reference ... mChatMessageDb
        mChatMessagesDb.addChildEventListener(new ChildEventListener() {
            @Override
            // this method is called when a new child is added to the databse reference
            // meaing if s new message comes in it will take the message and put it into the message list aka the message list array
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // we check to see if a datasnapshot exists
                if(dataSnapshot.exists()){
                    // we parse the data snap shot to be abel to store it in a object
                    MessageObject mMessage = new MessageObject();
                    mMessage.parseObject(dataSnapshot);
                    // we then add it to the message list array to be used
                    messageList.add(mMessage);

                    // we get the key values of the dat snap shot and store them in the key list arrays
                    keyList.add(dataSnapshot.getKey());
                    // we state we want to start the chat layout manager at the last added messsage
                    // this is for the user so they dont have to scroll thought the whole chat history
                    mChatLayoutManager.scrollToPosition(messageList.size()-1);
                    // this line of code is responsible for allowing the list to constantly be refreshed
                    // so the most recent view is always there
                    mChatAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // allows the deleting of values based on key value
                // removes based on the key value
                int index = keyList.indexOf(dataSnapshot.getKey());
                messageList.remove(index);
                keyList.remove(index);
                mChatAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // is in charge if sending the message to the associated user
    private void sendMessage(){
        // gets the text based ont he id message input
        EditText mMessage = findViewById(R.id.messageInput);

        // declare a simple string with nothing attached
        String message = "";

        // if the edit text is not equal to null we will save the value to the string we just created
        if(!mMessage.getText().toString().isEmpty())
            message = mMessage.getText().toString();

        // uses the send message java class in the utils folder
        new SendMessage(mChatObject, true, mediaUriList, null, message);

        // set text to null
        mMessage.setText(null);
        //clear the list
        mediaUriList.clear();
        // calls mMediaAdapter on data change
        mMediaAdapter.notifyDataSetChanged();


    }

    private void initializeMessage() {
        // create array list for the messages
        messageList = new ArrayList<>();
        // get the recylcer view by id
        RecyclerView mChat = findViewById(R.id.messageList);
        // turn of scrolling on the page
        mChat.setNestedScrollingEnabled(false);
        // set to not have a set size so it fits all phone types
        mChat.setHasFixedSize(false);
        // creates a list view layout
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        // setting the list view to the chat screen
        mChat.setLayoutManager(mChatLayoutManager);
        // calling the message adapter and attaching the chat object and the message lust to the chat adapter
        mChatAdapter = new MessageAdapter(this, mChatObject, messageList);
        // setting the adapter to the chat
        mChat.setAdapter(mChatAdapter);
    }

    void updateChatInfoViews(){
        // getting the image view and text view by id
        ImageView mImage = findViewById(R.id.chatImage);
        TextView mName = findViewById(R.id.chatName);

        // if name does not equal nothing then set text to the name the name of the user
        if(!mChatObject.getName().equals(""))
            mName.setText(mChatObject.getName());
        // else get name by users and set it to the text
        else
            mName.setText(mChatObject.getNameByUsers());

        // if the application is nto equal to null then we get the image saved in ic_user ... default image
        if(getApplicationContext()!=null){
            Glide.with(getApplicationContext())
                    .load(getResources().getDrawable(R.drawable.ic_user))
                    // turn into a circular image
                    .apply(RequestOptions.circleCropTransform().circleCrop())
                    // set into the chat image we got earlier
                    .into(mImage);
            // if it equals nothing keep the default image
            if(!mChatObject.getImage().equals(""))
                Glide.with(getApplicationContext())
                        .load(mChatObject.getImage())
                        .apply(RequestOptions.circleCropTransform().override(24, 24))
                        .into(mImage);
            else{
                // else get the array list to get the image the user has saved
                if (mChatObject.getUserObjectArrayList().size() == 2) {
                    for (UserObject mUser :  mChatObject.getUserObjectArrayList()){
                        // we get the users saves image and change it into a circular image
                        if (!mUser.getUid().equals(FirebaseAuth.getInstance().getUid()))
                            Glide.with(getApplicationContext())
                                    .load(mUser.getImage())
                                    .apply(RequestOptions.circleCropTransform().circleCrop())
                                    .into(mImage);
                    }
                }
            }
        }
    }

    // int vvariable
    int PICK_IMAGE_INTENT = 1;
    //array list to hold usi lits from firebase
    ArrayList<String> mediaUriList = new ArrayList<>();

    // this is same as initalising the message
    private void initializeMedia() {
        mediaUriList = new ArrayList<>();
        // getting media list by id
        RecyclerView mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        RecyclerView.LayoutManager mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
    }

    // open the chat configuration page and
    private void openConfig() {
        Intent intent = new Intent(this, ChatConfigActivity.class);
        intent.putExtra("chatObject", mChatObject);
        startActivity(intent);
    }
    // opening the devices gallery
    private void openGallery() {
        // declare enw intent
        Intent intent = new Intent();
        // decvlare as image
        intent.setType("image/*");
        // allow multiple images to be sent
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // set the action of getting content
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // start the activity of picking images
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }


    @Override
    // this is to check if the result for the image is okay
    // its a series of checks and then we change the to uri to a string to be then added to the media uri list
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_IMAGE_INTENT){
                if(data.getClipData() == null){
                    mediaUriList.add(data.getData().toString());
                }else{
                    for(int i = 0; i < data.getClipData().getItemCount(); i++){
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                mMediaAdapter.notifyDataSetChanged();
            }
        }
    }
}