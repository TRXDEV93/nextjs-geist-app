package com.thebluecode.trxautophone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.LogEntry;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Enhanced adapter for displaying execution logs with improved UI/UX
 */
public class LogAdapter extends ListAdapter<LogEntry, LogAdapter.LogViewHolder> {
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
    private final OnLogClickListener listener;

    public interface OnLogClickListener {
        void onLogClick(LogEntry log);
    }

    public LogAdapter(OnLogClickListener listener) {
        super(new LogDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry log = getItem(position);
        holder.bind(log);
    }

    /**
     * Release resources
     */
    public void release() {
        // Clean up any resources if needed
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView statusIcon;
        private final TextView taskName;
        private final TextView timestamp;
        private final TextView summary;
        private final TextView duration;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            statusIcon = itemView.findViewById(R.id.statusIcon);
            taskName = itemView.findViewById(R.id.taskName);
            timestamp = itemView.findViewById(R.id.timestamp);
            summary = itemView.findViewById(R.id.summary);
            duration = itemView.findViewById(R.id.duration);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onLogClick(getItem(position));
                }
            });
        }

        void bind(LogEntry log) {
            // Set status icon
            statusIcon.setImageResource(log.isSuccess() ? 
                R.drawable.ic_success : R.drawable.ic_error);
            statusIcon.setContentDescription(log.isSuccess() ? 
                "Success" : "Error");

            // Set task name
            taskName.setText(log.getTaskName());

            // Set timestamp
            timestamp.setText(DATE_FORMAT.format(log.getTimestamp()));

            // Set summary
            String summaryText = getSummaryText(log);
            if (summaryText != null && !summaryText.isEmpty()) {
                summary.setText(summaryText);
                summary.setVisibility(View.VISIBLE);
            } else {
                summary.setVisibility(View.GONE);
            }

            // Set duration
            if (log.getDuration() > 0) {
                duration.setText(formatDuration(log.getDuration()));
                duration.setVisibility(View.VISIBLE);
            } else {
                duration.setVisibility(View.GONE);
            }

            // Update card appearance based on status
            updateCardAppearance(log);
        }

        /**
         * Get formatted summary text
         */
        private String getSummaryText(LogEntry log) {
            if (log.isSuccess()) {
                return String.format("Completed %d steps successfully", log.getStepsCompleted());
            } else {
                String error = log.getError();
                if (error != null && !error.isEmpty()) {
                    return String.format("Failed: %s", error);
                } else {
                    return String.format("Failed at step %d", log.getStepsCompleted() + 1);
                }
            }
        }

        /**
         * Format duration for display
         */
        private String formatDuration(long durationMs) {
            if (durationMs < 1000) {
                return durationMs + "ms";
            }
            
            long seconds = durationMs / 1000;
            if (seconds < 60) {
                return seconds + "s";
            }
            
            long minutes = seconds / 60;
            seconds = seconds % 60;
            if (minutes < 60) {
                return String.format("%dm %ds", minutes, seconds);
            }
            
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }

        /**
         * Update card appearance based on log status
         */
        private void updateCardAppearance(LogEntry log) {
            int strokeColor;
            float elevation;

            if (log.isSuccess()) {
                strokeColor = cardView.getContext().getColor(R.color.success);
                elevation = 2f;
            } else {
                strokeColor = cardView.getContext().getColor(R.color.error);
                elevation = 4f;
            }

            cardView.setStrokeColor(strokeColor);
            cardView.setStrokeWidth(2);
            cardView.setCardElevation(elevation);
        }
    }

    /**
     * DiffUtil callback for efficient updates
     */
    private static class LogDiffCallback extends DiffUtil.ItemCallback<LogEntry> {
        @Override
        public boolean areItemsTheSame(@NonNull LogEntry oldItem, @NonNull LogEntry newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull LogEntry oldItem, @NonNull LogEntry newItem) {
            return oldItem.getTimestamp() == newItem.getTimestamp() &&
                   oldItem.isSuccess() == newItem.isSuccess() &&
                   oldItem.getStepsCompleted() == newItem.getStepsCompleted() &&
                   oldItem.getDuration() == newItem.getDuration() &&
                   oldItem.getTaskName().equals(newItem.getTaskName()) &&
                   ((oldItem.getError() == null && newItem.getError() == null) ||
                    (oldItem.getError() != null && oldItem.getError().equals(newItem.getError())));
        }
    }
}
