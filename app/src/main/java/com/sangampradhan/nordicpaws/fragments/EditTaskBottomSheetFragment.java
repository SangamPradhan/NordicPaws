package com.sangampradhan.nordicpaws.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.models.RoutineTask;
import com.sangampradhan.nordicpaws.utils.DummyData;

import java.util.Arrays;
import java.util.List;

public class EditTaskBottomSheetFragment extends BottomSheetDialogFragment {

    private RoutineTask taskToEdit;
    
    private LinearLayout editPetSelectorPill;
    private ImageView editSelectedPetAvatar;
    private TextView editSelectedPetName;
    private LinearLayout editCategoryChipsContainer;
    private EditText etEditTaskTitle;
    
    private TextView btnEditRecurrenceDaily;
    private TextView btnEditRecurrenceWeekly;
    private TextView btnEditRecurrenceMonthly;
    private TextView btnEditRecurrenceSpecificDate;
    
    private LinearLayout editTimePickerPill;
    private TextView tvEditScheduledTime;
    private LinearLayout editDatePickerPill;
    private TextView tvEditScheduledDate;
    
    private EditText etEditTaskNotes;
    private AppCompatButton btnUpdateTask;
    private AppCompatButton btnDeleteTaskSchedule;

    private String selectedCategory = "Feeding";
    private String selectedRecurrence = "Daily";

    public static EditTaskBottomSheetFragment newInstance(RoutineTask task) {
        EditTaskBottomSheetFragment fragment = new EditTaskBottomSheetFragment();
        fragment.taskToEdit = task;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task_bottom_sheet, container, false);

        editPetSelectorPill = view.findViewById(R.id.editPetSelectorPill);
        editSelectedPetAvatar = view.findViewById(R.id.editSelectedPetAvatar);
        editSelectedPetName = view.findViewById(R.id.editSelectedPetName);
        editCategoryChipsContainer = view.findViewById(R.id.editCategoryChipsContainer);
        etEditTaskTitle = view.findViewById(R.id.etEditTaskTitle);
        
        btnEditRecurrenceDaily = view.findViewById(R.id.btnEditRecurrenceDaily);
        btnEditRecurrenceWeekly = view.findViewById(R.id.btnEditRecurrenceWeekly);
        btnEditRecurrenceMonthly = view.findViewById(R.id.btnEditRecurrenceMonthly);
        btnEditRecurrenceSpecificDate = view.findViewById(R.id.btnEditRecurrenceSpecificDate);
        
        editTimePickerPill = view.findViewById(R.id.editTimePickerPill);
        tvEditScheduledTime = view.findViewById(R.id.tvEditScheduledTime);
        editDatePickerPill = view.findViewById(R.id.editDatePickerPill);
        tvEditScheduledDate = view.findViewById(R.id.tvEditScheduledDate);
        
        etEditTaskNotes = view.findViewById(R.id.etEditTaskNotes);
        btnUpdateTask = view.findViewById(R.id.btnUpdateTask);
        btnDeleteTaskSchedule = view.findViewById(R.id.btnDeleteTaskSchedule);

        // Populate fields if task given
        if (taskToEdit != null) {
            etEditTaskTitle.setText(taskToEdit.getTitle());
            tvEditScheduledTime.setText(taskToEdit.getTime());
            selectedCategory = taskToEdit.getCategory();
        }

        // Pet Switcher Popup
        editPetSelectorPill.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), editPetSelectorPill);
            List<Pet> pets = DummyData.getPets();
            for (int i = 0; i < pets.size(); i++) {
                popup.getMenu().add(0, i, 0, pets.get(i).getName());
            }
            popup.setOnMenuItemClickListener(item -> {
                Pet p = pets.get(item.getItemId());
                editSelectedPetName.setText(p.getName());
                editSelectedPetAvatar.setImageResource(p.getAvatarResId());
                return true;
            });
            popup.show();
        });

        // Setup Category Chips
        setupCategoryChips();

        // TimePicker dialog trigger
        editTimePickerPill.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = calendar.get(java.util.Calendar.MINUTE);

            android.app.TimePickerDialog dialog = new android.app.TimePickerDialog(getContext(),
                    (view1, selectedHour, selectedMinute) -> {
                        String amPm = selectedHour >= 12 ? "PM" : "AM";
                        int hourIn12Format = selectedHour % 12;
                        if (hourIn12Format == 0) hourIn12Format = 12;
                        String formattedTime = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm);
                        tvEditScheduledTime.setText(formattedTime);
                    }, hour, minute, false);
            dialog.show();
        });

        // Recurrence Click Handlers
        btnEditRecurrenceDaily.setOnClickListener(v -> selectRecurrence("Daily"));
        btnEditRecurrenceWeekly.setOnClickListener(v -> selectRecurrence("Weekly"));
        btnEditRecurrenceMonthly.setOnClickListener(v -> selectRecurrence("Monthly"));
        btnEditRecurrenceSpecificDate.setOnClickListener(v -> selectRecurrence("Specific Date"));

        // Action Handlers
        btnUpdateTask.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // EXCLUSIVE DELETE ACTION
        btnDeleteTaskSchedule.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Task completely deleted from schedule", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void setupCategoryChips() {
        editCategoryChipsContainer.removeAllViews();
        List<String> categories = Arrays.asList("Feeding", "Exercise", "Grooming", "Medication", "Healthcare");
        
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
            
            editCategoryChipsContainer.addView(chip);
        }
    }

    private void selectRecurrence(String mode) {
        selectedRecurrence = mode;
        int activeBg = Color.parseColor("#F9E38A");
        int defaultBg = Color.parseColor("#F1F5F9");
        int activeText = Color.parseColor("#121316");
        int defaultText = Color.parseColor("#475569");

        btnEditRecurrenceDaily.setBackgroundColor("Daily".equals(mode) ? activeBg : defaultBg);
        btnEditRecurrenceDaily.setTextColor("Daily".equals(mode) ? activeText : defaultText);

        btnEditRecurrenceWeekly.setBackgroundColor("Weekly".equals(mode) ? activeBg : defaultBg);
        btnEditRecurrenceWeekly.setTextColor("Weekly".equals(mode) ? activeText : defaultText);

        btnEditRecurrenceMonthly.setBackgroundColor("Monthly".equals(mode) ? activeBg : defaultBg);
        btnEditRecurrenceMonthly.setTextColor("Monthly".equals(mode) ? activeText : defaultText);

        btnEditRecurrenceSpecificDate.setBackgroundColor("Specific Date".equals(mode) ? activeBg : defaultBg);
        btnEditRecurrenceSpecificDate.setTextColor("Specific Date".equals(mode) ? activeText : defaultText);

        if ("Specific Date".equals(mode)) {
            tvEditScheduledDate.setText("Oct 24, 2026 (One-time)");
        } else if ("Monthly".equals(mode)) {
            tvEditScheduledDate.setText("Day 15 of Month");
        } else {
            tvEditScheduledDate.setText("Select Date");
        }
    }
}
