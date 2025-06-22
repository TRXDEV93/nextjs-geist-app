package com.thebluecode.trxautophone;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.google.android.material.button.MaterialButton;
import com.thebluecode.trxautophone.adapters.TaskAdapter;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.services.AutoClickAccessibilityService;
import com.thebluecode.trxautophone.services.TaskExecutor;

public class MainActivity extends AppCompatActivity implements TaskAdapter.TaskActionListener {

    private MainViewModel viewModel;
    private TaskAdapter adapter;
    private RecyclerView taskRecyclerView;
    private ExtendedFloatingActionButton fabAddTask;
    private MaterialButton btnRun;
    private MaterialButton btnStop;
    private MaterialButton btnImport;
    private MaterialButton btnExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Setup UI components
        setupViews();
        setupRecyclerView();
        setupListeners();
        setupObservers();

        // Check accessibility permission
        checkAccessibilityPermission();
    }

    private void setupViews() {
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        fabAddTask = findViewById(R.id.fabAddTask);
        btnRun = findViewById(R.id.btnRun);
        btnStop = findViewById(R.id.btnStop);
        btnImport = findViewById(R.id.btnImport);
        btnExport = findViewById(R.id.btnExport);

        // Initially disable execution controls
        btnStop.setEnabled(false);
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter(this);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskEditActivity.class);
            startActivity(intent);
        });

        btnRun.setOnClickListener(v -> {
            Task selectedTask = viewModel.getSelectedTask().getValue();
            if (selectedTask != null) {
                startTaskExecution(selectedTask);
            } else {
                showSnackbar("Please select a task to run");
            }
        });

        btnStop.setOnClickListener(v -> {
            TaskExecutor.getInstance().stopExecution();
            updateExecutionControls(false);
        });

        btnImport.setOnClickListener(v -> importTask());
        btnExport.setOnClickListener(v -> exportSelectedTask());
    }

    private void setupObservers() {
        viewModel.getTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            updateEmptyState(tasks.isEmpty());
        });

        viewModel.getSelectedTask().observe(this, task -> {
            btnRun.setEnabled(task != null);
            btnExport.setEnabled(task != null);
        });

        // Observe execution progress
        viewModel.getExecutionProgress().observe(this, progress -> {
            if (progress != null) {
                adapter.updateTaskProgress(progress.first, progress.second);
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        // Show/hide empty state view if you have one
        if (isEmpty) {
            showSnackbar("No tasks available. Create one by tapping the + button");
        }
    }

    private void startTaskExecution(Task task) {
        TaskExecutor.getInstance().executeTask(task, new TaskExecutor.ExecutionCallback() {
            @Override
            public void onStepStarted(Step step) {

            }

            @Override
            public void onStepExecuted(Step step, boolean success) {
                runOnUiThread(() -> {
                    int progress = (int) ((float) (TaskExecutor.getInstance().getCurrentStepIndex() + 1) 
                        / task.getSteps().size() * 100);
                    viewModel.updateExecutionProgress(task.getId(), progress);
                });
            }

            @Override
            public void onTaskCompleted(Task task, boolean success) {
                runOnUiThread(() -> {
                    updateExecutionControls(false);
                    showSnackbar(success ? "Task completed successfully" : "Task execution failed");
                });
            }

            @Override
            public void onTaskPaused(Task task, int currentStep) {
                runOnUiThread(() -> {
                    updateExecutionControls(false);
                    showSnackbar("Task paused");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showSnackbar("Error: " + error));
            }
        });

        updateExecutionControls(true);
    }

    private void updateExecutionControls(boolean isRunning) {
        btnRun.setEnabled(!isRunning);
        btnStop.setEnabled(isRunning);
        fabAddTask.setEnabled(!isRunning);
    }

    private void checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Accessibility Permission Required")
                .setMessage("This app needs accessibility permission to automate tasks")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String serviceName = getPackageName() + "/." + AutoClickAccessibilityService.class.getSimpleName();
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (settingValue != null) {
                return settingValue.contains(serviceName);
            }
        }
        return false;
    }

    // TaskAdapter.TaskActionListener implementations
    @Override
    public void onTaskEdit(Task task) {
        Intent intent = new Intent(this, TaskEditActivity.class);
        intent.putExtra("task_id", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskDelete(Task task) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteTask(task))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onTaskSelected(Task task) {
        viewModel.setSelectedTask(task);
    }

    private void importTask() {
        // Implement task import functionality
        // This could open a file picker and import JSON/XML task definitions
    }

    private void exportSelectedTask() {
        Task selectedTask = viewModel.getSelectedTask().getValue();
        if (selectedTask != null) {
            // Implement task export functionality
            // This could save the task definition as JSON/XML
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
