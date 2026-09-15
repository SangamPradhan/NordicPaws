package com.sangampradhan.nordicpaws.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.adapters.CategoryAdapter;
import com.sangampradhan.nordicpaws.adapters.RoutineTaskAdapter;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.models.RoutineTask;
import com.sangampradhan.nordicpaws.utils.DummyData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoutineFragment extends Fragment {

    private ImageView activePetAvatar;
    private FrameLayout petSwitcherContainer;
    private ProgressBar progressCircle;
    private TextView progressPercentText;
    private TextView progressTitleText;
    private TextView progressSubtitleText;
    private RecyclerView rvCategories;
    private RecyclerView rvTasks;
    private FloatingActionButton fabAddTask;

    private RoutineTaskAdapter taskAdapter;
    private CategoryAdapter categoryAdapter;

    private String currentPetId = "1";
    private String currentCategory = "All";
    private List<RoutineTask> displayedTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routine, container, false);
        
        activePetAvatar = view.findViewById(R.id.activePetAvatar);
        petSwitcherContainer = view.findViewById(R.id.petSwitcherContainer);
        progressCircle = view.findViewById(R.id.progressCircle);
        progressPercentText = view.findViewById(R.id.progressPercentText);
        progressTitleText = view.findViewById(R.id.progressTitleText);
        progressSubtitleText = view.findViewById(R.id.progressSubtitleText);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvTasks = view.findViewById(R.id.rvTasks);
        fabAddTask = view.findViewById(R.id.fabAddTask);

        // Pet Switcher Logic
        petSwitcherContainer.setOnClickListener(v -> showPetSelectionPopup());

        // Category setup
        List<String> categories = Arrays.asList("All", "Feeding", "Exercise", "Grooming", "Medication", "Healthcare");
        categoryAdapter = new CategoryAdapter(categories, cat -> {
            currentCategory = cat;
            loadTasks();
        });
        rvCategories.setAdapter(categoryAdapter);

        // Tasks Setup with Task Action Listeners
        taskAdapter = new RoutineTaskAdapter(displayedTasks, new RoutineTaskAdapter.OnTaskActionListener() {
            @Override
            public void onStatusChanged() {
                updateProgress();
            }

            @Override
            public void onEditTask(RoutineTask task) {
                EditTaskBottomSheetFragment editSheet = EditTaskBottomSheetFragment.newInstance(task);
                editSheet.show(getParentFragmentManager(), "EditTaskBottomSheet");
            }

            @Override
            public void onRemoveTodayTask(RoutineTask task) {
                displayedTasks.remove(task);
                taskAdapter.notifyDataSetChanged();
                updateProgress();
                Toast.makeText(getContext(), "Task removed for today", Toast.LENGTH_SHORT).show();
            }
        });
        rvTasks.setAdapter(taskAdapter);

        // FAB Setup opens AddActivityBottomSheetFragment
        fabAddTask.setOnClickListener(v -> {
            AddActivityBottomSheetFragment addSheet = new AddActivityBottomSheetFragment();
            addSheet.show(getParentFragmentManager(), "AddActivityBottomSheet");
        });

        setupSwipeGestures();

        loadPetData();

        return view;
    }

    private void setupSwipeGestures() {
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback simpleCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                RoutineTask task = taskAdapter.getTask(position);
                if (task != null) {
                    if (direction == androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                        // Edit
                        taskAdapter.notifyItemChanged(position); // reset view
                        EditTaskBottomSheetFragment editSheet = EditTaskBottomSheetFragment.newInstance(task);
                        editSheet.show(getParentFragmentManager(), "EditTaskBottomSheet");
                    } else if (direction == androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                        // Delete for today
                        taskAdapter.notifyItemChanged(position); // reset view
                        showDeleteConfirmation(task, position);
                    }
                }
            }
        };

        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvTasks);
    }
    
    private void showDeleteConfirmation(RoutineTask task, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Remove Task")
                .setMessage("Are you sure you want to remove this task for today?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    displayedTasks.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    updateProgress();
                    Toast.makeText(getContext(), "Task removed for today", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPetSelectionPopup() {
        PopupMenu popup = new PopupMenu(getContext(), petSwitcherContainer);
        List<Pet> pets = DummyData.getPets();
        for (int i = 0; i < pets.size(); i++) {
            popup.getMenu().add(0, i, 0, pets.get(i).getName());
        }
        popup.setOnMenuItemClickListener(item -> {
            Pet selectedPet = pets.get(item.getItemId());
            currentPetId = selectedPet.getId();
            loadPetData();
            return true;
        });
        popup.show();
    }

    private void loadPetData() {
        Pet currentPet = null;
        for (Pet p : DummyData.getPets()) {
            if (p.getId().equals(currentPetId)) {
                currentPet = p;
                break;
            }
        }
        
        if (currentPet != null) {
            activePetAvatar.setImageResource(currentPet.getAvatarResId());
            progressTitleText.setText(currentPet.getName() + "'s Day");
        }

        loadTasks();
    }

    private void loadTasks() {
        displayedTasks = new ArrayList<>(DummyData.getTasksForPet(currentPetId, currentCategory));
        taskAdapter.setTasks(displayedTasks);
        updateProgress();
    }

    private void updateProgress() {
        int total = displayedTasks.size();
        int completed = 0;
        for (RoutineTask t : displayedTasks) {
            if (t.isCompleted()) completed++;
        }

        progressSubtitleText.setText(completed + " of " + total + " completed");
        
        int percent = total == 0 ? 0 : (int) (((float) completed / total) * 100);
        progressCircle.setProgress(percent);
        progressPercentText.setText(percent + "%");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("ROUTINE");
        }
    }
}
