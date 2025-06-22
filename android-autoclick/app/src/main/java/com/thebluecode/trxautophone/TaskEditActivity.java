package com.thebluecode.trxautophone;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.thebluecode.trxautophone.adapters.StepAdapter;
import com.thebluecode.trxautophone.dialogs.StepConfigDialog;
import com.thebluecode.trxautophone.dialogs.StepTypeDialog;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.Constants;
import com.thebluecode.trxautophone.viewmodels.TaskEditViewModel;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Enhanced TaskEditActivity with improved UI/UX and validation
 */
public class TaskEditActivity extends AppCompatActivity implements 
        StepAdapter.OnStepClickListener,
        StepTypeDialog.OnStepTypeSelectedListener,
        StepConfigDialog.OnStepConfiguredListener {

    private static final String TAG = "TaskEditActivity";

    private TaskEditViewModel viewModel;
    private StepAdapter stepAdapter;
    private TextInputLayout nameInput;
    private TextInputLayout descriptionInput;
    private TextInputLayout repeatCountInput;
    private TextInputLayout repeatDelayInput;
    private View emptyView;
    private long taskId;
    private boolean isNewTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        taskId = getIntent().getLongExtra("task_id", -1);
        isNewTask = taskId == -1;

        setupViews();
        setupViewModel();
        setupStepsList();
    }

    /**
     * Initialize views and setup listeners
     */
    private void setupViews() {
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(isNewTask ? R.string.new_task : R.string.edit_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup input fields
        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        repeatCountInput = findViewById(R.id.repeatCountInput);
        repeatDelayInput = findViewById(R.id.repeatDelayInput);

        // Setup FAB
        FloatingActionButton fabAddStep = findViewById(R.id.fabAddStep);
        fabAddStep.setOnClickListener(v -> showStepTypeDialog());

        // Setup empty view
        emptyView = findViewById(R.id.emptyView);
    }

    /**
     * Setup ViewModel and observe changes
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TaskEditViewModel.class);

        if (!isNewTask) {
            viewModel.loadTask(taskId);
        }

        // Observe task
        viewModel.getTask().observe(this, this::updateUI);

        // Observe steps
        viewModel.getSteps().observe(this, steps -> {
            stepAdapter.submitList(new ArrayList<>(steps));
            updateEmptyView(steps.isEmpty());
        });

        // Observe validation errors
        viewModel.getValidationError().observe(this, error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    /**
     * Setup RecyclerView for steps
     */
    private void setupStepsList() {
        RecyclerView recyclerView = findViewById(R.id.stepsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        stepAdapter = new StepAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(stepAdapter);

        // Setup drag & drop
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder source,
                                @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = source.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                viewModel.moveStep(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteStepDialog(position);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    /**
     * Update UI with task data
     */
    private void updateUI(Task task) {
        if (task != null) {
            nameInput.getEditText().setText(task.getName());
            descriptionInput.getEditText().setText(task.getDescription());
            repeatCountInput.getEditText().setText(String.valueOf(task.getRepeatCount()));
            repeatDelayInput.getEditText().setText(String.valueOf(task.getRepeatDelay()));
        }
    }

    /**
     * Show step type selection dialog
     */
    private void showStepTypeDialog() {
        StepTypeDialog dialog = new StepTypeDialog();
        dialog.show(getSupportFragmentManager(), "step_type");
    }

    /**
     * Show step configuration dialog
     */
    private void showStepConfigDialog(Step.StepType type, Step existingStep) {
        StepConfigDialog dialog = StepConfigDialog.newInstance(type, existingStep);
        dialog.show(getSupportFragmentManager(), "step_config");
    }

    /**
     * Show delete step confirmation dialog
     */
    private void showDeleteStepDialog(int position) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_step)
            .setMessage(R.string.delete_step_message)
            .setPositiveButton(R.string.delete, (dialog, which) -> 
                viewModel.removeStep(position))
            .setNegativeButton(R.string.cancel, (dialog, which) -> 
                stepAdapter.notifyItemChanged(position))
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
    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Save task
     */
    private void saveTask() {
        String name = nameInput.getEditText().getText().toString().trim();
        String description = descriptionInput.getEditText().getText().toString().trim();
        String repeatCountStr = repeatCountInput.getEditText().getText().toString();
        String repeatDelayStr = repeatDelayInput.getEditText().getText().toString();

        // Validate name
        if (TextUtils.isEmpty(name)) {
            nameInput.setError(getString(R.string.error_name_required));
            return;
        }

        // Validate repeat count
        int repeatCount;
        try {
            repeatCount = Integer.parseInt(repeatCountStr);
            if (repeatCount < 1 || repeatCount > Constants.Limits.MAX_REPEAT_COUNT) {
                repeatCountInput.setError(getString(R.string.error_invalid_repeat_count));
                return;
            }
        } catch (NumberFormatException e) {
            repeatCountInput.setError(getString(R.string.error_invalid_number));
            return;
        }

        // Validate repeat delay
        long repeatDelay;
        try {
            repeatDelay = Long.parseLong(repeatDelayStr);
            if (repeatDelay < 0 || repeatDelay > Constants.Limits.MAX_REPEAT_DELAY) {
                repeatDelayInput.setError(getString(R.string.error_invalid_repeat_delay));
                return;
            }
        } catch (NumberFormatException e) {
            repeatDelayInput.setError(getString(R.string.error_invalid_number));
            return;
        }

        // Clear errors
        nameInput.setError(null);
        repeatCountInput.setError(null);
        repeatDelayInput.setError(null);

        // Save task
        viewModel.saveTask(name, description, repeatCount, repeatDelay);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_save:
                saveTask();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStepClick(Step step, int position) {
        showStepConfigDialog(step.getType(), step);
    }

    @Override
    public void onStepTypeSelected(Step.StepType type) {
        showStepConfigDialog(type, null);
    }

    @Override
    public void onStepConfigured(Step step) {
        if (step.getId() > 0) {
            viewModel.updateStep(step);
        } else {
            viewModel.addStep(step);
        }
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.discard_changes)
            .setMessage(R.string.discard_changes_message)
            .setPositiveButton(R.string.discard, (dialog, which) -> super.onBackPressed())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}
