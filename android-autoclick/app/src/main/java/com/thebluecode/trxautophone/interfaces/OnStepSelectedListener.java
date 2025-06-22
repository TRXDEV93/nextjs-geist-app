package com.thebluecode.trxautophone.interfaces;

import com.thebluecode.trxautophone.models.Step;

/**
 * Interface for handling step selection events
 */
public interface OnStepSelectedListener {
    /**
     * Called when a step is selected
     * @param step The selected step
     * @param position The position of the selected step in the list
     */
    void onStepSelected(Step step, int position);

    /**
     * Called when a step is long pressed
     * @param step The long pressed step
     * @param position The position of the long pressed step in the list
     * @return true if the event was handled, false otherwise
     */
    boolean onStepLongPressed(Step step, int position);

    /**
     * Called when a step's enabled state is changed
     * @param step The step whose enabled state changed
     * @param position The position of the step in the list
     * @param enabled The new enabled state
     */
    void onStepEnabledChanged(Step step, int position, boolean enabled);

    /**
     * Called when a step is moved
     * @param fromPosition The starting position of the step
     * @param toPosition The ending position of the step
     */
    void onStepMoved(int fromPosition, int toPosition);

    /**
     * Called when a step is swiped to delete
     * @param step The step to be deleted
     * @param position The position of the step in the list
     */
    void onStepSwiped(Step step, int position);

    /**
     * Called when a step's configuration button is clicked
     * @param step The step to be configured
     * @param position The position of the step in the list
     */
    void onStepConfigClicked(Step step, int position);

    /**
     * Called when a step's test button is clicked
     * @param step The step to be tested
     * @param position The position of the step in the list
     */
    void onStepTestClicked(Step step, int position);

    /**
     * Called when a step's delay is changed
     * @param step The step whose delay changed
     * @param position The position of the step in the list
     * @param delay The new delay value in milliseconds
     */
    void onStepDelayChanged(Step step, int position, long delay);

    /**
     * Called when a step's action data is changed
     * @param step The step whose action data changed
     * @param position The position of the step in the list
     * @param actionData The new action data
     */
    void onStepActionDataChanged(Step step, int position, String actionData);

    /**
     * Called when a step's validation fails
     * @param step The step that failed validation
     * @param position The position of the step in the list
     * @param error The validation error message
     */
    void onStepValidationFailed(Step step, int position, String error);

    /**
     * Called when a step is duplicated
     * @param step The step to be duplicated
     * @param position The position of the step in the list
     */
    void onStepDuplicated(Step step, int position);

    /**
     * Called when a step's details should be shown
     * @param step The step whose details should be shown
     * @param position The position of the step in the list
     */
    void onStepDetailsRequested(Step step, int position);

    /**
     * Called when a step's help information is requested
     * @param step The step that needs help
     * @param position The position of the step in the list
     */
    void onStepHelpRequested(Step step, int position);

    /**
     * Called when a step's execution preview is requested
     * @param step The step to be previewed
     * @param position The position of the step in the list
     */
    void onStepPreviewRequested(Step step, int position);

    /**
     * Called when a step's error state changes
     * @param step The step whose error state changed
     * @param position The position of the step in the list
     * @param hasError Whether the step has an error
     * @param errorMessage The error message if hasError is true, null otherwise
     */
    void onStepErrorStateChanged(Step step, int position, boolean hasError, String errorMessage);
}
