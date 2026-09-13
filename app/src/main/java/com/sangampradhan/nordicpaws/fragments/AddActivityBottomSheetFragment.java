package com.sangampradhan.nordicpaws.fragments;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.utils.DummyData;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddActivityBottomSheetFragment extends BottomSheetDialogFragment {

    private LinearLayout petSelectorPill;
    private ImageView selectedPetAvatar;
    private TextView selectedPetName;

    private TextView btnRecurrenceDaily;
    private TextView btnRecurrenceWeekly;
    private TextView btnRecurrenceMonthly;
    private TextView btnRecurrenceSpecificDate;

    private LinearLayout datePickerPill;
    private TextView tvScheduledDate;

    private LinearLayout btnAddMoreTask;
    private LinearLayout bulkTasksContainer;
    private AppCompatButton btnSaveTask;

    private String selectedRecurrence = "Daily";
    private List<String> categoryOptions = Arrays.asList("Feeding", "Exercise", "Grooming", "Medication", "Healthcare");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_activity_bottom_sheet, container, false);

        petSelectorPill = view.findViewById(R.id.petSelectorPill);
        selectedPetAvatar = view.findViewById(R.id.selectedPetAvatar);
        selectedPetName = view.findViewById(R.id.selectedPetName);

        btnRecurrenceDaily = view.findViewById(R.id.btnRecurrenceDaily);
        btnRecurrenceWeekly = view.findViewById(R.id.btnRecurrenceWeekly);
        btnRecurrenceMonthly = view.findViewById(R.id.btnRecurrenceMonthly);
        btnRecurrenceSpecificDate = view.findViewById(R.id.btnRecurrenceSpecificDate);

        datePickerPill = view.findViewById(R.id.datePickerPill);
        tvScheduledDate = view.findViewById(R.id.tvScheduledDate);

        btnAddMoreTask = view.findViewById(R.id.btnAddMoreTask);
        bulkTasksContainer = view.findViewById(R.id.bulkTasksContainer);
        btnSaveTask = view.findViewById(R.id.btnSaveTask);

        // Pet Switcher Popup
        petSelectorPill.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), petSelectorPill);
            List<Pet> pets = DummyData.getPets();
            for (int i = 0; i < pets.size(); i++) {
                popup.getMenu().add(0, i, 0, pets.get(i).getName());
            }
            popup.setOnMenuItemClickListener(item -> {
                Pet p = pets.get(item.getItemId());
                selectedPetName.setText(p.getName());
                selectedPetAvatar.setImageResource(p.getAvatarResId());
                return true;
            });
            popup.show();
        });

        // Recurrence Handlers
        btnRecurrenceDaily.setOnClickListener(v -> selectRecurrence("Daily"));
        btnRecurrenceWeekly.setOnClickListener(v -> selectRecurrence("Weekly"));
        btnRecurrenceMonthly.setOnClickListener(v -> selectRecurrence("Monthly"));
        btnRecurrenceSpecificDate.setOnClickListener(v -> selectRecurrence("Specific Date"));

        // Default set Recurrence pill styling
        selectRecurrence("Daily");

        // Dynamic Add Task Row Handler
        btnAddMoreTask.setOnClickListener(v -> addBulkTaskRow("Feeding", "", "08:00 AM", ""));

        // Add 2 default initial task rows with internal dropdowns
        addBulkTaskRow("Feeding", "Morning organic kibble & probiotic", "08:00 AM", "1.5 scoops kibble");
        addBulkTaskRow("Exercise", "Evening neighborhood walk", "06:00 PM", "30 mins walk in park");

        // Save Action
        btnSaveTask.setOnClickListener(v -> {
            int taskCount = bulkTasksContainer.getChildCount();
            if (taskCount == 0) {
                Toast.makeText(getContext(), "Please add at least one task", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), taskCount + " task(s) created under " + selectedRecurrence + " schedule", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void addBulkTaskRow(String defaultCategory, String defaultTitle, String defaultTime, String defaultNotes) {
        if (getContext() == null) return;
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_bulk_task_row, bulkTasksContainer, false);

        Spinner spinnerCategory = row.findViewById(R.id.spinnerTaskCategory);
        EditText etTitle = row.findViewById(R.id.etBulkTaskTitle);
        TextView tvTime = row.findViewById(R.id.tvBulkScheduledTime);
        EditText etNotes = row.findViewById(R.id.etBulkTaskNotes);
        LinearLayout timePickerPill = row.findViewById(R.id.bulkTimePickerPill);
        ImageButton btnRemove = row.findViewById(R.id.btnRemoveBulkTask);

        // Setup Dropdown Category Spinner inside task container
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions);
        spinnerCategory.setAdapter(adapter);
        int catIndex = categoryOptions.indexOf(defaultCategory);
        if (catIndex >= 0) spinnerCategory.setSelection(catIndex);

        etTitle.setText(defaultTitle);
        tvTime.setText(defaultTime);
        etNotes.setText(defaultNotes);

        // Native Android TimePickerDialog
        timePickerPill.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view, selectedHour, selectedMinute) -> {
                        String amPm = selectedHour >= 12 ? "PM" : "AM";
                        int hourIn12Format = selectedHour % 12;
                        if (hourIn12Format == 0) hourIn12Format = 12;
                        String formattedTime = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm);
                        tvTime.setText(formattedTime);
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        btnRemove.setOnClickListener(v -> bulkTasksContainer.removeView(row));

        bulkTasksContainer.addView(row);
    }

    private void selectRecurrence(String mode) {
        selectedRecurrence = mode;
        int activeBg = Color.parseColor("#F9E38A");
        int defaultBg = Color.parseColor("#F1F5F9");
        int activeText = Color.parseColor("#121316");
        int defaultText = Color.parseColor("#475569");

        btnRecurrenceDaily.setBackgroundColor("Daily".equals(mode) ? activeBg : defaultBg);
        btnRecurrenceDaily.setTextColor("Daily".equals(mode) ? activeText : defaultText);

        btnRecurrenceWeekly.setBackgroundColor("Weekly".equals(mode) ? activeBg : defaultBg);
        btnRecurrenceWeekly.setTextColor("Weekly".equals(mode) ? activeText : defaultText);

        btnRecurrenceMonthly.setBackgroundColor("Monthly".equals(mode) ? activeBg : defaultBg);
        btnRecurrenceMonthly.setTextColor("Monthly".equals(mode) ? activeText : defaultText);

        btnRecurrenceSpecificDate.setBackgroundColor("Specific Date".equals(mode) ? activeBg : defaultBg);
        btnRecurrenceSpecificDate.setTextColor("Specific Date".equals(mode) ? activeText : defaultText);

        if ("Specific Date".equals(mode) || "Monthly".equals(mode)) {
            datePickerPill.setVisibility(View.VISIBLE);
            tvScheduledDate.setText("Specific Date".equals(mode) ? "Oct 24, 2026 (Vet / One-time)" : "Day 15 of Month");
        } else {
            datePickerPill.setVisibility(View.GONE);
        }
    }
}
