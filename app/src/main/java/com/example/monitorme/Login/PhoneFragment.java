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

    EditText mPhoneNumber;
    Button mNext;
    CountryCodePicker ccp;

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_authentication_phone, container, false);
        else
            container.removeView(view);


        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeObjects();

        phoneEditTextButtonColor();
    }

    /**
     * Change the color of R.id.next Button Background
     * Color if there is text in the R.id.phone EditText
     */
    private void phoneEditTextButtonColor() {
        mNext.setActivated(false);
        mNext.setBackgroundColor(getResources().getColor(R.color.unselected));
        mPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0){
                    mNext.setActivated(true);
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
        String phoneBuilt = "";
        phoneBuilt = ccp.getSelectedCountryCodeWithPlus() + mPhoneNumber.getText().toString();

        return  phoneBuilt;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.next:
                if(mPhoneNumber.getText().length() <= 0)
                    return;

                ((AuthenticationActivity) getActivity()).startPhoneNumberVerification(buildPhoneNumber());
                break;
        }
    }
    void initializeObjects(){
        mPhoneNumber = view.findViewById(R.id.phone);
        mNext = view.findViewById(R.id.next);
        ccp = view.findViewById(R.id.ccp);


        mNext.setOnClickListener(this);

    }
}