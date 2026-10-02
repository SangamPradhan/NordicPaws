package com.sangampradhan.nordicpaws.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.models.RoutineTask;
import com.sangampradhan.nordicpaws.utils.FirestoreManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    private FirestoreManager firestoreManager;
    private android.hardware.SensorManager sensorManager;
    private com.sangampradhan.nordicpaws.utils.ShakeDetector shakeDetector;
    private List<Pet> userPets = new ArrayList<>();
    private Pet selectedPet = null;

    private String selectedRecurrence = "Daily";
    private String selectedSpecificDate = ""; // Formatted yyyy-MM-dd
    private List<String> selectedWeeklyDays = new ArrayList<>(Arrays.asList("MON")); // Selected preferable days for Weekly
    private int selectedDayOfMonth = 15;     // Day of Month for Monthly (1-31)

    private Runnable onTasksSavedListener;

    private List<String> categoryOptions = Arrays.asList("Feeding", "Exercise", "Grooming", "Medication", "Healthcare");

    public void setOnTasksSavedListener(Runnable listener) {
        this.onTasksSavedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_activity_bottom_sheet, container, false);

        firestoreManager = new FirestoreManager();

        // Shake sensor setup
        if (getContext() != null) {
            sensorManager = (android.hardware.SensorManager) getContext().getSystemService(android.content.Context.SENSOR_SERVICE);
            shakeDetector = new com.sangampradhan.nordicpaws.utils.ShakeDetector();
            shakeDetector.setOnShakeListener(this::resetTaskForm);
        }

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

        // Load Pets dynamically from Firestore
        loadPetsForSelector();

        petSelectorPill.setOnClickListener(v -> showPetSelectorPopup());

        // Recurrence Handlers
        btnRecurrenceDaily.setOnClickListener(v -> selectRecurrence("Daily"));
        btnRecurrenceWeekly.setOnClickListener(v -> selectRecurrence("Weekly"));
        btnRecurrenceMonthly.setOnClickListener(v -> selectRecurrence("Monthly"));
        btnRecurrenceSpecificDate.setOnClickListener(v -> selectRecurrence("Specific Date"));

        // Date / Day Selection Pill Click Handler
        datePickerPill.setOnClickListener(v -> handleScheduleDateSelection());

        selectRecurrence("Daily");

        // Dynamic Add Task Row Handler
        btnAddMoreTask.setOnClickListener(v -> addBulkTaskRow("Feeding", "", "08:00 AM", ""));

        // Initial task row
        addBulkTaskRow("Feeding", "Morning kibble", "08:00 AM", "1.5 scoops kibble");

        // Save Action
        btnSaveTask.setOnClickListener(v -> saveTasksToFirestore());

        return view;
    }

    private void loadPetsForSelector() {
        firestoreManager.fetchPets(pets -> {
            this.userPets = pets;
            if (!pets.isEmpty()) {
                selectedPet = pets.get(0);
                selectedPetName.setText(selectedPet.getName());
                updatePetAvatarDisplay(selectedPet);
            }
        }, null);
    }

    private void updatePetAvatarDisplay(Pet pet) {
        if (pet.getLocalImagePath() != null && !pet.getLocalImagePath().isEmpty()) {
            android.graphics.Bitmap bmp = com.sangampradhan.nordicpaws.utils.LocalStorageManager.loadLocalBitmap(pet.getLocalImagePath());
            if (bmp != null) {
                selectedPetAvatar.setImageBitmap(bmp);
                return;
            }
        }
        int resId = pet.getAvatarResId() != 0 ? pet.getAvatarResId() : R.drawable.dog;
        selectedPetAvatar.setImageResource(resId);
    }

    private void showPetSelectorPopup() {
        if (userPets.isEmpty()) return;
        PopupMenu popup = new PopupMenu(getContext(), petSelectorPill);
        for (int i = 0; i < userPets.size(); i++) {
            popup.getMenu().add(0, i, 0, userPets.get(i).getName());
        }
        popup.setOnMenuItemClickListener(item -> {
            selectedPet = userPets.get(item.getItemId());
            selectedPetName.setText(selectedPet.getName());
            updatePetAvatarDisplay(selectedPet);
            return true;
        });
        popup.show();
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

        if ("Specific Date".equals(mode)) {
            datePickerPill.setVisibility(View.VISIBLE);
            if (selectedSpecificDate.isEmpty()) {
                selectedSpecificDate = RoutineTask.getTodayDateString();
            }
            tvScheduledDate.setText("Date: " + selectedSpecificDate + " (Tap to pick date)");
        } else if ("Weekly".equals(mode)) {
            datePickerPill.setVisibility(View.VISIBLE);
            tvScheduledDate.setText("Days: " + TextUtils.join(", ", selectedWeeklyDays) + " (Tap to select days)");
        } else if ("Monthly".equals(mode)) {
            datePickerPill.setVisibility(View.VISIBLE);
            tvScheduledDate.setText("Day: " + selectedDayOfMonth + "th of month (Tap to change)");
        } else {
            datePickerPill.setVisibility(View.GONE);
        }
    }

    private void handleScheduleDateSelection() {
        if ("Specific Date".equals(selectedRecurrence)) {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year, month, dayOfMonth);
                selectedSpecificDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.getTime());
                tvScheduledDate.setText("Date: " + selectedSpecificDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        } else if ("Weekly".equals(selectedRecurrence)) {
            String[] days = new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
            boolean[] checkedDays = new boolean[days.length];
            for (int i = 0; i < days.length; i++) {
                checkedDays[i] = selectedWeeklyDays.contains(days[i]);
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Select Preferable Days")
                    .setMultiChoiceItems(days, checkedDays, (dialog, which, isChecked) -> {
                        checkedDays[which] = isChecked;
                    })
                    .setPositiveButton("OK", (dialog, which) -> {
                        selectedWeeklyDays.clear();
                        for (int i = 0; i < days.length; i++) {
                            if (checkedDays[i]) {
                                selectedWeeklyDays.add(days[i]);
                            }
                        }
                        if (selectedWeeklyDays.isEmpty()) selectedWeeklyDays.add("MON");
                        tvScheduledDate.setText("Days: " + TextUtils.join(", ", selectedWeeklyDays) + " (Tap to select days)");
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else if ("Monthly".equals(selectedRecurrence)) {
            String[] monthDays = new String[31];
            for (int i = 1; i <= 31; i++) monthDays[i - 1] = "Day " + i;
            new AlertDialog.Builder(getContext())
                    .setTitle("Select Day of Month")
                    .setItems(monthDays, (dialog, which) -> {
                        selectedDayOfMonth = which + 1;
                        tvScheduledDate.setText("Day: " + selectedDayOfMonth + "th of month (Tap to change)");
                    })
                    .show();
        }
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions);
        spinnerCategory.setAdapter(adapter);
        int catIndex = categoryOptions.indexOf(defaultCategory);
        if (catIndex >= 0) spinnerCategory.setSelection(catIndex);

        etTitle.setText(defaultTitle);
        tvTime.setText(defaultTime);
        etNotes.setText(defaultNotes);

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

    private void saveTasksToFirestore() {
        if (selectedPet == null) {
            Toast.makeText(getContext(), "Please select a pet profile first", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = bulkTasksContainer.getChildCount();
        if (count == 0) {
            Toast.makeText(getContext(), "Please add at least one task row", Toast.LENGTH_SHORT).show();
            return;
        }

        int savedTasksCount = 0;
        for (int i = 0; i < count; i++) {
            View row = bulkTasksContainer.getChildAt(i);
            Spinner spinnerCategory = row.findViewById(R.id.spinnerTaskCategory);
            EditText etTitle = row.findViewById(R.id.etBulkTaskTitle);
            TextView tvTime = row.findViewById(R.id.tvBulkScheduledTime);

            String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Feeding";
            String title = etTitle.getText().toString().trim();
            String time = tvTime.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Title is required");
                etTitle.requestFocus();
                return;
            }

            String taskId = "t_" + UUID.randomUUID().toString().substring(0, 8);
            RoutineTask task = new RoutineTask();
            task.setId(taskId);
            task.setPetId(selectedPet.getId());
            task.setTitle(title);
            task.setTime(time.isEmpty() ? "08:00 AM" : time);
            task.setCategory(category);
            task.setCompleted(false);

            // Set Schedule Frequencies & Selected Days / Dates
            if ("Daily".equals(selectedRecurrence)) {
                task.setScheduleType("DAILY");
            } else if ("Weekly".equals(selectedRecurrence)) {
                task.setScheduleType("SPECIFIC_DAYS");
                task.setDaysOfWeek(new ArrayList<>(selectedWeeklyDays));
            } else if ("Monthly".equals(selectedRecurrence)) {
                task.setScheduleType("MONTHLY");
                task.setDaysOfWeek(Collections.singletonList(String.valueOf(selectedDayOfMonth)));
            } else if ("Specific Date".equals(selectedRecurrence)) {
                task.setScheduleType("SPECIFIC_DATE");
                task.setDaysOfWeek(Collections.singletonList(selectedSpecificDate));
            }

            firestoreManager.saveRoutineTask(task, null, null);
            savedTasksCount++;
        }

        Toast.makeText(getContext(), savedTasksCount + " task(s) created for " + selectedPet.getName() + "! 🐾", Toast.LENGTH_LONG).show();
        if (onTasksSavedListener != null) {
            onTasksSavedListener.run();
        }
        dismiss();
    }

    /**
     * Resets the entire task form to its default state.
     * Triggered by the ShakeDetector gesture.
     */
    private void resetTaskForm() {
        // Clear all task rows and re-add the default one
        if (bulkTasksContainer != null) {
            bulkTasksContainer.removeAllViews();
            addBulkTaskRow("Feeding", "", "08:00 AM", "");
        }

        // Reset recurrence to Daily
        selectedRecurrence = "Daily";
        selectedSpecificDate = "";
        selectedWeeklyDays = new ArrayList<>(Arrays.asList("MON"));
        selectedDayOfMonth = 15;
        selectRecurrence("Daily");

        // Reset pet selector to first pet
        if (!userPets.isEmpty()) {
            selectedPet = userPets.get(0);
            selectedPetName.setText(selectedPet.getName());
            updatePetAvatarDisplay(selectedPet);
        }

        Toast.makeText(getContext(), "Form reset! 🔄", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && shakeDetector != null) {
            android.hardware.Sensor accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(shakeDetector, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }
}
