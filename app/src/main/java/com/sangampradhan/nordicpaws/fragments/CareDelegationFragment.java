package com.sangampradhan.nordicpaws.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;

public class CareDelegationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_care_delegation, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackDelegation);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        AppCompatButton btnSendDelegation = view.findViewById(R.id.btnSendDelegation);
        if (btnSendDelegation != null) {
            btnSendDelegation.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Delegation plan sent to Clara via SMS!", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("DELEGATE");
        }
    }
}
