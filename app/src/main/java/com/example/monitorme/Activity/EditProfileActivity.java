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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    // declare everything we need from the page, variables, image view, text, database references etc
    // this page is the exat same as the chat config page
    // we jsut need more things here like status
    // same principles applied
    // same comments
    ImageView mBack,
            mConfirm;

    EditText mName,
            mStatus,
            mPhone;

    ImageView mProfileImage;

    FirebaseAuth mAuth;
    DatabaseReference mUserDatabase;

    String      userId = "",
            name = "--",
            image="--",
            status = "--",
            phone = "--";


    Uri resultUri;

    UserObject mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile_edit);


        mUser = (UserObject) getIntent().getSerializableExtra("userObject");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initializeObjects();

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

    }

    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        //mDialog.show();
    }
    public void  dismissProgressDialog(){
        if(mDialog!=null)
            mDialog.dismiss();
    }

    private void getUserInfo() {
        name = mUser.getName();
        image = mUser.getImage();
        status = mUser.getStatus();
        phone = mUser.getPhone();

        if(phone != null)
            mPhone.setText(phone);
        if(status != null)
            mStatus.setText(status);
        if(name != null)
            mName.setText(name);

        if(image != null && getApplication()!=null)
            Glide.with(this)
                    .load(image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mProfileImage);
    }

    private void saveUserInformation() {
        showProgressDialog("Saving Data...");

        if(!mName.getText().toString().isEmpty())
            name = mName.getText().toString();
        if(!mStatus.getText().toString().isEmpty())
            status = mStatus.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("status", status);

        if(image != null)
            userInfo.put("image", image);

        mUserDatabase.updateChildren(userInfo);

        if(resultUri != null) {
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_image").child(userId);

            UploadTask uploadTask = filePath.putFile(resultUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
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
                            mUserDatabase.updateChildren(newImage);

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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                Glide.with(this)
                        .load(bitmap) // Uri of the picture
                        .apply(RequestOptions.circleCropTransform())
                        .into(mProfileImage);
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
                saveUserInformation();
                break;

        }
    }

    private void initializeObjects() {
        mName = findViewById(R.id.name);
        mStatus = findViewById(R.id.status);
        mPhone = findViewById(R.id.phone);
        mProfileImage = findViewById(R.id.profileImage);
        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);
        mBack.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
    }
}
