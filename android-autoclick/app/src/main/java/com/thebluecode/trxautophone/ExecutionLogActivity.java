package com.thebluecode.trxautophone;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.thebluecode.trxautophone.adapters.LogAdapter;
import com.thebluecode.trxautophone.models.LogEntry;
import com.thebluecode.trxautophone.utils.Constants;
import com.thebluecode.trxautophone.viewmodels.LogViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Enhanced activity for displaying execution logs with filtering and export options
 */
public class ExecutionLogActivity extends AppCompatActivity implements LogAdapter.OnLogClickListener {
    private static final String TAG = "ExecutionLogActivity";

    private LogViewModel viewModel;
    private LogAdapter logAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;
    private TextView filterInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_execution_log);

        setupViews();
        setupViewModel();
    }

    /**
     * Initialize views and setup listeners
     */
    private void setupViews() {
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.execution_log);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter(this);
        recyclerView.setAdapter(logAdapter);

        // Setup SwipeRefreshLayout
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshLogs();
            swipeRefresh.setRefreshing(false);
        });

        // Setup empty view and filter info
        emptyView = findViewById(R.id.emptyView);
        filterInfo = findViewById(R.id.filterInfo);

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if (lastVisible >= totalItemCount - 5) {
                    viewModel.loadMoreLogs();
                }
            }
        });
    }

    /**
     * Setup ViewModel and observe changes
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(LogViewModel.class);

        // Observe logs
        viewModel.getLogs().observe(this, logs -> {
            logAdapter.submitList(logs);
            updateEmptyView(logs.isEmpty());
        });

        // Observe filter info
        viewModel.getFilterInfo().observe(this, info -> {
            if (info != null && !info.isEmpty()) {
                filterInfo.setText(info);
                filterInfo.setVisibility(View.VISIBLE);
            } else {
                filterInfo.setVisibility(View.GONE);
            }
        });

        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_execution_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_filter:
                showFilterDialog();
                return true;

            case R.id.action_export:
                exportLogs();
                return true;

            case R.id.action_clear:
                showClearDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show filter options dialog
     */
    private void showFilterDialog() {
        String[] options = {
            getString(R.string.filter_all),
            getString(R.string.filter_success),
            getString(R.string.filter_error),
            getString(R.string.filter_today),
            getString(R.string.filter_this_week),
            getString(R.string.filter_custom)
        };

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.filter_logs)
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // All
                        viewModel.clearFilter();
                        break;
                    case 1: // Success
                        viewModel.filterByStatus(true);
                        break;
                    case 2: // Error
                        viewModel.filterByStatus(false);
                        break;
                    case 3: // Today
                        viewModel.filterByToday();
                        break;
                    case 4: // This week
                        viewModel.filterByThisWeek();
                        break;
                    case 5: // Custom
                        showCustomFilterDialog();
                        break;
                }
            })
            .show();
    }

    /**
     * Show custom date filter dialog
     */
    private void showCustomFilterDialog() {
        // Implementation for custom date range picker
        // This would typically use MaterialDatePicker
    }

    /**
     * Export logs to file
     */
    private void exportLogs() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String fileName = "execution_log_" + dateFormat.format(new Date()) + Constants.Files.LOG_EXTENSION;

        viewModel.exportLogs(fileName).observe(this, success -> {
            if (success) {
                File exportFile = new File(getExternalFilesDir(null), fileName);
                showSuccess(getString(R.string.logs_exported, exportFile.getAbsolutePath()));
            } else {
                showError(getString(R.string.error_exporting_logs));
            }
        });
    }

    /**
     * Show clear logs confirmation dialog
     */
    private void showClearDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_logs)
            .setMessage(R.string.clear_logs_message)
            .setPositiveButton(R.string.clear, (dialog, which) -> {
                viewModel.clearLogs();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    /**
     * Update empty view visibility
     */
    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.dismiss, v -> {})
            .show();
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .show();
    }

    @Override
    public void onLogClick(LogEntry log) {
        // Show log details dialog
        new MaterialAlertDialogBuilder(this)
            .setTitle(log.getTaskName())
            .setMessage(log.getDetails())
            .setPositiveButton(R.string.ok, null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logAdapter.release();
    }
}
