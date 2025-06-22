package com.thebluecode.trxautophone.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Task;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    public interface TaskActionListener {
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
        void onTaskSelected(Task task);
    }

    private final TaskActionListener listener;

    public TaskAdapter(TaskActionListener listener) {
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

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName;
        private final TextView taskDescription;
        private final TextView stepCount;
        private final TextView lastRun;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;
        private final LinearProgressIndicator taskProgress;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            stepCount = itemView.findViewById(R.id.stepCount);
            lastRun = itemView.findViewById(R.id.lastRun);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            taskProgress = itemView.findViewById(R.id.taskProgress);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskSelected(getItem(position));
                }
            });
        }

        void bind(Task task) {
            Context context = itemView.getContext();
            
            taskName.setText(task.getName());
            taskDescription.setText(task.getDescription());
            
            int steps = task.getSteps() != null ? task.getSteps().size() : 0;
            stepCount.setText(context.getString(R.string.step_count, steps));

            String timeAgo = DateUtils.getRelativeTimeSpanString(
                task.getUpdatedAt(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString();
            lastRun.setText(context.getString(R.string.last_run, timeAgo));

            btnEdit.setOnClickListener(v -> listener.onTaskEdit(task));
            btnDelete.setOnClickListener(v -> listener.onTaskDelete(task));

            // Show progress if task is running
            taskProgress.setVisibility(View.GONE); // By default hidden
        }

        void updateProgress(int progress) {
            if (progress > 0 && progress <= 100) {
                taskProgress.setVisibility(View.VISIBLE);
                taskProgress.setProgress(progress);
            } else {
                taskProgress.setVisibility(View.GONE);
            }
        }
    }

    private static class TaskDiffCallback extends DiffUtil.ItemCallback<Task> {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.getUpdatedAt() == newItem.getUpdatedAt() &&
                   oldItem.getSteps().size() == newItem.getSteps().size();
        }
    }

    // Method to update progress for a specific task
    public void updateTaskProgress(long taskId, int progress) {
        for (int i = 0; i < getItemCount(); i++) {
            Task task = getItem(i);
            if (task.getId() == taskId) {
                RecyclerView.ViewHolder holder = 
                    getCurrentList().get(i).equals(task) ? null : getViewHolderAt(i);
                if (holder instanceof TaskViewHolder) {
                    ((TaskViewHolder) holder).updateProgress(progress);
                }
                break;
            }
        }
    }

    private RecyclerView.ViewHolder getViewHolderAt(int position) {
        RecyclerView recyclerView = null;
        // Try to find the RecyclerView from any existing ViewHolder
        for (int i = 0; i < getItemCount(); i++) {
            RecyclerView.ViewHolder holder = getCurrentList().get(i).equals(getItem(i)) ? 
                null : findViewHolderForAdapterPosition(i);
            if (holder != null) {
                View itemView = holder.itemView;
                if (itemView.getParent() instanceof RecyclerView) {
                    recyclerView = (RecyclerView) itemView.getParent();
                    break;
                }
            }
        }
        return recyclerView != null ? recyclerView.findViewHolderForAdapterPosition(position) : null;
    }

    private RecyclerView.ViewHolder findViewHolderForAdapterPosition(int i) {
        return null;
    }
}
