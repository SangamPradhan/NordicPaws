package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sangampradhan.nordicpaws.models.User;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout fullNameInputLayout, emailInputLayout, passwordInputLayout, confirmPasswordInputLayout;
    private TextInputEditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private CheckBox termsCheckBox;
    private MaterialButton createAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

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

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Initialize UI Views
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);

        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        termsCheckBox = findViewById(R.id.termsCheckBox);
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
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        String fullName = fullNameEditText.getText() != null ? fullNameEditText.getText().toString().trim() : "";
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

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
                            User user = new User(firebaseUser.getUid(), fullName, email, System.currentTimeMillis());
                            mFirestore.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this, "Account created successfully! Welcome to Nordic Paws 🐾", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(SignUpActivity.this, LoadingActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Even if Firestore doc write fails, user is authenticated in Auth
                                        Toast.makeText(SignUpActivity.this, "Account created! Error saving user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, LoadingActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    });
                        }
                    } else {
                        setLoadingState(false);
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            emailInputLayout.setError("An account with this email already exists");
                            emailEditText.requestFocus();
                        } else {
                            String errorMsg = exception != null ? exception.getLocalizedMessage() : "Authentication failed.";
                            Toast.makeText(SignUpActivity.this, "Registration Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (createAccountButton != null) {
            createAccountButton.setEnabled(!isLoading);
            createAccountButton.setText(isLoading ? "Creating Account..." : "Create Account");
        }
        if (fullNameEditText != null) fullNameEditText.setEnabled(!isLoading);
        if (emailEditText != null) emailEditText.setEnabled(!isLoading);
        if (passwordEditText != null) passwordEditText.setEnabled(!isLoading);
        if (confirmPasswordEditText != null) confirmPasswordEditText.setEnabled(!isLoading);
        if (termsCheckBox != null) termsCheckBox.setEnabled(!isLoading);
    }
}
