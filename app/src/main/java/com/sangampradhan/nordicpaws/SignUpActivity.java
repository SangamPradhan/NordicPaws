package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sangampradhan.nordicpaws.models.User;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout fullNameInputLayout, emailInputLayout, addressInputLayout, mobileNumberInputLayout;
    private TextInputEditText fullNameEditText, emailEditText, addressEditText, mobileNumberEditText;
    private MaterialButton createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Views
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        addressInputLayout = findViewById(R.id.addressInputLayout);
        mobileNumberInputLayout = findViewById(R.id.mobileNumberInputLayout);

        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addressEditText = findViewById(R.id.addressEditText);
        mobileNumberEditText = findViewById(R.id.mobileNumberEditText);
        createAccountButton = findViewById(R.id.createAccountButton);

        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        TextView loginText = findViewById(R.id.loginText);
        if (loginText != null) {
            loginText.setOnClickListener(v -> {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (createAccountButton != null) {
            createAccountButton.setOnClickListener(v -> registerUser());
        }
    }

    private void registerUser() {
        // Clear previous errors
        fullNameInputLayout.setError(null);
        emailInputLayout.setError(null);
        addressInputLayout.setError(null);
        mobileNumberInputLayout.setError(null);

        String fullName = fullNameEditText.getText() != null ? fullNameEditText.getText().toString().trim() : "";
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String address = addressEditText.getText() != null ? addressEditText.getText().toString().trim() : "";
        String mobileNumber = mobileNumberEditText.getText() != null ? mobileNumberEditText.getText().toString().trim() : "";

        // Input Validations
        if (TextUtils.isEmpty(fullName)) {
            fullNameInputLayout.setError("Please enter your full name");
            fullNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            addressInputLayout.setError("Please enter your address");
            addressEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(mobileNumber)) {
            mobileNumberInputLayout.setError("Please enter your mobile number");
            mobileNumberEditText.requestFocus();
            return;
        }

        // Pass data to next step
        Intent intent = new Intent(SignUpActivity.this, SignUpPasswordActivity.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("address", address);
        intent.putExtra("mobileNumber", mobileNumber);
        startActivity(intent);
    }
}
