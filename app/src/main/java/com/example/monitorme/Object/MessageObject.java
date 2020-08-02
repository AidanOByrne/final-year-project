package com.example.monitorme.Object;

import android.text.format.DateUtils;

import com.google.firebase.database.DataSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageObject {

    // getters and setters for everything needed for the message
    // private string variables
    private String  messageId,
            senderId,
            message,
            timestampStr;

    // long for timestamp
    private Long timestamp;

    // array list
    private ArrayList<String> mediaUrlList = new ArrayList<>();

    public MessageObject(){
    }

    // parse each object to string from data snapshot based on the the vlaues
    public void parseObject(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){return;}
        messageId = dataSnapshot.getKey();
        if(dataSnapshot.child("text").getValue() != null)
            message = dataSnapshot.child("text").getValue().toString();
        if(dataSnapshot.child("creator").getValue() != null)
            senderId = dataSnapshot.child("creator").getValue().toString();
        if(dataSnapshot.child("timestamp").getValue() != null)
            timestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());

        //
        if(dataSnapshot.child("media").getChildrenCount() > 0)
            for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren())
                mediaUrlList.add(mediaSnapshot.getValue().toString());

        // get date function called
        getDate();
    }
    // getters
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

    // declare date and declared the pattern wanted when seen by users
    private void getDate(){
        DateFormat dateFormat;
        // if its today hours minutes
        if(DateUtils.isToday(timestamp)){
            dateFormat = new SimpleDateFormat("HH:mm");
            // if not then day month year
        }else{
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        }

        //set the date to the time stamp
        Date date = new Date(timestamp);
        timestampStr = dateFormat.format(date);
    }

}
