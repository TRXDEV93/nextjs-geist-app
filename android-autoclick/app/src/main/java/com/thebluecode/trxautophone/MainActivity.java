package com.thebluecode.trxautophone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.thebluecode.trxautophone.adapters.TaskAdapter;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.service.AutoClickAccessibilityService;
import com.thebluecode.trxautophone.utils.Constants;
import com.thebluecode.trxautophone.utils.PermissionUtils;
import com.thebluecode.trxautophone.viewmodels.MainViewModel;

import java.util.ArrayList;

/**
 * Enhanced MainActivity with improved UI/UX and error handling
 */
public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ACCESSIBILITY_SETTINGS = 1001;
    private static final int REQUEST_OVERLAY_SETTINGS = 1002;
    private static final int REQUEST_IMPORT_FILE = 1003;
    private static final int REQUEST_EXPORT_FILE = 1004;

    private MainViewModel viewModel;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAdd;
    private View emptyView;
    private boolean isServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupViews();
        setupViewModel();
        checkPermissions();
    }

    /**
     * Initialize views and setup listeners
     */
    private void setupViews() {
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.app_name);

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(taskAdapter);

        // Setup FAB
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> startTaskEdit(null));

        // Setup empty view
        emptyView = findViewById(R.id.emptyView);

        // Setup swipe refresh
        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshTasks();
            swipeRefresh.setRefreshing(false);
        });
    }

    /**
     * Setup ViewModel and observe changes
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observe tasks
        viewModel.getTasks().observe(this, tasks -> {
            taskAdapter.submitList(tasks);
            updateEmptyView(tasks.isEmpty());
        });

        // Observe selected task
        viewModel.getSelectedTask().observe(this, task -> {
            if (task != null) {
                updateTaskControls(task);
            }
        });

        // Observe execution status
        viewModel.getExecutionStatus().observe(this, status -> {
            updateExecutionStatus(status);
        });

        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }

    /**
     * Check required permissions
     */
    private void checkPermissions() {
        if (!PermissionUtils.isAccessibilityServiceEnabled(this)) {
            showAccessibilityDialog();
        } else if (!PermissionUtils.canDrawOverlays(this)) {
            showOverlayDialog();
        } else {
            isServiceRunning = true;
            viewModel.refreshTasks();
        }
    }

    /**
     * Show accessibility permission dialog
     */
    private void showAccessibilityDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.accessibility_required)
            .setMessage(R.string.accessibility_message)
            .setCancelable(false)
            .setPositiveButton(R.string.settings, (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, REQUEST_ACCESSIBILITY_SETTINGS);
            })
            .setNegativeButton(R.string.exit, (dialog, which) -> finish())
            .show();
    }

    /**
     * Show overlay permission dialog
     */
    private void showOverlayDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.overlay_required)
            .setMessage(R.string.overlay_message)
            .setCancelable(false)
            .setPositiveButton(R.string.settings, (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_SETTINGS);
            })
            .setNegativeButton(R.string.exit, (dialog, which) -> finish())
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch (requestCode) {
            case REQUEST_ACCESSIBILITY_SETTINGS:
                if (PermissionUtils.isAccessibilityServiceEnabled(this)) {
                    if (!PermissionUtils.canDrawOverlays(this)) {
                        showOverlayDialog();
                    } else {
                        isServiceRunning = true;
                        viewModel.refreshTasks();
                    }
                } else {
                    showAccessibilityDialog();
                }
                break;

            case REQUEST_OVERLAY_SETTINGS:
                if (PermissionUtils.canDrawOverlays(this)) {
                    isServiceRunning = true;
                    viewModel.refreshTasks();
                } else {
                    showOverlayDialog();
                }
                break;

            case REQUEST_IMPORT_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    viewModel.importTask(data.getData());
                }
                break;

            case REQUEST_EXPORT_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    Task selectedTask = viewModel.getSelectedTask().getValue();
                    if (selectedTask != null) {
                        viewModel.exportTask(selectedTask, data.getData());
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import:
                importTask();
                return true;

            case R.id.action_settings:
                openSettings();
                return true;

            case R.id.action_logs:
                openLogs();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Import task from file
     */
    private void importTask() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_IMPORT_FILE);
    }

    /**
     * Export task to file
     */
    private void exportTask(Task task) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, task.getName() + ".json");
        startActivityForResult(intent, REQUEST_EXPORT_FILE);
    }

    /**
     * Open settings activity
     */
    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Open logs activity
     */
    private void openLogs() {
        startActivity(new Intent(this, ExecutionLogActivity.class));
    }

    /**
     * Start task edit activity
     */
    private void startTaskEdit(@Nullable Task task) {
        Intent intent = new Intent(this, TaskEditActivity.class);
        if (task != null) {
            intent.putExtra("task_id", task.getId());
        }
        startActivity(intent);
    }

    /**
     * Update empty view visibility
     */
    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    /**
     * Update UI based on task execution status
     */
    private void updateExecutionStatus(MainViewModel.ExecutionStatus status) {
        switch (status) {
            case RUNNING:
                fabAdd.hide();
                taskAdapter.setEnabled(false);
                break;

            case PAUSED:
                fabAdd.hide();
                taskAdapter.setEnabled(false);
                break;

            case IDLE:
                fabAdd.show();
                taskAdapter.setEnabled(true);
                break;
        }
    }

    /**
     * Update task controls based on selected task
     */
    private void updateTaskControls(Task task) {
        // Update menu items, FAB visibility, etc.
        invalidateOptionsMenu();
    }

    /**
     * Show error message
     */
    private void showError(String error) {
        Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG)
            .setAction("Dismiss", v -> {})
            .show();
    }

    // TaskAdapter.OnTaskClickListener implementation
    @Override
    public void onTaskClick(Task task) {
        startTaskEdit(task);
    }

    @Override
    public void onTaskRunClick(Task task) {
        if (!isServiceRunning) {
            showError(getString(R.string.service_not_running));
            return;
        }

        AutoClickAccessibilityService service = AutoClickAccessibilityService.getInstance();
        if (service != null) {
            service.startTask(task);
            viewModel.setSelectedTask(task);
        } else {
            showError(getString(R.string.service_not_available));
        }
    }

    @Override
    public void onTaskStopClick(Task task) {
        AutoClickAccessibilityService service = AutoClickAccessibilityService.getInstance();
        if (service != null) {
            service.stopTask();
        }
    }

    @Override
    public void onTaskDeleteClick(Task task) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_task)
            .setMessage(getString(R.string.delete_task_message, task.getName()))
            .setPositiveButton(R.string.delete, (dialog, which) -> 
                viewModel.deleteTask(task))
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public void onTaskExportClick(Task task) {
        exportTask(task);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isServiceRunning) {
            viewModel.refreshTasks();
            
            // Update execution status
            AutoClickAccessibilityService service = AutoClickAccessibilityService.getInstance();
            if (service != null && service.isTaskRunning()) {
                Task currentTask = service.getCurrentTask();
                if (currentTask != null) {
                    viewModel.setSelectedTask(currentTask);
                    viewModel.setExecutionStatus(MainViewModel.ExecutionStatus.RUNNING);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskAdapter.release();
    }
}
