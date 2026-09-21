package com.sangampradhan.nordicpaws.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sangampradhan.nordicpaws.R;

import java.util.Arrays;
import java.util.List;

public class AddLocationBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText etPlaceName;
    private EditText etPlaceAddress;
    private EditText etPlaceNotes;
    private LinearLayout placeCategoryChipsContainer;
    private AppCompatButton btnSavePlace;

    private String selectedCategory = "Park";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_location_bottom_sheet, container, false);

        etPlaceName = view.findViewById(R.id.etPlaceName);
        etPlaceAddress = view.findViewById(R.id.etPlaceAddress);
        etPlaceNotes = view.findViewById(R.id.etPlaceNotes);
        placeCategoryChipsContainer = view.findViewById(R.id.placeCategoryChipsContainer);
        btnSavePlace = view.findViewById(R.id.btnSavePlace);

        setupCategoryChips();

        btnSavePlace.setOnClickListener(v -> {
            String name = etPlaceName.getText().toString().trim();
            if (name.isEmpty()) {
                etPlaceName.setError("Required");
                return;
            }
            Toast.makeText(getContext(), name + " saved to " + selectedCategory + " places", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void setupCategoryChips() {
        placeCategoryChipsContainer.removeAllViews();
        List<String> categories = Arrays.asList("Vet", "Grooming", "Park", "Store", "Shelter");

        for (String cat : categories) {
            TextView chip = new TextView(getContext());
            chip.setText(cat);
            chip.setTextSize(12);
            chip.setPadding(36, 16, 36, 16);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);

            if (cat.equals(selectedCategory)) {
                chip.setBackgroundResource(R.drawable.bg_rounded_input);
                chip.setBackgroundColor(Color.parseColor("#121316"));
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_rounded_input);
                chip.setTextColor(Color.parseColor("#475569"));
            }

            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                setupCategoryChips();
            });

            placeCategoryChipsContainer.addView(chip);
        }
    }
}
