package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sangampradhan.nordicpaws.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountDetailsActivity extends AppCompatActivity {

    private ImageView accountAvatarImage;
    private TextView accountUserName;
    private EditText etFullName, etEmail, etPhone, etLocation;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageView btnToggleCurrentPwd, btnToggleNewPwd, btnToggleConfirmPwd;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private boolean isCurrentPwdVisible = false;
    private boolean isNewPwdVisible = false;
    private boolean isConfirmPwdVisible = false;

    private ActivityResultLauncher<String> mGetContent;
    private String currentImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        loadUserData();

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        currentImageUri = uri.toString();
                        accountAvatarImage.setImageURI(uri);
                        saveImageUriToFirestore(currentImageUri);
                    }
                });
    }

    private void initViews() {
        accountAvatarImage = findViewById(R.id.accountAvatarImage);
        accountUserName = findViewById(R.id.accountUserName);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnToggleCurrentPwd = findViewById(R.id.btnToggleCurrentPwd);
        btnToggleNewPwd = findViewById(R.id.btnToggleNewPwd);
        btnToggleConfirmPwd = findViewById(R.id.btnToggleConfirmPwd);
    }

    private void setupClickListeners() {
        CardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        CardView btnClose = findViewById(R.id.btnCloseAccount);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        View btnChangeAvatar = findViewById(R.id.btnAccountChangeAvatar);
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        }

        View btnSaveChanges = findViewById(R.id.btnSaveChanges);
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> savePersonalDetails());
        }

        View btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        if (btnUpdatePassword != null) {
            btnUpdatePassword.setOnClickListener(v -> updatePassword());
        }

        TextView btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }

        LinearLayout btnLogout = findViewById(R.id.accountBtnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }

        setupPasswordToggle(btnToggleCurrentPwd, etCurrentPassword, () -> {
            isCurrentPwdVisible = !isCurrentPwdVisible;
            return isCurrentPwdVisible;
        });

        setupPasswordToggle(btnToggleNewPwd, etNewPassword, () -> {
            isNewPwdVisible = !isNewPwdVisible;
            return isNewPwdVisible;
        });

        setupPasswordToggle(btnToggleConfirmPwd, etConfirmPassword, () -> {
            isConfirmPwdVisible = !isConfirmPwdVisible;
            return isConfirmPwdVisible;
        });
    }

    private void setupPasswordToggle(ImageView toggleBtn, EditText editText, ToggleCallback callback) {
        if (toggleBtn == null || editText == null) return;
        toggleBtn.setOnClickListener(v -> {
            boolean isVisible = callback.toggle();
            if (isVisible) {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleBtn.setColorFilter(getResources().getColor(R.color.primary_black, getTheme()));
            } else {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleBtn.clearColorFilter();
            }
            editText.setSelection(editText.getText().length());
        });
    }

    interface ToggleCallback {
        boolean toggle();
    }

    private void loadUserData() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;

        // Email always comes from Firebase Auth (read-only)
        if (fUser.getEmail() != null) {
            etEmail.setText(fUser.getEmail());
        }
        etEmail.setEnabled(false);

        mFirestore.collection("users").document(fUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            String name = user.getFullName() != null ? user.getFullName() : "";
                            accountUserName.setText(name);
                            etFullName.setText(name);
                            if (user.getMobileNumber() != null) etPhone.setText(user.getMobileNumber());
                            if (user.getAddress() != null) etLocation.setText(user.getAddress());

                            if (user.getLocalProfileImagePath() != null && !user.getLocalProfileImagePath().isEmpty()) {
                                currentImageUri = user.getLocalProfileImagePath();
                                try {
                                    accountAvatarImage.setImageURI(Uri.parse(currentImageUri));
                                } catch (Exception e) {
                                    accountAvatarImage.setImageResource(R.drawable.person);
                                }
                            } else {
                                accountAvatarImage.setImageResource(R.drawable.person);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> accountAvatarImage.setImageResource(R.drawable.person));
    }

    private void saveImageUriToFirestore(String uriString) {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;

        mFirestore.collection("users").document(fUser.getUid())
                .update("localProfileImagePath", uriString)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show());
    }

    private void savePersonalDetails() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;

        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("mobileNumber", phone);
        updates.put("address", location);

        mFirestore.collection("users").document(fUser.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    accountUserName.setText(fullName);
                    Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show());
    }

    private void updatePassword() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null || fUser.getEmail() == null) return;

        String currentPwd = etCurrentPassword.getText().toString();
        String newPwd = etNewPassword.getText().toString();
        String confirmPwd = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(currentPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirmPwd)) {
            Toast.makeText(this, "Please fill all password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPwd.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(fUser.getEmail(), currentPwd);
        fUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fUser.updatePassword(newPwd).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                    } else {
                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Signing Out?")
                .setMessage("Your pet care schedules, reminders, and health journals remain synced safely in the cloud.")
                .setPositiveButton("Confirm Log Out", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you certain you want to initiate account deletion? This action is permanent and cannot be undone.")
                .setPositiveButton("Delete Account", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;
        String uid = fUser.getUid();

        Toast.makeText(this, "Deleting account...", Toast.LENGTH_LONG).show();

        // Recursively delete data: pets, tasks, routines where owner == uid or related.
        // For simplicity, we delete the user document and their pets.
        Task<QuerySnapshot> petsTask = mFirestore.collection("pets").whereEqualTo("ownerId", uid).get();

        petsTask.addOnCompleteListener(task -> {
            List<Task<Void>> deleteTasks = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (int i = 0; i < task.getResult().size(); i++) {
                    deleteTasks.add(task.getResult().getDocuments().get(i).getReference().delete());
                }
            }

            Tasks.whenAllComplete(deleteTasks).addOnCompleteListener(t -> {
                // Now delete user doc
                mFirestore.collection("users").document(uid).delete().addOnCompleteListener(t2 -> {
                    // Finally delete auth user
                    fUser.delete().addOnCompleteListener(t3 -> {
                        if (t3.isSuccessful()) {
                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete Auth account", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
