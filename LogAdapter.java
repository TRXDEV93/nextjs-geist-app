package com.thebluecode.trxautophone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.services.TaskExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private List<TaskExecutor.LogEntry> logs;
    private final SimpleDateFormat timeFormat;

    public LogAdapter(List<TaskExecutor.LogEntry> logs) {
        this.logs = logs;
        this.timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_entry, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        TaskExecutor.LogEntry entry = logs.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void updateLogs(List<TaskExecutor.LogEntry> newLogs) {
        this.logs = newLogs;
        notifyDataSetChanged();
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeView;
        private final TextView messageView;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            timeView = itemView.findViewById(R.id.tv_timestamp);
            messageView = itemView.findViewById(R.id.tv_details);
        }

        void bind(TaskExecutor.LogEntry entry) {
            timeView.setText(timeFormat.format(new Date(entry.getTimestamp())));
            messageView.setText(entry.getMessage());

            int textColor;
            switch (entry.getType()) {
                case ERROR:
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.error);
                    break;
                case SUCCESS:
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.success);
                    break;
                default:
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.teal_200);
                    break;
            }
            messageView.setTextColor(textColor);
        }
    }
}