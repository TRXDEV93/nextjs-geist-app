package com.thebluecode.trxautophone;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.thebluecode.trxautophone.adapters.StepAdapter;
import com.thebluecode.trxautophone.dialog.StepConfigDialog;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.models.TaskEditViewModel;

import java.util.ArrayList;

public class TaskEditActivity extends AppCompatActivity implements StepAdapter.StepActionListener {

    private TaskEditViewModel viewModel;
    private StepAdapter stepAdapter;

    private TextInputEditText taskNameInput;
    private TextInputEditText taskDescriptionInput;
    private RecyclerView stepsRecyclerView;
    private MaterialButton btnAddStep;
    private SwitchMaterial switchEnabled;
    private ExtendedFloatingActionButton fabSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, new TaskEditViewModel.Factory(getApplication()))
                .get(TaskEditViewModel.class);

        // Setup UI
        setupViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupObservers();

        // Load task if editing
        long taskId = getIntent().getLongExtra("task_id", -1);
        if (taskId != -1) {
            viewModel.loadTask(taskId);
        }
    }

    private void setupViews() {
        taskNameInput = findViewById(R.id.taskNameInput);
        taskDescriptionInput = findViewById(R.id.taskDescriptionInput);
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
        btnAddStep = findViewById(R.id.btnAddStep);
        switchEnabled = findViewById(R.id.switchEnabled);
        fabSave = findViewById(R.id.fabSave);
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(viewModel.isNewTask() ? "Create Task" : "Edit Task");
    }

    private void setupRecyclerView() {
        stepAdapter = new StepAdapter(this);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setAdapter(stepAdapter);
    }

    private void setupListeners() {
        btnAddStep.setOnClickListener(v -> showStepTypeDialog());
        
        fabSave.setOnClickListener(v -> saveTask());

        // Handle back navigation in toolbar
        findViewById(R.id.toolbar).setNavigationOnClickListener(v -> 
            new MaterialAlertDialogBuilder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard your changes?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show()
        );
    }

    private void setupObservers() {
        viewModel.getTask().observe(this, this::updateUI);
        viewModel.getSteps().observe(this, steps -> stepAdapter.submitList(steps));
    }

    private void updateUI(Task task) {
        if (task != null) {
            taskNameInput.setText(task.getName());
            taskDescriptionInput.setText(task.getDescription());
            switchEnabled.setChecked(task.isEnabled());
        }
    }

    private void showStepTypeDialog() {
        String[] stepTypes = {
            "System Key (Home, Back)",
            "Tap",
            "Long Press",
            "Swipe",
            "Find Text",
            "Find Image",
            "Delay",
            "Condition"
        };

        new MaterialAlertDialogBuilder(this)
            .setTitle("Select Step Type")
            .setItems(stepTypes, (dialog, which) -> {
                Step.StepType type = Step.StepType.values()[which];
                showStepConfigDialog(type);
            })
            .show();
    }

    private void showStepConfigDialog(Step.StepType type) {
        // Create a new step configuration dialog based on the type
        StepConfigDialog dialog = StepConfigDialog.newInstance(type);
        dialog.setStepConfigListener(step -> viewModel.addStep(step));
        dialog.show(getSupportFragmentManager(), "step_config");
    }

    private void saveTask() {
        String name = taskNameInput.getText().toString().trim();
        String description = taskDescriptionInput.getText().toString().trim();
        
        if (name.isEmpty()) {
            taskNameInput.setError("Task name is required");
            return;
        }

        Task task = viewModel.getTask().getValue();
        if (task == null) {
            task = new Task();
        }

        task.setName(name);
        task.setDescription(description);
        task.setEnabled(switchEnabled.isChecked());
        
        viewModel.saveTask(task, success -> {
            if (success) {
                finish();
            } else {
                Snackbar.make(findViewById(android.R.id.content), 
                    "Error saving task", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // StepAdapter.StepActionListener implementations
    @Override
    public void onStepEdit(Step step, int position) {
        StepConfigDialog dialog = StepConfigDialog.newInstance(step);
        dialog.setStepConfigListener(updatedStep -> 
            viewModel.updateStep(position, updatedStep));
        dialog.show(getSupportFragmentManager(), "step_edit");
    }

    @Override
    public void onStepDelete(Step step, int position) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Step")
            .setMessage("Are you sure you want to delete this step?")
            .setPositiveButton("Delete", (dialog, which) -> 
                viewModel.removeStep(position))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onStepMoveUp(int position) {
        viewModel.moveStepUp(position);
    }

    @Override
    public void onStepMoveDown(int position) {
        viewModel.moveStepDown(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
