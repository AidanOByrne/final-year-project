package com.example.monitorme.Login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.example.monitorme.Activity.MainActivity;


public class LauncherActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launcher activity class
        // checks to see if the user is not equal to null,
        // aka is signed in already
        // if signed in then go straight to the MainActivity

        // get instance of current user from Firebase
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            // if user is Not equal to null then go to MainActivity
            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }else{
            // else go to the AuthenticationActivity where the user will login
            Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }
}
