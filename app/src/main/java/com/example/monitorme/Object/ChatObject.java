package com.example.monitorme.Object;

import android.text.format.DateUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatObject implements Serializable, Comparable<ChatObject>{

    private String  chatId,
            lastMessage,
            timestampStr,
            image,
            nameByUsers,
            name;
    Long timestamp;

    private ArrayList<UserObject> userObjectArrayList = new ArrayList<>();

    public ChatObject(String chatId){
        this.chatId = chatId;
        nameByUsers = "";
        name = "";
        image = "";
    }

    public void parseObject(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){return;}
        if(dataSnapshot.child("image").getValue() != null)
            image = dataSnapshot.child("image").getValue().toString();
        if(dataSnapshot.child("name").getValue() != null)
            name = dataSnapshot.child("name").getValue().toString();
    }

    public String getChatId() {
        return chatId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNameByUsers(String nameByUsers) {
        this.nameByUsers = nameByUsers;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        getDate();
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<UserObject> getUserObjectArrayList() {
        return userObjectArrayList;
    }
    public String getName() {
        return name;
    }
    public String getNameByUsers() {
        String name = "";
        ArrayList<UserObject> userNameList = new ArrayList<>();

        for (UserObject mUser : userObjectArrayList){
            if (mUser.getUid().equals(FirebaseAuth.getInstance().getUid()))
                continue;

            userNameList.add(mUser);
        }
        int i;
        for (i = 0; i < userNameList.size() - 1; i++){
            name += userNameList.get(i).getName() + ", ";
        }
        if(userNameList.size()>0)
            name += userNameList.get(i).getName();

        nameByUsers = name;
        return nameByUsers;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public String getTimestampStr() {
        return timestampStr;
    }
    public String getImage() {
        return image;
    }

    public void addUserToArrayList(UserObject mUser){
        userObjectArrayList.add(mUser);
    }

    private void getDate(){
        DateFormat dateFormat;
        if(DateUtils.isToday(timestamp)){
            dateFormat = new SimpleDateFormat("HH:mm");
        }else{
            dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        }

        Date date = new Date(timestamp);
        timestampStr = dateFormat.format(date);
    }

    @Override
    public int compareTo(ChatObject f) {

        if (timestamp < f.getTimestamp()) {
            return 1;
        }
        else if (timestamp >  f.getTimestamp()) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
