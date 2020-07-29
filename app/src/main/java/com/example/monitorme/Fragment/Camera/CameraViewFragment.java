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

    View view;

    CameraView mCamera;

    ImageButton     mReverse,
                    mProfile,
                    mFlash,
                    mCapture;

    public static CameraViewFragment newInstance(){
        CameraViewFragment fragment = new CameraViewFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera_view , container, false);

        initializeObjects();

        return view;
    }

    void initializeObjects(){
        mCamera = view.findViewById(R.id.camera);
        mReverse = view.findViewById(R.id.reverse);
        mCapture = view.findViewById(R.id.capture);
        mFlash = view.findViewById(R.id.flash);

        mReverse.setOnClickListener(this);
        mFlash.setOnClickListener(this);
        mCapture.setOnClickListener(this);

        mCamera.setFlash(CameraKit.Constants.FLASH_ON);


    }

    public void captureImage() {
        mCamera.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(CameraKitImage cameraKitImage) {
                ((MainActivity) getActivity()).setBitmapToSend(cameraKitImage.getBitmap());
                ((MainActivity) getActivity()).openDisplayImageFragment();
            }
        });
    }

    private void reverseCameraFacing() {
        if (mCamera.getFacing() == CameraKit.Constants.FACING_BACK)
            mCamera.setFacing(CameraKit.Constants.FACING_FRONT);
        else
            mCamera.setFacing(CameraKit.Constants.FACING_BACK);
    }

    private void flashClick() {
        if (mCamera.getFlash() == CameraKit.Constants.FLASH_ON){
            mFlash.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_flash_off_black_24dp));
            mCamera.setFlash(CameraKit.Constants.FLASH_OFF);
        }
        else{
            mFlash.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_flash_on_black_24dp));
            mCamera.setFlash(CameraKit.Constants.FLASH_ON);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.reverse:
                reverseCameraFacing();
                break;
            case R.id.flash:
                flashClick();
                break;
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
