package com.sangampradhan.nordicpaws.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.sangampradhan.nordicpaws.LoginActivity;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

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

        // Click listeners for settings items
        View.OnClickListener stubListener = v -> {
            Toast.makeText(getContext(), "Opening section details...", Toast.LENGTH_SHORT).show();
        };

        btnChangeAvatar.setOnClickListener(v -> Toast.makeText(getContext(), "Change profile picture", Toast.LENGTH_SHORT).show());
        btnEditProfilePill.setOnClickListener(v -> Toast.makeText(getContext(), "Editing User Profile", Toast.LENGTH_SHORT).show());

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
        optionHelpSupport.setOnClickListener(stubListener);

        // Log out handler
        btnProfileLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("PROFILE");
        }
    }
}
