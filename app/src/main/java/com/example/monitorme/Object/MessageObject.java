package com.example.monitorme.Object;

import android.text.format.DateUtils;

import com.google.firebase.database.DataSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageObject {

    private String  messageId,
            senderId,
            message,
            timestampStr;

    private Long timestamp;

    private ArrayList<String> mediaUrlList = new ArrayList<>();

    public MessageObject(){
    }

    public void parseObject(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){return;}
        messageId = dataSnapshot.getKey();
        if(dataSnapshot.child("text").getValue() != null)
            message = dataSnapshot.child("text").getValue().toString();
        if(dataSnapshot.child("creator").getValue() != null)
            senderId = dataSnapshot.child("creator").getValue().toString();
        if(dataSnapshot.child("timestamp").getValue() != null)
            timestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());

        if(dataSnapshot.child("media").getChildrenCount() > 0)
            for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren())
                mediaUrlList.add(mediaSnapshot.getValue().toString());

        getDate();
    }
    public String getSenderId() {
        return senderId;
    }
    public String getMessage() {
        return message;
    }
    public String getTimestampStr() {
        return timestampStr;
    }
    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
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

}
