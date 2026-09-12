package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        TextView signupText = findViewById(R.id.signupText);
        if (signupText != null) {
            signupText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            });
        }
        
        Button loginButton = findViewById(R.id.loginButton);
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, LoadingActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}