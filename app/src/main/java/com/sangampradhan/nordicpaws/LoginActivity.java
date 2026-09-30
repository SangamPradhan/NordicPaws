package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPassword, signupText;

    private FirebaseAuth mAuth;

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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind Views
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        signupText = findViewById(R.id.signupText);

        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        if (signupText != null) {
            signupText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            });
        }

        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> handleForgotPassword());
        }

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> loginUser());
        }
    }

    private void loginUser() {
        // Clear previous errors
        if (emailInputLayout != null) emailInputLayout.setError(null);
        if (passwordInputLayout != null) passwordInputLayout.setError(null);

        String email = emailEditText != null && emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText != null && passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

        // Input Validations
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailInputLayout != null) emailInputLayout.setError("Please enter a valid email address");
            if (emailEditText != null) emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            if (passwordInputLayout != null) passwordInputLayout.setError("Please enter your password");
            if (passwordEditText != null) passwordEditText.requestFocus();
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Perform Firebase Authentication Sign In
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Welcome back! 🐾", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, LoadingActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    } else {
                        setLoadingState(false);
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            if (emailInputLayout != null) emailInputLayout.setError("No account found with this email");
                            if (emailEditText != null) emailEditText.requestFocus();
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            if (passwordInputLayout != null) passwordInputLayout.setError("Incorrect password");
                            if (passwordEditText != null) passwordEditText.requestFocus();
                        } else {
                            String errorMsg = exception != null ? exception.getLocalizedMessage() : "Login failed.";
                            Toast.makeText(LoginActivity.this, "Authentication Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void handleForgotPassword() {
        String email = emailEditText != null && emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter your registered email address above first.", Toast.LENGTH_LONG).show();
            if (emailEditText != null) emailEditText.requestFocus();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Send password reset instructions to " + email + "?")
                .setPositiveButton("Send", (dialog, which) -> {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "Password reset link sent to your email!", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error sending reset email: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setLoadingState(boolean isLoading) {
        if (loginButton != null) {
            loginButton.setEnabled(!isLoading);
            loginButton.setText(isLoading ? "Logging in..." : "Log In 🐾");
        }
        if (emailEditText != null) emailEditText.setEnabled(!isLoading);
        if (passwordEditText != null) passwordEditText.setEnabled(!isLoading);
    }
}