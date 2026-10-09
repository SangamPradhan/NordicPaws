package com.sangampradhan.nordicpaws.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sangampradhan.nordicpaws.LoginActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountDetailsBottomSheetFragment extends BottomSheetDialogFragment {

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

    private String currentImageUri = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    public static AccountDetailsBottomSheetFragment newInstance() {
        return new AccountDetailsBottomSheetFragment();
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
                        .setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
                        .setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    requireActivity().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentImageUri = uri.toString();
                if (accountAvatarImage != null) accountAvatarImage.setImageURI(uri);
                saveImageUri(currentImageUri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_account_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        accountAvatarImage = view.findViewById(R.id.accountAvatarImage);
        accountUserName   = view.findViewById(R.id.accountUserName);
        etFullName        = view.findViewById(R.id.etFullName);
        etEmail           = view.findViewById(R.id.etEmail);
        etPhone           = view.findViewById(R.id.etPhone);
        etLocation        = view.findViewById(R.id.etLocation);
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword     = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnToggleCurrentPwd = view.findViewById(R.id.btnToggleCurrentPwd);
        btnToggleNewPwd     = view.findViewById(R.id.btnToggleNewPwd);
        btnToggleConfirmPwd = view.findViewById(R.id.btnToggleConfirmPwd);

        // Navigation
        CardView btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> dismiss());
        CardView btnClose = view.findViewById(R.id.btnCloseAccount);
        if (btnClose != null) btnClose.setOnClickListener(v -> dismiss());

        // Avatar picker
        View btnChangeAvatar = view.findViewById(R.id.btnAccountChangeAvatar);
        if (btnChangeAvatar != null) btnChangeAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Save personal info
        View btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        if (btnSaveChanges != null) btnSaveChanges.setOnClickListener(v -> savePersonalDetails());

        // Update password
        View btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword);
        if (btnUpdatePassword != null) btnUpdatePassword.setOnClickListener(v -> updatePassword());

        // Delete account
        TextView btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // Logout
        LinearLayout btnLogout = view.findViewById(R.id.accountBtnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Password toggles
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

        // Load real user data
        loadUserData();
    }

    // ─── Data loading ───────────────────────────────────────────────────────────

    private void loadUserData() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;

        // Email from Auth — read-only
        if (etEmail != null && fUser.getEmail() != null) {
            etEmail.setText(fUser.getEmail());
        }
        if (etEmail != null) etEmail.setEnabled(false);

        // Show displayName immediately as placeholder while Firestore loads
        if (fUser.getDisplayName() != null && !fUser.getDisplayName().isEmpty()) {
            if (accountUserName != null) accountUserName.setText(fUser.getDisplayName());
            if (etFullName != null) etFullName.setText(fUser.getDisplayName());
        }

        mFirestore.collection("users").document(fUser.getUid()).get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    if (snap.exists()) {
                        User user = snap.toObject(User.class);
                        if (user != null) {
                            // Full name — prefer Firestore, fall back to Auth displayName
                            String name = user.getFullName();
                            if (name == null || name.isEmpty()) {
                                name = fUser.getDisplayName() != null ? fUser.getDisplayName() : "";
                            }
                            if (accountUserName != null) accountUserName.setText(name);
                            if (etFullName != null) etFullName.setText(name);
                            if (etPhone != null && user.getMobileNumber() != null)
                                etPhone.setText(user.getMobileNumber());
                            if (etLocation != null && user.getAddress() != null)
                                etLocation.setText(user.getAddress());

                            // Profile image
                            if (user.getLocalProfileImagePath() != null
                                    && !user.getLocalProfileImagePath().isEmpty()) {
                                currentImageUri = user.getLocalProfileImagePath();
                                try {
                                    if (accountAvatarImage != null)
                                        accountAvatarImage.setImageURI(Uri.parse(currentImageUri));
                                } catch (Exception e) {
                                    if (accountAvatarImage != null)
                                        accountAvatarImage.setImageResource(R.drawable.person);
                                }
                            } else {
                                if (accountAvatarImage != null)
                                    accountAvatarImage.setImageResource(R.drawable.person);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (accountAvatarImage != null)
                        accountAvatarImage.setImageResource(R.drawable.person);
                });
    }

    // ─── Save personal details ──────────────────────────────────────────────────

    private void savePersonalDetails() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;

        String fullName = etFullName.getText().toString().trim();
        String phone    = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("mobileNumber", phone);
        updates.put("address", location);

        mFirestore.collection("users").document(fUser.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (accountUserName != null) accountUserName.setText(fullName);
                    Toast.makeText(getContext(), "Changes saved successfully ✓", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error saving changes", Toast.LENGTH_SHORT).show());
    }

    // ─── Profile image ──────────────────────────────────────────────────────────

    private void saveImageUri(String uriString) {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;
        mFirestore.collection("users").document(fUser.getUid())
                .update("localProfileImagePath", uriString)
                .addOnSuccessListener(v ->
                        Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update picture", Toast.LENGTH_SHORT).show());
    }

    // ─── Password update ────────────────────────────────────────────────────────

    private void updatePassword() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null || fUser.getEmail() == null) return;

        String currentPwd = etCurrentPassword.getText().toString();
        String newPwd     = etNewPassword.getText().toString();
        String confirmPwd = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(currentPwd)) {
            etCurrentPassword.setError("Enter your current password");
            etCurrentPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPwd)) {
            etNewPassword.setError("Enter a new password");
            etNewPassword.requestFocus();
            return;
        }
        if (newPwd.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(fUser.getEmail(), currentPwd);
        fUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (!isAdded()) return;
            if (task.isSuccessful()) {
                fUser.updatePassword(newPwd).addOnCompleteListener(task1 -> {
                    if (!isAdded()) return;
                    if (task1.isSuccessful()) {
                        Toast.makeText(getContext(), "Password updated successfully ✓", Toast.LENGTH_SHORT).show();
                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                    } else {
                        Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Password toggle ────────────────────────────────────────────────────────

    private void setupPasswordToggle(ImageView toggleBtn, EditText editText, ToggleCallback callback) {
        if (toggleBtn == null || editText == null) return;
        toggleBtn.setOnClickListener(v -> {
            boolean isVisible = callback.toggle();
            if (isVisible) {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleBtn.setColorFilter(requireContext().getColor(R.color.primary_black));
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

    // ─── Logout ─────────────────────────────────────────────────────────────────

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Signing Out?")
                .setMessage("Your pet care schedules, reminders, and health journals remain synced safely in the cloud.")
                .setPositiveButton("Confirm Log Out", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Delete account ─────────────────────────────────────────────────────────

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you certain? This action is permanent and cannot be undone. All your pets and data will be deleted.")
                .setPositiveButton("Delete Account", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;
        String uid = fUser.getUid();

        Toast.makeText(getContext(), "Deleting account...", Toast.LENGTH_LONG).show();

        Task<QuerySnapshot> petsTask = mFirestore.collection("users").document(uid)
                .collection("pets").get();

        petsTask.addOnCompleteListener(task -> {
            if (!isAdded()) return;
            List<Task<Void>> deleteTasks = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (int i = 0; i < task.getResult().size(); i++) {
                    deleteTasks.add(task.getResult().getDocuments().get(i).getReference().delete());
                }
            }
            Tasks.whenAllComplete(deleteTasks).addOnCompleteListener(t -> {
                mFirestore.collection("users").document(uid).delete().addOnCompleteListener(t2 -> {
                    fUser.delete().addOnCompleteListener(t3 -> {
                        if (!isAdded()) return;
                        if (t3.isSuccessful()) {
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete account. Try re-logging in first.", Toast.LENGTH_LONG).show();
                        }
                    });
                });
            });
        });
    }
}
