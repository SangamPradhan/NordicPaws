package com.sangampradhan.nordicpaws.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.models.RoutineTask;
import com.sangampradhan.nordicpaws.utils.FirestoreManager;
import com.sangampradhan.nordicpaws.utils.LocalStorageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CareDelegationFragment extends Fragment {

    private static final int SMS_PERMISSION_CODE = 101;

    private TextInputEditText etRecipientPhone;
    private TextView btnSelectAll;
    private CardView btnPickContact;
    private RecyclerView recyclerViewTasks;
    private AppCompatButton btnSendDelegation;

    private LinearLayout delegationPetPill, delegationDatePickerPill;
    private TextView tvDelegationPetName, tvDelegationDate, tvDelegationTasksCount, tvEmptyState;
    private ImageView ivDelegationPetAvatar;

    private FirestoreManager firestoreManager;

    private List<Pet> availablePets = new ArrayList<>();
    private Pet selectedPet = null;
    private String selectedDate = "";

    private final List<DelegationTaskItem> delegationList = new ArrayList<>();
    private DelegationAdapter delegationAdapter;
    private boolean isAllSelected = true;

    public static class DelegationTaskItem {
        RoutineTask task;
        boolean isSelected;

        public DelegationTaskItem(RoutineTask task, boolean isSelected) {
            this.task = task;
            this.isSelected = isSelected;
        }
    }

    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_care_delegation, container, false);
        firestoreManager = new FirestoreManager();

        initViews(view);
        setupLaunchers();
        setupPickers();

        fetchPets();

        return view;
    }

    private void initViews(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnBackDelegation);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        etRecipientPhone = view.findViewById(R.id.et_recipient_phone);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        btnPickContact = view.findViewById(R.id.btn_pick_contact);
        recyclerViewTasks = view.findViewById(R.id.recycler_delegation_tasks);
        btnSendDelegation = view.findViewById(R.id.btnSendDelegation);

        delegationPetPill = view.findViewById(R.id.delegationPetPill);
        delegationDatePickerPill = view.findViewById(R.id.delegationDatePickerPill);
        tvDelegationPetName = view.findViewById(R.id.delegationPetName);
        tvDelegationDate = view.findViewById(R.id.tvDelegationDate);
        tvDelegationTasksCount = view.findViewById(R.id.tvDelegationTasksCount);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        ivDelegationPetAvatar = view.findViewById(R.id.delegationPetAvatar);

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        delegationAdapter = new DelegationAdapter(delegationList, this::updateSendButtonState);
        recyclerViewTasks.setAdapter(delegationAdapter);

        btnSelectAll.setOnClickListener(v -> {
            isAllSelected = !isAllSelected;
            for (DelegationTaskItem item : delegationList) {
                item.isSelected = isAllSelected;
            }
            delegationAdapter.notifyDataSetChanged();
            btnSelectAll.setText(isAllSelected ? "Deselect All" : "Select All");
            updateSendButtonState();
        });

        btnSendDelegation.setOnClickListener(v -> checkAndSendSms());
    }

    private void setupLaunchers() {
        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        if (contactUri != null) {
                            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                            try (Cursor cursor = getContext().getContentResolver().query(contactUri, projection, null, null, null)) {
                                if (cursor != null && cursor.moveToFirst()) {
                                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                    String phoneNumber = cursor.getString(numberIndex);
                                    etRecipientPhone.setText(phoneNumber);
                                }
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Failed to read contact", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    String phone = etRecipientPhone.getText() != null ? etRecipientPhone.getText().toString().trim() : "";
                    if (isGranted) {
                        executeDirectBackgroundSms(phone);
                    } else {
                        Toast.makeText(getContext(), "Permission denied. Opening SMS app...", Toast.LENGTH_SHORT).show();
                        executeDirectBackgroundSms(phone);
                    }
                }
        );

        btnPickContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            contactPickerLauncher.launch(intent);
        });
    }

    private void fetchPets() {
        firestoreManager.fetchPets(pets -> {
            availablePets.clear();
            availablePets.addAll(pets);
            if (!availablePets.isEmpty()) {
                selectedPet = availablePets.get(0);
                setDefaultDate();
                updatePetUI();
                fetchTasksFromDatabase();
            } else {
                tvDelegationPetName.setText("No Pets Available");
            }
        }, null);
    }

    private void setDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        // Default to tomorrow
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
        tvDelegationDate.setText("Tomorrow (" + selectedDate + ")");
    }

    private void setupPickers() {
        delegationPetPill.setOnClickListener(v -> {
            if (availablePets.isEmpty()) {
                Toast.makeText(getContext(), "No pets available", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] petNames = new String[availablePets.size()];
            for (int i = 0; i < availablePets.size(); i++) {
                petNames[i] = availablePets.get(i).getName();
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Select Pet")
                    .setItems(petNames, (dialog, which) -> {
                        selectedPet = availablePets.get(which);
                        setDefaultDate();
                        updatePetUI();
                        fetchTasksFromDatabase();
                    })
                    .show();
        });

        delegationDatePickerPill.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                String[] parts = selectedDate.split("/");
                if (parts.length == 3) {
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    calendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
                }
            } catch (Exception ignored) {}

            new DatePickerDialog(getContext(),
                    (picker, year, month, dayOfMonth) -> {
                        selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                        
                        Calendar today = Calendar.getInstance();
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year, month, dayOfMonth);
                        
                        if (today.get(Calendar.YEAR) == year && today.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)) {
                            tvDelegationDate.setText("Today (" + selectedDate + ")");
                        } else {
                            tvDelegationDate.setText(selectedDate);
                        }
                        
                        fetchTasksFromDatabase();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void updatePetUI() {
        if (selectedPet != null) {
            tvDelegationPetName.setText(selectedPet.getName());
            if (selectedPet.getLocalImagePath() != null && !selectedPet.getLocalImagePath().isEmpty()) {
                Bitmap bmp = LocalStorageManager.loadLocalBitmap(selectedPet.getLocalImagePath());
                if (bmp != null) ivDelegationPetAvatar.setImageBitmap(bmp);
                else ivDelegationPetAvatar.setImageResource(selectedPet.getAvatarResId() != 0 ? selectedPet.getAvatarResId() : R.drawable.dog);
            } else {
                ivDelegationPetAvatar.setImageResource(selectedPet.getAvatarResId() != 0 ? selectedPet.getAvatarResId() : R.drawable.dog);
            }
        }
    }

    private void fetchTasksFromDatabase() {
        if (selectedPet == null) return;
        
        firestoreManager.fetchTasksForPet(selectedPet.getId(), tasks -> {
            delegationList.clear();
            for (RoutineTask task : tasks) {
                // Since our task model doesn't currently store a specific date natively for DAILY tasks,
                // we assume "DAILY" means it happens every day. If it's specific date, we check it.
                boolean matchesDate = isTaskScheduledForDate(task, selectedDate);
                
                if (matchesDate) {
                    delegationList.add(new DelegationTaskItem(task, true));
                }
            }

            if (delegationList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerViewTasks.setVisibility(View.GONE);
                tvDelegationTasksCount.setText("0 Tasks");
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerViewTasks.setVisibility(View.VISIBLE);
                tvDelegationTasksCount.setText(delegationList.size() + " Tasks");
            }

            isAllSelected = true;
            btnSelectAll.setText("Deselect All");
            delegationAdapter.notifyDataSetChanged();
            updateSendButtonState();
            
        }, null);
    }

    private void updateSendButtonState() {
        int count = 0;
        for (DelegationTaskItem item : delegationList) {
            if (item.isSelected) count++;
        }
        if (count == 0) {
            btnSendDelegation.setEnabled(false);
            btnSendDelegation.setAlpha(0.5f);
        } else {
            btnSendDelegation.setEnabled(true);
            btnSendDelegation.setAlpha(1.0f);
        }
    }

    private boolean isTaskScheduledForDate(RoutineTask task, String dateStr) {
        String scheduleType = task.getScheduleType();
        if (scheduleType == null || "DAILY".equalsIgnoreCase(scheduleType)) {
            return true;
        }

        try {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                int dayOfMonth = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);

                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                String[] dayNames = new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
                String targetDayOfWeek = dayNames[dayOfWeek - 1];

                String formattedForOnce = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                if ("SPECIFIC_DAYS".equalsIgnoreCase(scheduleType) || "WEEKLY".equalsIgnoreCase(scheduleType)) {
                    List<String> days = task.getDaysOfWeek();
                    return days != null && days.contains(targetDayOfWeek);
                } else if ("MONTHLY".equalsIgnoreCase(scheduleType)) {
                    List<String> days = task.getDaysOfWeek();
                    if (days != null && !days.isEmpty()) {
                        int scheduledDay = Integer.parseInt(days.get(0));
                        return scheduledDay == dayOfMonth;
                    }
                    return true;
                } else if ("ONCE".equalsIgnoreCase(scheduleType) || "SPECIFIC_DATE".equalsIgnoreCase(scheduleType)) {
                    List<String> days = task.getDaysOfWeek();
                    if (days != null && !days.isEmpty()) {
                        return days.get(0).equals(formattedForOnce);
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return true;
    }

    private void checkAndSendSms() {
        String phone = etRecipientPhone.getText() != null ? etRecipientPhone.getText().toString().trim() : "";
        if (phone.isEmpty()) {
            Toast.makeText(getContext(), "Please enter or select a recipient phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = 0;
        for (DelegationTaskItem item : delegationList) {
            if (item.isSelected) count++;
        }

        if (count == 0) {
            Toast.makeText(getContext(), "Please select at least one task to delegate", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        } else {
            executeDirectBackgroundSms(phone);
        }
    }

    private void executeDirectBackgroundSms(String phone) {
        String petName = selectedPet != null ? selectedPet.getName() : "Pet";
        StringBuilder smsBody = new StringBuilder("🐾 Care Delegation for " + petName + " (" + selectedDate + "):\n\n");

        for (DelegationTaskItem item : delegationList) {
            if (item.isSelected) {
                smsBody.append("• ").append(item.task.getTitle());
                if (item.task.getTime() != null && !item.task.getTime().isEmpty()) {
                    smsBody.append(" (").append(item.task.getTime()).append(")");
                }
                smsBody.append("\n");
            }
        }
        smsBody.append("\nThanks for looking after them!");

        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(smsBody.toString());
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            Toast.makeText(getContext(), "Delegation sent successfully in background!", Toast.LENGTH_LONG).show();
            if (getActivity() != null) getActivity().onBackPressed();
        } catch (Exception e) {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + phone));
            smsIntent.putExtra("sms_body", smsBody.toString());
            try {
                startActivity(smsIntent);
                Toast.makeText(getContext(), "Prepared via SMS app!", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(getContext(), "SMS failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("DELEGATE");
        }
    }

    private static class DelegationAdapter extends RecyclerView.Adapter<DelegationAdapter.ViewHolder> {
        private final List<DelegationTaskItem> items;
        private final Runnable onSelectionChanged;

        public DelegationAdapter(List<DelegationTaskItem> items, Runnable onSelectionChanged) {
            this.items = items;
            this.onSelectionChanged = onSelectionChanged;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delegation_task, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DelegationTaskItem item = items.get(position);
            holder.tvTitle.setText(item.task.getTitle());

            String details = "Category: " + (item.task.getCategory() != null ? item.task.getCategory() : "General") +
                    (item.task.getTime() != null && !item.task.getTime().isEmpty() ? " | Time: " + item.task.getTime() : "");

            holder.tvDetails.setText(details);

            updateSelectionUI(holder, item.isSelected);

            holder.itemView.setOnClickListener(v -> {
                item.isSelected = !item.isSelected;
                updateSelectionUI(holder, item.isSelected);
                if (onSelectionChanged != null) {
                    onSelectionChanged.run();
                }
            });
        }

        private void updateSelectionUI(ViewHolder holder, boolean isSelected) {
            holder.cbCheck.setChecked(isSelected);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.CheckBox cbCheck;
            TextView tvTitle, tvDetails;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cbCheck = itemView.findViewById(R.id.taskCheckbox);
                tvTitle = itemView.findViewById(R.id.tv_task_title);
                tvDetails = itemView.findViewById(R.id.tv_task_details);
            }
        }
    }
}
