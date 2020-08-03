package com.example.monitorme.Fragment.Camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.monitorme.Activity.MainActivity;
import com.example.monitorme.R;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;


public class CameraViewFragment extends Fragment implements View.OnClickListener{

    // variables
    View view;

    CameraView mCamera;

    ImageButton     mReverse,
                    mProfile,
                    mFlash,
                    mCapture;

    // not used
    public static CameraViewFragment newInstance(){
        CameraViewFragment fragment = new CameraViewFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera_view , container, false);

        // call method
        initializeObjects();

        return view;
    }

    void initializeObjects(){
        // initialise all variables with correspnding valuse from xml file
        mCamera = view.findViewById(R.id.camera);
        mReverse = view.findViewById(R.id.reverse);
        mCapture = view.findViewById(R.id.capture);
        mFlash = view.findViewById(R.id.flash);

        // set on click listeners
        mReverse.setOnClickListener(this);
        mFlash.setOnClickListener(this);
        mCapture.setOnClickListener(this);
        // sets flash on all time
        mCamera.setFlash(CameraKit.Constants.FLASH_ON);
    }

    public void captureImage() {
        mCamera.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(CameraKitImage cameraKitImage) {
                // save to bit map to sent to user
                ((MainActivity) getActivity()).setBitmapToSend(cameraKitImage.getBitmap());
                // opens the display image fragment java class
                ((MainActivity) getActivity()).openDisplayImageFragment();
            }
        });
    }

    private void reverseCameraFacing() {
        // reverse camera view
        if (mCamera.getFacing() == CameraKit.Constants.FACING_BACK)
            mCamera.setFacing(CameraKit.Constants.FACING_FRONT);
        else
            mCamera.setFacing(CameraKit.Constants.FACING_BACK);
    }

    private void flashClick() {
        // changes flash on / off
        if (mCamera.getFlash() == CameraKit.Constants.FLASH_ON){
            mFlash.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_flash_off_black_24dp));
            mCamera.setFlash(CameraKit.Constants.FLASH_OFF);
        }
        else{
            mFlash.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_flash_on_black_24dp));
            mCamera.setFlash(CameraKit.Constants.FLASH_ON);
        }
    }
    // changes the case
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // reverse camera
            case R.id.reverse:
                reverseCameraFacing();
                break;
                // turn on off flash
            case R.id.flash:
                flashClick();
                break;
                // capture
            case R.id.capture:
                captureImage();
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        initializeObjects();
        mCamera.start();
    }
    @Override
    public void onPause() {
        mCamera.stop();
        super.onPause();
    }
}
