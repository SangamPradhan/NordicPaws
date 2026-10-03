package com.sangampradhan.nordicpaws.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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

    private FirestoreManager firestoreManager;
    private List<Pet> userPets = new ArrayList<>();
    private Pet selectedPet = null;

    private String selectedCategory = "Feeding";
    private String selectedRecurrence = "Daily";
    private String selectedSpecificDate = "";
    private List<String> selectedWeeklyDays = new ArrayList<>(Arrays.asList("MON"));
    private int selectedDayOfMonth = 15;

    private Runnable onTaskUpdatedListener;

    public void setOnTaskUpdatedListener(Runnable listener) {
        this.onTaskUpdatedListener = listener;
    }

    public static EditTaskBottomSheetFragment newInstance(RoutineTask task) {
        EditTaskBottomSheetFragment fragment = new EditTaskBottomSheetFragment();
        fragment.taskToEdit = task;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task_bottom_sheet, container, false);

        firestoreManager = new FirestoreManager();

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

        // Pre-populate fields from the task being edited
        if (taskToEdit != null) {
            etEditTaskTitle.setText(taskToEdit.getTitle());
            tvEditScheduledTime.setText(taskToEdit.getTime());
            selectedCategory = taskToEdit.getCategory() != null ? taskToEdit.getCategory() : "Feeding";

            // Map scheduleType back to recurrence mode
            String schedType = taskToEdit.getScheduleType();
            if ("SPECIFIC_DAYS".equalsIgnoreCase(schedType) || "WEEKLY".equalsIgnoreCase(schedType)) {
                selectedRecurrence = "Weekly";
                if (taskToEdit.getDaysOfWeek() != null && !taskToEdit.getDaysOfWeek().isEmpty()) {
                    selectedWeeklyDays = new ArrayList<>(taskToEdit.getDaysOfWeek());
                }
            } else if ("MONTHLY".equalsIgnoreCase(schedType)) {
                selectedRecurrence = "Monthly";
                if (taskToEdit.getDaysOfWeek() != null && !taskToEdit.getDaysOfWeek().isEmpty()) {
                    try {
                        selectedDayOfMonth = Integer.parseInt(taskToEdit.getDaysOfWeek().get(0));
                    } catch (Exception ignored) {}
                }
            } else if ("ONCE".equalsIgnoreCase(schedType) || "SPECIFIC_DATE".equalsIgnoreCase(schedType)) {
                selectedRecurrence = "Specific Date";
                if (taskToEdit.getDaysOfWeek() != null && !taskToEdit.getDaysOfWeek().isEmpty()) {
                    selectedSpecificDate = taskToEdit.getDaysOfWeek().get(0);
                }
            } else {
                selectedRecurrence = "Daily";
            }
        }

        // Load pets from Firestore for the pet switcher
        loadPetsForSelector();

        // Pet Switcher Popup
        editPetSelectorPill.setOnClickListener(v -> showPetSelectorPopup());

        // Setup Category Chips
        setupCategoryChips();

        // TimePicker dialog trigger
        editTimePickerPill.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(getContext(),
                    (view1, selectedHour, selectedMinute) -> {
                        String amPm = selectedHour >= 12 ? "PM" : "AM";
                        int hourIn12Format = selectedHour % 12;
                        if (hourIn12Format == 0) hourIn12Format = 12;
                        String formattedTime = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm);
                        tvEditScheduledTime.setText(formattedTime);
                    }, hour, minute, false);
            dialog.show();
        });

        // Date Picker Pill click handler
        editDatePickerPill.setOnClickListener(v -> handleScheduleDateSelection());

        // Recurrence Click Handlers
        btnEditRecurrenceDaily.setOnClickListener(v -> selectRecurrence("Daily"));
        btnEditRecurrenceWeekly.setOnClickListener(v -> selectRecurrence("Weekly"));
        btnEditRecurrenceMonthly.setOnClickListener(v -> selectRecurrence("Monthly"));
        btnEditRecurrenceSpecificDate.setOnClickListener(v -> selectRecurrence("Specific Date"));

        // Apply initial recurrence selection
        selectRecurrence(selectedRecurrence);

        // UPDATE ACTION — saves changes back to Firestore
        btnUpdateTask.setOnClickListener(v -> updateTaskInFirestore());

        // DELETE ACTION — permanently deletes the task from Firestore
        btnDeleteTaskSchedule.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Task Permanently")
                    .setMessage("This will permanently remove \"" + taskToEdit.getTitle() + "\" from the schedule. This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        firestoreManager.deleteTask(taskToEdit.getPetId(), taskToEdit.getId(),
                                aVoid -> {
                                    Toast.makeText(getContext(), "Task deleted permanently 🗑️", Toast.LENGTH_SHORT).show();
                                    if (onTaskUpdatedListener != null) onTaskUpdatedListener.run();
                                    dismiss();
                                },
                                e -> Toast.makeText(getContext(), "Failed to delete: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void loadPetsForSelector() {
        firestoreManager.fetchPets(pets -> {
            this.userPets = pets;
            // Find and select the pet that owns this task
            if (taskToEdit != null) {
                for (Pet p : pets) {
                    if (p.getId() != null && p.getId().equals(taskToEdit.getPetId())) {
                        selectedPet = p;
                        break;
                    }
                }
            }
            if (selectedPet == null && !pets.isEmpty()) {
                selectedPet = pets.get(0);
            }
            if (selectedPet != null) {
                editSelectedPetName.setText(selectedPet.getName());
                updatePetAvatarDisplay(selectedPet);
            }
        }, null);
    }

    private void updatePetAvatarDisplay(Pet pet) {
        if (pet.getLocalImagePath() != null && !pet.getLocalImagePath().isEmpty()) {
            android.graphics.Bitmap bmp = com.sangampradhan.nordicpaws.utils.LocalStorageManager.loadLocalBitmap(pet.getLocalImagePath());
            if (bmp != null) {
                editSelectedPetAvatar.setImageBitmap(bmp);
                return;
            }
        }
        int resId = pet.getAvatarResId() != 0 ? pet.getAvatarResId() : R.drawable.dog;
        editSelectedPetAvatar.setImageResource(resId);
    }

    private void showPetSelectorPopup() {
        if (userPets.isEmpty()) return;
        PopupMenu popup = new PopupMenu(getContext(), editPetSelectorPill);
        for (int i = 0; i < userPets.size(); i++) {
            popup.getMenu().add(0, i, 0, userPets.get(i).getName());
        }
        popup.setOnMenuItemClickListener(item -> {
            selectedPet = userPets.get(item.getItemId());
            editSelectedPetName.setText(selectedPet.getName());
            updatePetAvatarDisplay(selectedPet);
            return true;
        });
        popup.show();
    }

    private void updateTaskInFirestore() {
        if (taskToEdit == null) return;

        String title = etEditTaskTitle.getText().toString().trim();
        String time = tvEditScheduledTime.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etEditTaskTitle.setError("Title is required");
            etEditTaskTitle.requestFocus();
            return;
        }

        taskToEdit.setTitle(title);
        taskToEdit.setTime(time.isEmpty() ? "08:00 AM" : time);
        taskToEdit.setCategory(selectedCategory);

        // Update pet assignment if switched
        if (selectedPet != null) {
            taskToEdit.setPetId(selectedPet.getId());
        }

        // Set schedule type & frequency data
        if ("Daily".equals(selectedRecurrence)) {
            taskToEdit.setScheduleType("DAILY");
            taskToEdit.setDaysOfWeek(null);
        } else if ("Weekly".equals(selectedRecurrence)) {
            taskToEdit.setScheduleType("SPECIFIC_DAYS");
            taskToEdit.setDaysOfWeek(new ArrayList<>(selectedWeeklyDays));
        } else if ("Monthly".equals(selectedRecurrence)) {
            taskToEdit.setScheduleType("MONTHLY");
            taskToEdit.setDaysOfWeek(Collections.singletonList(String.valueOf(selectedDayOfMonth)));
        } else if ("Specific Date".equals(selectedRecurrence)) {
            taskToEdit.setScheduleType("SPECIFIC_DATE");
            taskToEdit.setDaysOfWeek(Collections.singletonList(selectedSpecificDate));
        }

        btnUpdateTask.setEnabled(false);
        btnUpdateTask.setText("Updating...");

        firestoreManager.saveRoutineTask(taskToEdit,
                aVoid -> {
                    Toast.makeText(getContext(), "Task updated successfully ✅", Toast.LENGTH_SHORT).show();
                    if (onTaskUpdatedListener != null) onTaskUpdatedListener.run();
                    dismiss();
                },
                e -> {
                    btnUpdateTask.setEnabled(true);
                    btnUpdateTask.setText("Update Task");
                    Toast.makeText(getContext(), "Failed to update: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
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

        // Update date picker pill visibility and text
        if ("Specific Date".equals(mode)) {
            editDatePickerPill.setVisibility(View.VISIBLE);
            if (selectedSpecificDate.isEmpty()) {
                selectedSpecificDate = RoutineTask.getTodayDateString();
            }
            tvEditScheduledDate.setText("Date: " + selectedSpecificDate + " (Tap to pick date)");
        } else if ("Weekly".equals(mode)) {
            editDatePickerPill.setVisibility(View.VISIBLE);
            tvEditScheduledDate.setText("Days: " + TextUtils.join(", ", selectedWeeklyDays) + " (Tap to select days)");
        } else if ("Monthly".equals(mode)) {
            editDatePickerPill.setVisibility(View.VISIBLE);
            tvEditScheduledDate.setText("Day: " + selectedDayOfMonth + "th of month (Tap to change)");
        } else {
            editDatePickerPill.setVisibility(View.GONE);
        }
    }

    private void handleScheduleDateSelection() {
        if ("Specific Date".equals(selectedRecurrence)) {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year, month, dayOfMonth);
                selectedSpecificDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.getTime());
                tvEditScheduledDate.setText("Date: " + selectedSpecificDate);
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
                        tvEditScheduledDate.setText("Days: " + TextUtils.join(", ", selectedWeeklyDays) + " (Tap to select days)");
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
                        tvEditScheduledDate.setText("Day: " + selectedDayOfMonth + "th of month (Tap to change)");
                    })
                    .show();
        }
    }
}
