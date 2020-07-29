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
    int totalMediaUploaded = 0;
    ArrayList<String> mediaUriList = new ArrayList<>();
    ArrayList<String> mediaIdList = new ArrayList<>();
    ChatObject chatObject;
    Bitmap bitmap;
    Boolean fromMessageActivity;
    DatabaseReference mChatMessagesDb, mChatInfoDb;

    public SendMessage(ChatObject chatObject, Boolean fromMessageActivity, final ArrayList<String> mediaUriList, Bitmap bitmap, String message){
        this.mediaUriList = mediaUriList;
        this.chatObject = chatObject;
        this.bitmap = bitmap;
        this.fromMessageActivity = fromMessageActivity;

        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatObject.getChatId()).child("messages");
        mChatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatObject.getChatId()).child("info");

        String messageId = mChatMessagesDb.push().getKey();
        final DatabaseReference newMessageDb = mChatMessagesDb.child(messageId);

        final Map newMessageMap = new HashMap<>();

        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
        newMessageMap.put("timestamp", ServerValue.TIMESTAMP);

        if(!message.isEmpty())
            newMessageMap.put("text", message);



        if(fromMessageActivity){
            if(!mediaUriList.isEmpty()){
                for (String mediaUri : mediaUriList){
                    String mediaId = newMessageDb.child("media").push().getKey();
                    mediaIdList.add(mediaId);
                    final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(chatObject.getChatId()).child(messageId).child(mediaId);

                    UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
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
        newMessageDb.updateChildren(newMessageMap);
        if (mediaUriList!=null)
            mediaUriList.clear();
        if (mediaIdList!=null)
            mediaIdList.clear();
        totalMediaUploaded=0;

        String message;

        if(newMessageMap.get("text") != null)
            message = newMessageMap.get("text").toString();
        else
            message = "Media Received...";

        Map mInfoMap = new HashMap();
        mInfoMap.put("lastMessage", message);
        mInfoMap.put("timestamp", ServerValue.TIMESTAMP);
        mChatInfoDb.updateChildren(mInfoMap);


        for(UserObject mUser : chatObject.getUserObjectArrayList()){
            if(!mUser.getUid().equals(FirebaseAuth.getInstance().getUid())){
                new SendNotification(message, "New Message", mUser.getNotificationKey());
            }
            FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getUid()).child("chat").child(chatObject.getChatId()).setValue(ServerValue.TIMESTAMP);

        }
    }
}
