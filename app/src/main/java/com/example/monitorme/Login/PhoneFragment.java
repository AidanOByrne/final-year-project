package com.example.monitorme.Login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hbb20.CountryCodePicker;
import com.example.monitorme.R;

public class PhoneFragment extends Fragment implements View.OnClickListener {

    // declare variables we need
    EditText mPhoneNumber;
    Button mNext;
    CountryCodePicker ccp;

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // if view null then get inflate fragment authentication phone
        if (view == null)
            view = inflater.inflate(R.layout.fragment_authentication_phone, container, false);
        else
            // remove the view
            container.removeView(view);

        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // call methods
        initializeObjects();
        phoneEditTextButtonColor();
    }

    /**
     * Change the color of R.id.next Button Background
     * Color if there is text in the R.id.phone EditText
     */
    private void phoneEditTextButtonColor() {
        // next button
        mNext.setActivated(false);
        // set background color when unselected
        mNext.setBackgroundColor(getResources().getColor(R.color.unselected));
        // change text but we use it to change colour
        mPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0){
                    mNext.setActivated(true);
                    // set background color to my primary colour set in values/colors.xml
                    mNext.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                else {
                    mNext.setActivated(false);
                    mNext.setBackgroundColor(getResources().getColor(R.color.unselected));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private String buildPhoneNumber(){
        // new string
        String phoneBuilt = "";
        // ccp is the country pre fix add the phone number change to string then save to phone built
        phoneBuilt = ccp.getSelectedCountryCodeWithPlus() + mPhoneNumber.getText().toString();
        // return phoneBuilt string
        return  phoneBuilt;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            // when next pressed
            case R.id.next:
                if(mPhoneNumber.getText().length() <= 0)
                    return;
                // send number we just created to authentication process
                ((AuthenticationActivity) getActivity()).startPhoneNumberVerification(buildPhoneNumber());
                break;
        }
    }
    void initializeObjects(){
        // initialize all variables based on id match in xml file
        mPhoneNumber = view.findViewById(R.id.phone);
        mNext = view.findViewById(R.id.next);
        ccp = view.findViewById(R.id.ccp);

        mNext.setOnClickListener(this);

    }
}