package com.example.monitorme.Login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chaos.view.PinView;
import com.example.monitorme.R;

public class CodeFragment extends Fragment {
    // same as phone fragment
    View view;
    PinView mCode;

    TextView mPhone,mPhoneLong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_registration_details, container, false);
        else
            container.removeView(view);

        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // call methods on view creation
        initializeObjects();
        codeInputHandling();
        setPhoneText();
    }

    private void codeInputHandling() {
        // add text change listener to get the code when the user is typing
        mCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // if length is correct save the code and send it to the verification page
                if(s.length() == 6){
                    ((AuthenticationActivity)getActivity()).verifyPhoneNumberWithCode(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void setPhoneText(){
        // sets the text of the message seen by the user when the try to get authenticated
        mPhone.setText("Verify " + ((AuthenticationActivity)getActivity()).getPhoneNumber());
        mPhoneLong.setText("Waiting for the users 6 digit security code " + ((AuthenticationActivity)getActivity()).getPhoneNumber());
    }

    void initializeObjects(){
        // initialise variables based on id in xml file
        mCode = view.findViewById(R.id.code);
        mPhone = view.findViewById(R.id.phone);
        mPhoneLong = view.findViewById(R.id.phoneLong);
    }
}