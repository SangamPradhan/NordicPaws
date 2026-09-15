package com.sangampradhan.nordicpaws.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.RoutineTask;

import java.util.List;

public class RoutineTaskAdapter extends RecyclerView.Adapter<RoutineTaskAdapter.TaskViewHolder> {

    private List<RoutineTask> tasks;
    private OnTaskActionListener actionListener;

    public interface OnTaskActionListener {
        void onStatusChanged();
        void onEditTask(RoutineTask task);
        void onRemoveTodayTask(RoutineTask task);
    }

    public RoutineTaskAdapter(List<RoutineTask> tasks, OnTaskActionListener actionListener) {
        this.tasks = tasks;
        this.actionListener = actionListener;
    }

    public void setTasks(List<RoutineTask> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        RoutineTask task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
    
    public RoutineTask getTask(int position) {
        if (position >= 0 && position < tasks.size()) {
            return tasks.get(position);
        }
        return null;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemMainLayout;
        FrameLayout iconContainer;
        ImageView taskIcon;
        TextView taskTitle;
        TextView taskTime;
        TextView taskRecurrenceTag;
        CheckBox taskCheckbox;
        ImageButton btnMore;
        
        // Expandable details elements
        LinearLayout expandableDetailsLayout;
        TextView taskNotes;
        TextView taskCategoryTag;
        TextView taskStatusText;

        private boolean isExpanded = false;

        TaskViewHolder(View itemView) {
            super(itemView);
            itemMainLayout = itemView.findViewById(R.id.itemMainLayout);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            taskIcon = itemView.findViewById(R.id.taskIcon);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskRecurrenceTag = itemView.findViewById(R.id.taskRecurrenceTag);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            btnMore = itemView.findViewById(R.id.btnMore);
            
            expandableDetailsLayout = itemView.findViewById(R.id.expandableDetailsLayout);
            taskNotes = itemView.findViewById(R.id.taskNotes);
            taskCategoryTag = itemView.findViewById(R.id.taskCategoryTag);
            taskStatusText = itemView.findViewById(R.id.taskStatusText);

            // Item click expands details (reveal on click)
            itemMainLayout.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                expandableDetailsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            });

            // Checkbox touch event specifically toggles task completion
            taskCheckbox.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    RoutineTask t = tasks.get(pos);
                    t.setCompleted(taskCheckbox.isChecked());
                    updateUI(t);
                    if (actionListener != null) actionListener.onStatusChanged();
                }
            });

            // 3-dots baseline_more_vert_24 button popup menu
            btnMore.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                RoutineTask currentTask = tasks.get(pos);

                PopupMenu popup = new PopupMenu(itemView.getContext(), btnMore);
                popup.inflate(R.menu.task_item_menu);
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit_task) {
                        if (actionListener != null) {
                            actionListener.onEditTask(currentTask);
                        }
                        return true;
                    } else if (itemId == R.id.action_remove_today) {
                        if (actionListener != null) {
                            actionListener.onRemoveTodayTask(currentTask);
                        }
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        void bind(RoutineTask task) {
            taskTitle.setText(task.getTitle());
            taskTime.setText(task.getTime());
            taskCategoryTag.setText(task.getCategory());
            
            // Default recurrence display or notes
            taskRecurrenceTag.setText("DAILY");
            taskNotes.setText("1.5 scoops kibble mixed with probiotic powder. Keep fresh spring water refilled.");
            
            // Category icon & background color
            String category = task.getCategory();
            int bgColor = Color.parseColor("#f8f9ff");
            int iconRes = R.drawable.baseline_restaurant_24;
            
            if ("Feeding".equals(category)) {
                bgColor = Color.parseColor("#f6df84");
                iconRes = R.drawable.baseline_restaurant_24;
            } else if ("Exercise".equals(category)) {
                bgColor = Color.parseColor("#d5e3fc");
                iconRes = R.drawable.paw;
            } else if ("Grooming".equals(category)) {
                bgColor = Color.parseColor("#dce9ff");
                iconRes = R.drawable.baseline_health_and_safety_24;
            } else if ("Medication".equals(category)) {
                bgColor = Color.parseColor("#ffddb7");
                iconRes = R.drawable.baseline_favorite_border_24;
            } else if ("Healthcare".equals(category)) {
                bgColor = Color.parseColor("#ffdad6");
                iconRes = R.drawable.baseline_health_and_safety_24;
            }

            iconContainer.getBackground().setColorFilter(bgColor, PorterDuff.Mode.SRC_IN);
            taskIcon.setImageResource(iconRes);

            taskCheckbox.setChecked(task.isCompleted());
            updateUI(task);
            
            // Keep collapsed by default
            isExpanded = false;
            expandableDetailsLayout.setVisibility(View.GONE);
        }

        private void updateUI(RoutineTask task) {
            if (task.isCompleted()) {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskTitle.setAlpha(0.6f);
                taskStatusText.setText("COMPLETED");
                taskStatusText.setTextColor(Color.parseColor("#dcc66e"));
                taskTime.setTextColor(Color.parseColor("#dcc66e"));
            } else {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                taskTitle.setAlpha(1.0f);
                taskStatusText.setText("PENDING");
                taskStatusText.setTextColor(Color.parseColor("#76777b"));
                taskTime.setTextColor(Color.parseColor("#76777b"));
            }
        }
    }
}
