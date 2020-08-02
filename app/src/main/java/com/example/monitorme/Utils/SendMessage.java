package com.example.monitorme.Utils;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.monitorme.Object.ChatObject;
import com.example.monitorme.Object.UserObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SendMessage {

    // declare all variables needed array list, chat objects, bit map, boolean , database reference
    int totalMediaUploaded = 0;
    ArrayList<String> mediaUriList = new ArrayList<>();
    ArrayList<String> mediaIdList = new ArrayList<>();
    ChatObject chatObject;
    Bitmap bitmap;
    Boolean fromMessageActivity;
    DatabaseReference mChatMessagesDb, mChatInfoDb;

    public SendMessage(ChatObject chatObject, Boolean fromMessageActivity, final ArrayList<String> mediaUriList, Bitmap bitmap, String message){
        // assign variables to values
        this.mediaUriList = mediaUriList;
        this.chatObject = chatObject;
        this.bitmap = bitmap;
        this.fromMessageActivity = fromMessageActivity;

        // create links to database fully
        // getting the right info and messages
        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatObject.getChatId()).child("messages");
        mChatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatObject.getChatId()).child("info");

        // get key
        String messageId = mChatMessagesDb.push().getKey();

        // get message id database reference
        final DatabaseReference newMessageDb = mChatMessagesDb.child(messageId);

        // create hash map
        final Map newMessageMap = new HashMap<>();

        // put the u id and timestamp with the correct name
        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
        newMessageMap.put("timestamp", ServerValue.TIMESTAMP);

        // checks if message is empty put the text beside the mesage
        if(!message.isEmpty())
            newMessageMap.put("text", message);

        // check if its from the boolean value
        if(fromMessageActivity){
            // check if its empty
            if(!mediaUriList.isEmpty()){
                // for media uri in media uri list
                // allows the the user to send more then one media message
                for (String mediaUri : mediaUriList){
                    // gey key and save to media id
                    String mediaId = newMessageDb.child("media").push().getKey();
                    // add to array list
                    mediaIdList.add(mediaId);
                    // get storage reference form database based on media id and message id
                    final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(chatObject.getChatId()).child(messageId).child(mediaId);

                    // create upload task to put the file path into database later
                    UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                    //  on success listener to upload task
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // on success get download url fro file path
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // put the
                                    newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());

                                    totalMediaUploaded++;
                                    if(totalMediaUploaded == mediaIdList.size())
                                        updateDatabaseWithNewMessage(newMessageDb, newMessageMap);

                                }
                            });
                        }
                    });
                }
            }else{
                if(!message.isEmpty())
                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
            }
        }else{
            final String mediaId = newMessageDb.child("media").push().getKey();
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(chatObject.getChatId()).child(messageId).child(mediaId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataToUpload = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(dataToUpload);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newMessageMap.put("/media/" + mediaId + "/", uri.toString());
                            updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
                        }
                    });
                }
            });
        }
    }


    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap){
        // update database based on new database reference
        newMessageDb.updateChildren(newMessageMap);
        // if null clear clear the uri list
        if (mediaUriList!=null)
            mediaUriList.clear();
        if (mediaIdList!=null)
            mediaIdList.clear();
        totalMediaUploaded=0;

        String message;

        // if the value was not equal to null get the text being sent and put it in message
        if(newMessageMap.get("text") != null)
            message = newMessageMap.get("text").toString();
        else
            // if not then default message
            message = "Media Received...";

        // update database with the correct message and value
        Map mInfoMap = new HashMap();
        mInfoMap.put("lastMessage", message);
        mInfoMap.put("timestamp", ServerValue.TIMESTAMP);
        mChatInfoDb.updateChildren(mInfoMap);

        // for each user in the array list send the notification to the user to show the they havea message
        for(UserObject mUser : chatObject.getUserObjectArrayList()){
            //
            if(!mUser.getUid().equals(FirebaseAuth.getInstance().getUid())){
                new SendNotification(message, "New Message", mUser.getNotificationKey());
            }
            // set timestamp
            FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getUid()).child("chat").child(chatObject.getChatId()).setValue(ServerValue.TIMESTAMP);

        }
    }
}
