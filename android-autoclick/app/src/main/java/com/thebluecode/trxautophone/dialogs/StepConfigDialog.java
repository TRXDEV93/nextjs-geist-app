package com.thebluecode.trxautophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Enhanced dialog for configuring step parameters with improved validation
 */
public class StepConfigDialog extends DialogFragment {
    private static final String ARG_STEP_TYPE = "step_type";
    private static final String ARG_STEP = "step";

    private Step.StepType stepType;
    private Step existingStep;
    private OnStepConfiguredListener listener;
    private View contentView;

    public interface OnStepConfiguredListener {
        void onStepConfigured(Step step);
    }

    public static StepConfigDialog newInstance(Step.StepType type, @Nullable Step step) {
        StepConfigDialog dialog = new StepConfigDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STEP_TYPE, type);
        if (step != null) {
            args.putParcelable(ARG_STEP, step);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stepType = (Step.StepType) getArguments().getSerializable(ARG_STEP_TYPE);
            existingStep = getArguments().getParcelable(ARG_STEP);
        }
        try {
            listener = (OnStepConfiguredListener) getParentFragment();
            if (listener == null) {
                listener = (OnStepConfiguredListener) requireActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent must implement OnStepConfiguredListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        
        // Set title based on whether we're editing or creating
        builder.setTitle(existingStep != null ? R.string.edit_step : R.string.new_step);

        // Create content view
        contentView = createConfigView();
        builder.setView(contentView);

        // Add buttons
        builder.setPositiveButton(R.string.save, null); // We'll set this later
        builder.setNegativeButton(R.string.cancel, null);

        Dialog dialog = builder.create();

        // Override positive button to handle validation
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog)
                .getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (validateAndSave()) {
                    dialog.dismiss();
                }
            });
        });

        return dialog;
    }

    /**
     * Create configuration view based on step type
     */
    private View createConfigView() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        LinearLayout container = (LinearLayout) inflater.inflate(
            R.layout.dialog_step_config, null);

        // Add common fields
        TextInputLayout descriptionInput = container.findViewById(R.id.descriptionInput);
        TextInputLayout delayInput = container.findViewById(R.id.delayInput);

        // Set existing values if editing
        if (existingStep != null) {
            descriptionInput.getEditText().setText(existingStep.getDescription());
            delayInput.getEditText().setText(String.valueOf(existingStep.getDelay()));
        }

        // Add type-specific fields
        LinearLayout fieldsContainer = container.findViewById(R.id.fieldsContainer);
        addTypeSpecificFields(fieldsContainer);

        return container;
    }

    /**
     * Add configuration fields based on step type
     */
    private void addTypeSpecificFields(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        switch (stepType) {
            case TAP:
            case LONG_PRESS:
                addCoordinateFields(container, inflater);
                if (stepType == Step.StepType.LONG_PRESS) {
                    addDurationField(container, inflater);
                }
                break;

            case SWIPE:
                addSwipeFields(container, inflater);
                break;

            case TEXT_SEARCH:
                addTextSearchFields(container, inflater);
                break;

            case IMAGE_SEARCH:
                addImageSearchFields(container, inflater);
                break;

            case INPUT_TEXT:
                addInputTextField(container, inflater);
                break;

            case SYSTEM_KEY:
                addSystemKeyField(container, inflater);
                break;

            case DELAY:
                // Only uses common delay field
                break;

            case CONDITION:
                addConditionFields(container, inflater);
                break;

            case LOOP:
                addLoopFields(container, inflater);
                break;

            // Add more cases for other step types
        }
    }

    /**
     * Add coordinate input fields
     */
    private void addCoordinateFields(LinearLayout container, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.layout_coordinate_input, container, false);
        TextInputLayout xInput = view.findViewById(R.id.xCoordinate);
        TextInputLayout yInput = view.findViewById(R.id.yCoordinate);

        if (existingStep != null) {
            try {
                JSONObject data = new JSONObject(existingStep.getActionData());
                xInput.getEditText().setText(String.valueOf(data.optDouble("x", 0)));
                yInput.getEditText().setText(String.valueOf(data.optDouble("y", 0)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        container.addView(view);
    }

    /**
     * Add duration input field
     */
    private void addDurationField(LinearLayout container, LayoutInflater inflater) {
        TextInputLayout durationInput = (TextInputLayout) inflater.inflate(
            R.layout.layout_duration_input, container, false);

        if (existingStep != null) {
            try {
                JSONObject data = new JSONObject(existingStep.getActionData());
                long duration = data.optLong("duration", Constants.Defaults.DEFAULT_LONG_PRESS_DURATION);
                durationInput.getEditText().setText(String.valueOf(duration));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        container.addView(durationInput);
    }

    /**
     * Add swipe configuration fields
     */
    private void addSwipeFields(LinearLayout container, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.layout_swipe_input, container, false);
        TextInputLayout startXInput = view.findViewById(R.id.startX);
        TextInputLayout startYInput = view.findViewById(R.id.startY);
        TextInputLayout endXInput = view.findViewById(R.id.endX);
        TextInputLayout endYInput = view.findViewById(R.id.endY);
        TextInputLayout durationInput = view.findViewById(R.id.duration);

        if (existingStep != null) {
            try {
                JSONObject data = new JSONObject(existingStep.getActionData());
                startXInput.getEditText().setText(String.valueOf(data.optDouble("startX", 0)));
                startYInput.getEditText().setText(String.valueOf(data.optDouble("startY", 0)));
                endXInput.getEditText().setText(String.valueOf(data.optDouble("endX", 0)));
                endYInput.getEditText().setText(String.valueOf(data.optDouble("endY", 0)));
                durationInput.getEditText().setText(String.valueOf(
                    data.optLong("duration", Constants.Defaults.DEFAULT_SWIPE_DURATION)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        container.addView(view);
    }

    /**
     * Add text search configuration fields
     */
    private void addTextSearchFields(LinearLayout container, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.layout_text_search_input, container, false);
        TextInputLayout textInput = view.findViewById(R.id.searchText);
        CheckBox clickAfterFound = view.findViewById(R.id.clickAfterFound);

        if (existingStep != null) {
            try {
                JSONObject data = new JSONObject(existingStep.getActionData());
                textInput.getEditText().setText(data.optString("text", ""));
                clickAfterFound.setChecked(data.optBoolean("click", true));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        container.addView(view);
    }

    /**
     * Validate inputs and save step
     */
    private boolean validateAndSave() {
        try {
            // Get common fields
            TextInputLayout descriptionInput = contentView.findViewById(R.id.descriptionInput);
            TextInputLayout delayInput = contentView.findViewById(R.id.delayInput);

            String description = descriptionInput.getEditText().getText().toString().trim();
            String delayStr = delayInput.getEditText().getText().toString().trim();

            // Validate delay
            long delay = 0;
            if (!TextUtils.isEmpty(delayStr)) {
                try {
                    delay = Long.parseLong(delayStr);
                    if (delay < 0 || delay > Constants.Limits.MAX_DELAY) {
                        delayInput.setError(getString(R.string.error_invalid_delay));
                        return false;
                    }
                } catch (NumberFormatException e) {
                    delayInput.setError(getString(R.string.error_invalid_number));
                    return false;
                }
            }

            // Create or update step
            Step step = existingStep != null ? existingStep : new Step();
            step.setType(stepType);
            step.setDescription(description);
            step.setDelay(delay);

            // Set action data based on type
            JSONObject actionData = getActionDataForType();
            if (actionData != null) {
                step.setActionData(actionData.toString());
            }

            if (!step.isValid()) {
                showError(getString(R.string.error_invalid_step));
                return false;
            }

            listener.onStepConfigured(step);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showError(getString(R.string.error_saving_step));
            return false;
        }
    }

    /**
     * Get action data based on step type
     */
    private JSONObject getActionDataForType() throws JSONException {
        JSONObject data = new JSONObject();

        switch (stepType) {
            case TAP:
            case LONG_PRESS:
                TextInputLayout xInput = contentView.findViewById(R.id.xCoordinate);
                TextInputLayout yInput = contentView.findViewById(R.id.yCoordinate);

                if (!validateCoordinate(xInput) || !validateCoordinate(yInput)) {
                    return null;
                }

                data.put("x", Double.parseDouble(xInput.getEditText().getText().toString()));
                data.put("y", Double.parseDouble(yInput.getEditText().getText().toString()));

                if (stepType == Step.StepType.LONG_PRESS) {
                    TextInputLayout durationInput = contentView.findViewById(R.id.duration);
                    if (!validateDuration(durationInput)) {
                        return null;
                    }
                    data.put("duration", Long.parseLong(
                        durationInput.getEditText().getText().toString()));
                }
                break;

            case SWIPE:
                TextInputLayout startXInput = contentView.findViewById(R.id.startX);
                TextInputLayout startYInput = contentView.findViewById(R.id.startY);
                TextInputLayout endXInput = contentView.findViewById(R.id.endX);
                TextInputLayout endYInput = contentView.findViewById(R.id.endY);
                TextInputLayout swipeDurationInput = contentView.findViewById(R.id.duration);

                if (!validateCoordinate(startXInput) || !validateCoordinate(startYInput) ||
                    !validateCoordinate(endXInput) || !validateCoordinate(endYInput) ||
                    !validateDuration(swipeDurationInput)) {
                    return null;
                }

                data.put("startX", Double.parseDouble(startXInput.getEditText().getText().toString()));
                data.put("startY", Double.parseDouble(startYInput.getEditText().getText().toString()));
                data.put("endX", Double.parseDouble(endXInput.getEditText().getText().toString()));
                data.put("endY", Double.parseDouble(endYInput.getEditText().getText().toString()));
                data.put("duration", Long.parseLong(
                    swipeDurationInput.getEditText().getText().toString()));
                break;

            case TEXT_SEARCH:
                TextInputLayout textInput = contentView.findViewById(R.id.searchText);
                CheckBox clickAfterFound = contentView.findViewById(R.id.clickAfterFound);

                String searchText = textInput.getEditText().getText().toString().trim();
                if (TextUtils.isEmpty(searchText)) {
                    textInput.setError(getString(R.string.error_field_required));
                    return null;
                }

                data.put("text", searchText);
                data.put("click", clickAfterFound.isChecked());
                break;

            // Add more cases for other step types
        }

        return data;
    }

    /**
     * Validate coordinate input
     */
    private boolean validateCoordinate(TextInputLayout input) {
        String value = input.getEditText().getText().toString();
        if (TextUtils.isEmpty(value)) {
            input.setError(getString(R.string.error_field_required));
            return false;
        }
        try {
            double coordinate = Double.parseDouble(value);
            if (coordinate < 0) {
                input.setError(getString(R.string.error_invalid_coordinate));
                return false;
            }
        } catch (NumberFormatException e) {
            input.setError(getString(R.string.error_invalid_number));
            return false;
        }
        input.setError(null);
        return true;
    }

    /**
     * Validate duration input
     */
    private boolean validateDuration(TextInputLayout input) {
        String value = input.getEditText().getText().toString();
        if (TextUtils.isEmpty(value)) {
            input.setError(getString(R.string.error_field_required));
            return false;
        }
        try {
            long duration = Long.parseLong(value);
            if (duration < Constants.Limits.MIN_DELAY || duration > Constants.Limits.MAX_DELAY) {
                input.setError(getString(R.string.error_invalid_duration));
                return false;
            }
        } catch (NumberFormatException e) {
            input.setError(getString(R.string.error_invalid_number));
            return false;
        }
        input.setError(null);
        return true;
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
