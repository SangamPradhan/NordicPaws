package com.sangampradhan.nordicpaws.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sangampradhan.nordicpaws.LoginActivity;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.User;

public class ProfileFragment extends Fragment {

    private ImageView userAvatarImage;
    private FrameLayout btnChangeAvatar;
    private TextView userNameText;
    private TextView userEmailText;
    private TextView btnEditProfilePill;

    private LinearLayout optionAccountDetails;
    private LinearLayout optionNotifications;
    private LinearLayout optionManagePets;
    private LinearLayout optionDelegatedContacts;
    private LinearLayout optionHelpSupport;
    private CardView btnProfileLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        userAvatarImage = view.findViewById(R.id.userAvatarImage);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        userNameText = view.findViewById(R.id.userNameText);
        userEmailText = view.findViewById(R.id.userEmailText);
        btnEditProfilePill = view.findViewById(R.id.btnEditProfilePill);

        optionAccountDetails = view.findViewById(R.id.optionAccountDetails);
        optionNotifications = view.findViewById(R.id.optionNotifications);
        optionManagePets = view.findViewById(R.id.optionManagePets);
        optionDelegatedContacts = view.findViewById(R.id.optionDelegatedContacts);
        optionHelpSupport = view.findViewById(R.id.optionHelpSupport);
        btnProfileLogout = view.findViewById(R.id.btnProfileLogout);

        loadUserData();

        // Edit profile opens AccountDetails
        btnEditProfilePill.setOnClickListener(v -> {
            AccountDetailsBottomSheetFragment sheet = AccountDetailsBottomSheetFragment.newInstance();
            sheet.show(getParentFragmentManager(), "AccountDetailsSheet");
        });

        // Change avatar from profile fragment also opens AccountDetails
        btnChangeAvatar.setOnClickListener(v -> {
            AccountDetailsBottomSheetFragment sheet = AccountDetailsBottomSheetFragment.newInstance();
            sheet.show(getParentFragmentManager(), "AccountDetailsSheet");
        });

        LinearLayout optionExpenseTracker = view.findViewById(R.id.optionExpenseTracker);
        if (optionExpenseTracker != null) {
            optionExpenseTracker.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new ExpenseTrackerFragment(), true);
                }
            });
        }

        optionAccountDetails.setOnClickListener(v -> {
            AccountDetailsBottomSheetFragment sheet = AccountDetailsBottomSheetFragment.newInstance();
            sheet.show(getParentFragmentManager(), "AccountDetailsSheet");
        });
        optionNotifications.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new NotificationsFragment(), true);
            }
        });
        optionManagePets.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new PetProfileListFragment(), true);
            }
        });
        optionDelegatedContacts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new CareDelegationFragment(), true);
            }
        });
        if (optionHelpSupport != null)
            optionHelpSupport.setOnClickListener(v -> {
            });

        btnProfileLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadUserData() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null)
            return;

        // Set email immediately from FirebaseAuth (always available)
        if (userEmailText != null && fUser.getEmail() != null) {
            userEmailText.setText(fUser.getEmail());
        }

        // Show displayName instantly as placeholder while Firestore loads
        if (fUser.getDisplayName() != null && !fUser.getDisplayName().isEmpty()) {
            if (userNameText != null)
                userNameText.setText(fUser.getDisplayName());
        }

        // Fetch Firestore profile
        mFirestore.collection("users").document(fUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded())
                        return;
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Prefer Firestore fullName, fall back to Auth displayName
                            String name = user.getFullName();
                            if (name == null || name.isEmpty()) {
                                name = fUser.getDisplayName() != null ? fUser.getDisplayName() : "User Name";
                            }
                            if (userNameText != null)
                                userNameText.setText(name);

                            // Load profile image
                            if (user.getLocalProfileImagePath() != null && !user.getLocalProfileImagePath().isEmpty()) {
                                try {
                                    userAvatarImage.setImageURI(Uri.parse(user.getLocalProfileImagePath()));
                                } catch (Exception e) {
                                    userAvatarImage.setImageResource(R.drawable.person);
                                }
                            } else {
                                userAvatarImage.setImageResource(R.drawable.person);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (userAvatarImage != null)
                        userAvatarImage.setImageResource(R.drawable.person);
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Signing Out?")
                .setMessage("Your pet care schedules and health journals remain synced safely in the cloud.")
                .setPositiveButton("Confirm Log Out", (dialog, which) -> {
                    mAuth.signOut();
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("PROFILE");
        }
        // Refresh data when returning from AccountDetails
        loadUserData();
    }
}
