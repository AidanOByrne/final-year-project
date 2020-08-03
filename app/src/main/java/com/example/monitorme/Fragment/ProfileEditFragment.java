package com.example.monitorme.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.monitorme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.monitorme.Activity.MainActivity;
import com.example.monitorme.Login.LauncherActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

// not used
public class ProfileEditFragment extends Fragment implements View.OnClickListener{

    
    ImageView   mBack,
                mConfirm;

    EditText    mName,
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


    public static ProfileEditFragment newInstance(){
        ProfileEditFragment fragment = new ProfileEditFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initializeObjects(view);

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

        return view;
    }

    private void getUserInfo() {
        name = ((MainActivity)getActivity()).getUser().getName();
        image = ((MainActivity)getActivity()).getUser().getImage();
        status = ((MainActivity)getActivity()).getUser().getStatus();
        phone = ((MainActivity)getActivity()).getUser().getPhone();

        if(phone != null)
            mPhone.setText(phone);
        if(status != null)
            mStatus.setText(status);
        if(name != null)
            mName.setText(name);

        if(image != null && getActivity()!=null)
            Glide.with(getActivity())
                    .load(image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mProfileImage);
    }

    private void saveUserInformation() {
        ((MainActivity)getActivity()).showProgressDialog("Saving Data...");

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
                    ((MainActivity)getActivity()).clearBackStack();
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

                            ((MainActivity)getActivity()).clearBackStack();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            ((MainActivity)getActivity()).clearBackStack();
                            return;
                        }
                    });
                }
            });
        }else{
            ((MainActivity)getActivity()).dismissProgressDialog();
            getActivity().onBackPressed();
        }
    }
    // not called 
    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    // 
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            resultUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                Glide.with(getActivity())
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
                ((MainActivity)getActivity()).clearBackStack();
                break;
            case R.id.confirm:
                saveUserInformation();
                break;

        }
    }

    private void initializeObjects(View view) {
        mName = view.findViewById(R.id.name);
        mStatus = view.findViewById(R.id.status);
        mPhone = view.findViewById(R.id.phone);
        mProfileImage = view.findViewById(R.id.profileImage);
        mBack = view.findViewById(R.id.back);
        mConfirm = view.findViewById(R.id.confirm);
        mBack.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
    }
}
