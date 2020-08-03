package com.example.monitorme.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.monitorme.Activity.MainActivity;
import com.example.monitorme.Adapter.ChatListAdapter;
import com.example.monitorme.Object.ChatObject;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class ChatListFragment extends Fragment {

    // declare variables
    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;

    ArrayList<ChatObject> chatList = new ArrayList<>();
    ArrayList<UserObject> userList = new ArrayList<>();

    View view;
    Boolean started = false;
    boolean mainTab;

    AppBarLayout mAppBar;

    public ChatListFragment() {
    }

    public static ChatListFragment newInstance(boolean mainTab) {
        ChatListFragment fragment = new ChatListFragment();

        Bundle args = new Bundle();
        args.putBoolean("main", mainTab);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatList = new ArrayList<>();

        mainTab = getArguments().getBoolean("main", true);

        if(mainTab)
            getUserChatList();
        else
            chatList = ((MainActivity)getActivity()).getChatList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_main, container, false);
        else
            container.removeView(view);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(started)
            return;

        started = true;

        initializeRecyclerView();
        mAppBar = view.findViewById(R.id.appbar);
        if(mainTab)
            mAppBar.setVisibility(View.GONE);

        ImageView mBack = view.findViewById(R.id.back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).onBackPressed();
            }
        });

    }

    private void getUserChatList(){
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");

        mUserChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        ChatObject mChat = new ChatObject(childSnapshot.getKey());
                        mChat.setTimestamp(Long.parseLong(childSnapshot.getValue().toString()));
                        boolean  exists = false;
                        for (ChatObject mChatIterator : chatList){
                            if (mChatIterator.getChatId().equals(mChat.getChatId()))
                                exists = true;
                        }
                        if (exists){
                            Collections.sort(chatList);
                            continue;
                        }

                        chatList.add(mChat);
                        Collections.sort(chatList);

                        getChatData(mChat.getChatId());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("info");
        mChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String  chatId = "",
                            lastMessage = "Send Your First Message",
                            timestamp = "0",
                            image = "",
                            name = "";

                    if(dataSnapshot.child("id").getValue() != null)
                        chatId = dataSnapshot.child("id").getValue().toString();
                    if(dataSnapshot.child("lastMessage").getValue() != null)
                        lastMessage = dataSnapshot.child("lastMessage").getValue().toString();
                    if(dataSnapshot.child("timestamp").getValue() != null)
                        timestamp = dataSnapshot.child("timestamp").getValue().toString();
                    if(dataSnapshot.child("image").getValue() != null)
                        image = dataSnapshot.child("image").getValue().toString();
                    if(dataSnapshot.child("name").getValue() != null)
                        name = dataSnapshot.child("name").getValue().toString();


                    for(DataSnapshot userSnapshot : dataSnapshot.child("users").getChildren()){
                        for(ChatObject mChat : chatList){
                            if(mChat.getChatId().equals(chatId)){
                                mChat.setLastMessage(lastMessage);
                                mChat.setImage(image);
                                mChat.setTimestamp(Long.parseLong(timestamp));
                                mChat.setName(name);

                                UserObject mUser = new UserObject(userSnapshot.getKey());
                                getUserData(mUser);
                                boolean exists = false;
                                for (UserObject mUserIt : userList) {
                                    if(mUserIt.getUid().equals(mUser.getUid())){
                                       exists = true;
                                    }
                                }
                                if(!exists){
                                }

                                if(!mChat.getUserObjectArrayList().contains(mUser)){
                                    mChat.addUserToArrayList(mUser);
                                }else{
                                    if(mChatListAdapter!=null)
                                        mChatListAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                    }

                    Collections.sort(chatList);
                    if(mChatListAdapter!=null)
                        mChatListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getUserData(UserObject mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getUid());
        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject mUser = new UserObject(dataSnapshot.getKey());

                if(dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());
                if(dataSnapshot.child("name").getValue() != null)
                    mUser.setName(dataSnapshot.child("name").getValue().toString());
                if(dataSnapshot.child("image").getValue() != null)
                    mUser.setImage(dataSnapshot.child("image").getValue().toString());

                for(ChatObject mChat : chatList){
                    for (UserObject mUserIt : mChat.getUserObjectArrayList()){
                        if(mUserIt.getUid().equals(mUser.getUid())){

                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                            mUserIt.setName(mUser.getName());
                            mUserIt.setImage(mUser.getImage());

                            if(mChatListAdapter!=null)
                                mChatListAdapter.notifyItemChanged(chatList.indexOf(mChat));
                        }
                    }
                }
                ((MainActivity)Objects.requireNonNull(getActivity())).setChatList(chatList);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void updatePaddingTop(){
        mChatList.setPadding(0,((MainActivity)getActivity()).getAppBarHeight(), 0, 0);
    }

    private void initializeRecyclerView() {

        mChatList= view.findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(true);
        mChatList.setHasFixedSize(false);
        mChatListLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                LinearLayout.VERTICAL);
        mChatList.addItemDecoration(dividerItemDecoration);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(getActivity(), chatList, userList, mainTab);
        mChatList.setAdapter(mChatListAdapter);
        mChatListAdapter.notifyDataSetChanged();

        if(mainTab)
            mChatList.setPadding(0,((MainActivity)getActivity()).getAppBarHeight(), 0, 0);
    }
}
