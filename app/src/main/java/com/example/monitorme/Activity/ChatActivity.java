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

    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    ArrayList<MessageObject> messageList;
    List<String> keyList = new ArrayList<String>();

    ChatObject mChatObject;

    DatabaseReference mChatMessagesDb, mChatInfoDb;

    // edit/ delete variables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");

        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");
        mChatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("info");

        FloatingActionButton mSend = findViewById(R.id.send);
        ImageView mAddMedia = findViewById(R.id.addMedia);
        ImageView mConfig = findViewById(R.id.config);
        ImageView mBack = findViewById(R.id.back);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        mConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openConfig();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initializeMessage();
        initializeMedia();
        getChatMessages();
        getChatInfo();
    }

    private void getChatInfo() {
        mChatInfoDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatObject.parseObject(dataSnapshot);
                updateChatInfoViews();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void getChatMessages() {
        mChatMessagesDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    MessageObject mMessage = new MessageObject();
                    mMessage.parseObject(dataSnapshot);

                    messageList.add(mMessage);
                    keyList.add(dataSnapshot.getKey());
                    mChatLayoutManager.scrollToPosition(messageList.size()-1);
                    mChatAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
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

    private void sendMessage(){
        EditText mMessage = findViewById(R.id.messageInput);

        String message = "";

        if(!mMessage.getText().toString().isEmpty())
            message = mMessage.getText().toString();

        new SendMessage(mChatObject, true, mediaUriList, null, message);

        mMessage.setText(null);
        mediaUriList.clear();
        mMediaAdapter.notifyDataSetChanged();


    }

    private void initializeMessage() {
        messageList = new ArrayList<>();
        RecyclerView mChat = findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(this, mChatObject, messageList);
        mChat.setAdapter(mChatAdapter);
    }

    void updateChatInfoViews(){

        ImageView mImage = findViewById(R.id.chatImage);
        TextView mName = findViewById(R.id.chatName);

        if(!mChatObject.getName().equals(""))
            mName.setText(mChatObject.getName());
        else
            mName.setText(mChatObject.getNameByUsers());

        if(getApplicationContext()!=null){
            Glide.with(getApplicationContext())
                    .load(getResources().getDrawable(R.drawable.ic_user))
                    .apply(RequestOptions.circleCropTransform().circleCrop())
                    .into(mImage);
            if(!mChatObject.getImage().equals(""))
                Glide.with(this)
                        .load(mChatObject.getImage())
                        .apply(RequestOptions.circleCropTransform().override(24, 24))
                        .into(mImage);
            else{
                if (mChatObject.getUserObjectArrayList().size() == 2) {
                    for (UserObject mUser :  mChatObject.getUserObjectArrayList()){
                        if (!mUser.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            Glide.with(this)
                                    .load(mUser.getImage())
                                    .apply(RequestOptions.circleCropTransform().circleCrop())
                                    .into(mImage);
                        }
                    }
                }
            }
        }
    }


    int PICK_IMAGE_INTENT = 1;
    ArrayList<String> mediaUriList = new ArrayList<>();

    private void initializeMedia() {
        mediaUriList = new ArrayList<>();
        RecyclerView mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        RecyclerView.LayoutManager mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
    }

    private void openConfig() {
        Intent intent = new Intent(this, ChatConfigActivity.class);
        intent.putExtra("chatObject", mChatObject);
        startActivity(intent);
    }
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }


    @Override
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