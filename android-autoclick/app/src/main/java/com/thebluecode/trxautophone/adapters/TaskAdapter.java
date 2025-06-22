package com.thebluecode.trxautophone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.service.AutoClickAccessibilityService;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Enhanced TaskAdapter with improved UI/UX and interaction handling
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    private final OnTaskClickListener listener;
    private boolean isEnabled = true;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskRunClick(Task task);
        void onTaskStopClick(Task task);
        void onTaskDeleteClick(Task task);
        void onTaskExportClick(Task task);
    }

    public TaskAdapter(OnTaskClickListener listener) {
        super(new TaskDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);
    }

    /**
     * Enable/disable interaction
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        notifyDataSetChanged();
    }

    /**
     * Release resources
     */
    public void release() {
        // Clean up any resources if needed
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView nameText;
        private final TextView descriptionText;
        private final TextView summaryText;
        private final TextView lastExecutedText;
        private final LinearProgressIndicator progressIndicator;
        private final ImageButton runButton;
        private final ImageButton stopButton;
        private final ImageButton deleteButton;
        private final ImageButton exportButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            nameText = itemView.findViewById(R.id.taskName);
            descriptionText = itemView.findViewById(R.id.taskDescription);
            summaryText = itemView.findViewById(R.id.taskSummary);
            lastExecutedText = itemView.findViewById(R.id.lastExecuted);
            progressIndicator = itemView.findViewById(R.id.progressIndicator);
            runButton = itemView.findViewById(R.id.runButton);
            stopButton = itemView.findViewById(R.id.stopButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            exportButton = itemView.findViewById(R.id.exportButton);
        }

        void bind(Task task) {
            // Set basic task info
            nameText.setText(task.getName());
            descriptionText.setText(task.getDescription());
            descriptionText.setVisibility(
                task.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
            summaryText.setText(task.getSummary());

            // Set last executed time
            if (task.getLastExecuted() > 0) {
                lastExecutedText.setText(String.format("Last run: %s",
                    DATE_FORMAT.format(task.getLastExecuted())));
                lastExecutedText.setVisibility(View.VISIBLE);
            } else {
                lastExecutedText.setVisibility(View.GONE);
            }

            // Update enabled state
            cardView.setEnabled(isEnabled && task.isEnabled());
            float alpha = (isEnabled && task.isEnabled()) ? 1.0f : 0.5f;
            cardView.setAlpha(alpha);

            // Check if this task is currently running
            boolean isRunning = false;
            AutoClickAccessibilityService service = AutoClickAccessibilityService.getInstance();
            if (service != null && service.isTaskRunning()) {
                Task currentTask = service.getCurrentTask();
                if (currentTask != null && currentTask.getId() == task.getId()) {
                    isRunning = true;
                    progressIndicator.setProgress(service.getExecutionProgress());
                }
            }

            // Update progress indicator
            progressIndicator.setVisibility(isRunning ? View.VISIBLE : View.GONE);

            // Update button visibility
            runButton.setVisibility(isRunning ? View.GONE : View.VISIBLE);
            stopButton.setVisibility(isRunning ? View.VISIBLE : View.GONE);
            deleteButton.setEnabled(!isRunning);
            exportButton.setEnabled(!isRunning);

            // Setup click listeners
            cardView.setOnClickListener(v -> {
                if (isEnabled && task.isEnabled()) {
                    listener.onTaskClick(task);
                }
            });

            runButton.setOnClickListener(v -> {
                if (isEnabled && task.isEnabled()) {
                    listener.onTaskRunClick(task);
                }
            });

            stopButton.setOnClickListener(v -> {
                if (isEnabled) {
                    listener.onTaskStopClick(task);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (isEnabled && !isRunning) {
                    listener.onTaskDeleteClick(task);
                }
            });

            exportButton.setOnClickListener(v -> {
                if (isEnabled && !isRunning) {
                    listener.onTaskExportClick(task);
                }
            });

            // Long press for additional options
            cardView.setOnLongClickListener(v -> {
                if (isEnabled && task.isEnabled()) {
                    showTaskOptionsMenu(task);
                    return true;
                }
                return false;
            });
        }

        /**
         * Show task options menu
         */
        private void showTaskOptionsMenu(Task task) {
            PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
            popup.inflate(R.menu.menu_task_options);

            // Update menu items based on task state
            Menu menu = popup.getMenu();
            boolean isRunning = false;
            AutoClickAccessibilityService service = AutoClickAccessibilityService.getInstance();
            if (service != null && service.isTaskRunning()) {
                Task currentTask = service.getCurrentTask();
                if (currentTask != null && currentTask.getId() == task.getId()) {
                    isRunning = true;
                }
            }

            menu.findItem(R.id.action_run).setVisible(!isRunning);
            menu.findItem(R.id.action_stop).setVisible(isRunning);
            menu.findItem(R.id.action_edit).setEnabled(!isRunning);
            menu.findItem(R.id.action_delete).setEnabled(!isRunning);
            menu.findItem(R.id.action_export).setEnabled(!isRunning);
            menu.findItem(R.id.action_enable).setVisible(!task.isEnabled());
            menu.findItem(R.id.action_disable).setVisible(task.isEnabled() && !isRunning);

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_run:
                        listener.onTaskRunClick(task);
                        return true;

                    case R.id.action_stop:
                        listener.onTaskStopClick(task);
                        return true;

                    case R.id.action_edit:
                        listener.onTaskClick(task);
                        return true;

                    case R.id.action_delete:
                        listener.onTaskDeleteClick(task);
                        return true;

                    case R.id.action_export:
                        listener.onTaskExportClick(task);
                        return true;

                    case R.id.action_enable:
                    case R.id.action_disable:
                        task.setEnabled(!task.isEnabled());
                        notifyItemChanged(getAdapterPosition());
                        return true;

                    default:
                        return false;
                }
            });

            popup.show();
        }
    }

    /**
     * DiffUtil callback for efficient updates
     */
    private static class TaskDiffCallback extends DiffUtil.ItemCallback<Task> {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.isEnabled() == newItem.isEnabled() &&
                   oldItem.getLastExecuted() == newItem.getLastExecuted() &&
                   oldItem.getExecutionCount() == newItem.getExecutionCount() &&
                   oldItem.getSuccessCount() == newItem.getSuccessCount();
        }
    }
}
