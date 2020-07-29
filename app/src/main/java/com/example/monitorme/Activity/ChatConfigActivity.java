package com.example.monitorme.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.monitorme.Object.ChatObject;
import com.example.monitorme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatConfigActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView mBack,
            mConfirm,
            mDelete;

    EditText mName;

    ImageView mImage;

    FirebaseAuth mAuth;
    DatabaseReference mInfoDatabase, mInfoDatabaseDelete, mInfoDatabaseDelete1, mInfoDatabaseUserDelete;

    String      userId = "",
                name = "--",
                image="--";


    Uri resultUri;
    ChatObject chatObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_config);

        chatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initializeObjects();

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mInfoDatabase = FirebaseDatabase.getInstance().getReference().child("chat").child(chatObject.getChatId()).child("info");

        getChatInfo();

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        // delete function for chat based on current chat id
        // create two connections into the chat table and then the user table
        // delete the both records from the corosponding tables

        mInfoDatabaseDelete = FirebaseDatabase.getInstance().getReference().child("chat").child(chatObject.getChatId());
        mInfoDatabaseUserDelete = FirebaseDatabase.getInstance().getReference().child("user").child(userId).child("chat").child(chatObject.getChatId());

        mInfoDatabaseDelete1 = FirebaseDatabase.getInstance().getReference().child("chat");
        mDelete = findViewById(R.id.chat_delete);

        mDelete.setOnClickListener(v -> {
            //mInfoDatabaseDelete.removeValue();
            mInfoDatabaseUserDelete.removeValue();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(ChatConfigActivity.this);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        //mDialog.show();
    }
    public void  dismissProgressDialog(){
        if(mDialog!=null)
            mDialog.dismiss();
    }

    private void getChatInfo() {

        name = chatObject.getName();
        image = chatObject.getImage();

        if(name != null)
            mName.setText(name);
        if(!image.isEmpty() && getApplication()!=null)
            Glide.with(ChatConfigActivity.this)
                    .load(image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mImage);
    }

    private void saveChatInformation() {

        showProgressDialog("Saving Chat Info...");

        if(!mName.getText().toString().isEmpty())
            name = mName.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);

        if(image != null)
            userInfo.put("image", image);

        mInfoDatabase.updateChildren(userInfo);

        if(resultUri != null) {
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat_image").child(userId);

            UploadTask uploadTask = filePath.putFile(resultUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("image", uri.toString());
                            mInfoDatabase.updateChildren(newImage);

                            finish();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            finish();
                            return;
                        }
                    });
                }
            });
        }else{
            finish();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            resultUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                Glide.with(ChatConfigActivity.this)
                        .load(bitmap) // Uri of the picture
                        .apply(RequestOptions.circleCropTransform())
                        .into(mImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.confirm:
                saveChatInformation();
                break;

        }
    }

    private void initializeObjects() {
        mName = findViewById(R.id.name);
        mImage = findViewById(R.id.profileImage);
        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);

        mBack.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
    }
}
