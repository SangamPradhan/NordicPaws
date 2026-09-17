package com.sangampradhan.nordicpaws.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sangampradhan.nordicpaws.LoginActivity;
import com.sangampradhan.nordicpaws.R;

public class AccountDetailsBottomSheetFragment extends BottomSheetDialogFragment {

    public static AccountDetailsBottomSheetFragment newInstance() {
        return new AccountDetailsBottomSheetFragment();
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            (com.google.android.material.bottomsheet.BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
                (com.google.android.material.bottomsheet.BottomSheetDialog) d;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet).setState(
                    com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                );
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            }
        });
        return dialog;
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


        // Back button
        CardView btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> dismiss());

        // Close button
        CardView btnClose = view.findViewById(R.id.btnCloseAccount);
        if (btnClose != null) btnClose.setOnClickListener(v -> dismiss());

        // Change avatar
        View btnChangeAvatar = view.findViewById(R.id.btnAccountChangeAvatar);
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v ->
                Toast.makeText(getContext(), "Change photo coming soon!", Toast.LENGTH_SHORT).show());
        }

        // Update Password
        TextView btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword);
        if (btnUpdatePassword != null) {
            btnUpdatePassword.setOnClickListener(v ->
                Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT).show());
        }

        // Save Changes
        View btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v ->
                Toast.makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show());
        }

        // Account rows
        LinearLayout rowDetails = view.findViewById(R.id.accountRowDetails);
        if (rowDetails != null) {
            rowDetails.setOnClickListener(v ->
                Toast.makeText(getContext(), "Personal details coming soon!", Toast.LENGTH_SHORT).show());
        }

        LinearLayout rowNotifPrefs = view.findViewById(R.id.accountRowNotifPrefs);
        if (rowNotifPrefs != null) {
            rowNotifPrefs.setOnClickListener(v ->
                Toast.makeText(getContext(), "Notification preferences coming soon!", Toast.LENGTH_SHORT).show());
        }

        LinearLayout rowManagePets = view.findViewById(R.id.accountRowManagePets);
        if (rowManagePets != null) {
            rowManagePets.setOnClickListener(v -> dismiss());
        }

        // Delete Account
        TextView btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }

        // Log out
        LinearLayout btnLogout = view.findViewById(R.id.accountBtnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Signing Out?")
            .setMessage("Your pet care schedules, reminders, and health journals remain synced safely in the cloud.")
            .setPositiveButton("Confirm Log Out", (dialog, which) -> {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you certain you want to initiate account deletion? This action is permanent and cannot be undone.")
            .setPositiveButton("Delete Account", (dialog, which) ->
                Toast.makeText(getContext(), "Deletion request received.", Toast.LENGTH_SHORT).show())
            .setNegativeButton("Cancel", null)
            .show();
    }
}
