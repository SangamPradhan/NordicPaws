package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private MaterialButton getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        getStartedButton = findViewById(R.id.getStartedButton);

        getStartedButton.setOnClickListener(v -> {

            // We will connect this to LoginActivity later.
//            Intent intent = new Intent(
//                    WelcomeActivity.this,
//                    LoginActivity.class
//            );
//
//            startActivity(intent);

        });
    }
}