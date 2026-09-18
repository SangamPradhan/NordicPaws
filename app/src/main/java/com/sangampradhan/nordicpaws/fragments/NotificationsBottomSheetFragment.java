package com.sangampradhan.nordicpaws.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sangampradhan.nordicpaws.R;

public class NotificationsBottomSheetFragment extends BottomSheetDialogFragment {

    public static NotificationsBottomSheetFragment newInstance() {
        return new NotificationsBottomSheetFragment();
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
        return inflater.inflate(R.layout.activity_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnClose = view.findViewById(R.id.btnCloseNotifications);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
    }
}
