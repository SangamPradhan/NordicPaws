package com.sangampradhan.nordicpaws.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;

public class PlacesFragment extends Fragment {

    private FloatingActionButton fabAddLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        fabAddLocation = view.findViewById(R.id.fabAddLocation);
        fabAddLocation.setOnClickListener(v -> {
            AddLocationBottomSheetFragment addLocationSheet = new AddLocationBottomSheetFragment();
            addLocationSheet.show(getParentFragmentManager(), "AddLocationBottomSheet");
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("PLACES");
        }
    }
}
