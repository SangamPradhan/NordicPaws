package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AccountDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        // Back button
        CardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Close button
        CardView btnClose = findViewById(R.id.btnCloseAccount);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // Change avatar
        View btnChangeAvatar = findViewById(R.id.btnAccountChangeAvatar);
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v ->
                Toast.makeText(this, "Change photo coming soon!", Toast.LENGTH_SHORT).show());
        }

        // Update Password
        TextView btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        if (btnUpdatePassword != null) {
            btnUpdatePassword.setOnClickListener(v ->
                Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show());
        }

        // Save Changes
        View btnSaveChanges = findViewById(R.id.btnSaveChanges);
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v ->
                Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show());
        }

        // Account rows
        LinearLayout rowDetails = findViewById(R.id.accountRowDetails);
        if (rowDetails != null) {
            rowDetails.setOnClickListener(v ->
                Toast.makeText(this, "Personal details coming soon!", Toast.LENGTH_SHORT).show());
        }

        LinearLayout rowNotifPrefs = findViewById(R.id.accountRowNotifPrefs);
        if (rowNotifPrefs != null) {
            rowNotifPrefs.setOnClickListener(v ->
                Toast.makeText(this, "Notification preferences coming soon!", Toast.LENGTH_SHORT).show());
        }

        LinearLayout rowManagePets = findViewById(R.id.accountRowManagePets);
        if (rowManagePets != null) {
            rowManagePets.setOnClickListener(v -> finish());
        }

        // Delete Account
        TextView btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }

        // Log out
        LinearLayout btnLogout = findViewById(R.id.accountBtnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Signing Out?")
            .setMessage("Your pet care schedules, reminders, and health journals remain synced safely in the cloud.")
            .setPositiveButton("Confirm Log Out", (dialog, which) -> {
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
            .setPositiveButton("Delete Account", (dialog, which) ->
                Toast.makeText(this, "Deletion request received.", Toast.LENGTH_SHORT).show())
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
