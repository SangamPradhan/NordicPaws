package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sangampradhan.nordicpaws.models.User;

public class SignUpPasswordActivity extends AppCompatActivity {

    private TextInputLayout passwordInputLayout, confirmPasswordInputLayout;
    private TextInputEditText passwordEditText, confirmPasswordEditText;
    private CheckBox termsCheckBox;
    private MaterialButton createAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private String fullName;
    private String email;
    private String address;
    private String mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Get data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            fullName = intent.getStringExtra("fullName");
            email = intent.getStringExtra("email");
            address = intent.getStringExtra("address");
            mobileNumber = intent.getStringExtra("mobileNumber");
        }

        // Initialize UI Views
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        createAccountButton = findViewById(R.id.createAccountButton);

        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        if (createAccountButton != null) {
            createAccountButton.setOnClickListener(v -> registerUser());
        }
    }

    private void registerUser() {
        // Clear previous errors
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

        // Input Validations
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Please enter a password");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters long");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (termsCheckBox != null && !termsCheckBox.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms & Privacy Policy to continue.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading status
        setLoadingState(true);

        // Perform Firebase Auth Sign Up
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // 1. Update Display Name in Firebase Auth profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates);

                            // 2. Save user profile to Cloud Firestore
                            User user = new User(firebaseUser.getUid(), fullName, email, address, mobileNumber, System.currentTimeMillis());
                            mFirestore.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpPasswordActivity.this, "Account created successfully! Welcome to Nordic Paws \uD83D\uDC3E", Toast.LENGTH_LONG).show();
                                        Intent loginIntent = new Intent(SignUpPasswordActivity.this, LoadingActivity.class);
                                        startActivity(loginIntent);
                                        finishAffinity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpPasswordActivity.this, "Account created! Error saving user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Intent loginIntent = new Intent(SignUpPasswordActivity.this, LoadingActivity.class);
                                        startActivity(loginIntent);
                                        finishAffinity();
                                    });
                        }
                    } else {
                        setLoadingState(false);
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_LONG).show();
                        } else {
                            String errorMsg = exception != null ? exception.getLocalizedMessage() : "Authentication failed.";
                            Toast.makeText(SignUpPasswordActivity.this, "Registration Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (createAccountButton != null) {
            createAccountButton.setEnabled(!isLoading);
            createAccountButton.setText(isLoading ? "Creating Account..." : "Create Account");
        }
        if (passwordEditText != null) passwordEditText.setEnabled(!isLoading);
        if (confirmPasswordEditText != null) confirmPasswordEditText.setEnabled(!isLoading);
        if (termsCheckBox != null) termsCheckBox.setEnabled(!isLoading);
    }
}
