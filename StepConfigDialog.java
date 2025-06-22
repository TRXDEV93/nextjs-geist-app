package com.thebluecode.trxautophone.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.thebluecode.trxautophone.models.Step;

public class StepConfigDialog extends DialogFragment {
    
    public interface OnStepConfiguredListener {
        void onStepConfigured(Step step);
    }
    
    private Step.Type stepType;
    private OnStepConfiguredListener listener;
    
    public StepConfigDialog(Step.Type stepType, OnStepConfiguredListener listener) {
        this.stepType = stepType;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        TextView title = new TextView(requireContext());
        title.setText("Configure " + getStepTypeName(stepType));
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);
        
        View configView = createConfigView(stepType);
        if (configView != null) {
            layout.addView(configView);
        }
        
        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> {
            Step step = createStepFromConfig(stepType, configView);
            if (listener != null && step != null) {
                listener.onStepConfigured(step);
            }
        });
        builder.setNegativeButton("Cancel", null);
        
        return builder.create();
    }
    
    private String getStepTypeName(Step.Type type) {
        switch (type) {
            case CLICK_TEXT: return "Click Text";
            case CLICK_IMAGE: return "Click Image";
            case CLICK_COORDINATE: return "Click Coordinate";
            case INPUT_TEXT: return "Input Text";
            case SWIPE_UP: return "Swipe Up";
            case SWIPE_DOWN: return "Swipe Down";
            case LAUNCH_APP: return "Launch App";
            case CLOSE_APP: return "Close App";
            case TOGGLE_AIRPLANE_MODE: return "Toggle Airplane Mode";
            default: return "Unknown";
        }
    }
    
    private View createConfigView(Step.Type type) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        
        switch (type) {
            case CLICK_TEXT:
            case INPUT_TEXT:
            case LAUNCH_APP:
                EditText editText = new EditText(requireContext());
                editText.setHint(getInputHint(type));
                editText.setTag("input_text");
                layout.addView(editText);
                break;
                
            case CLICK_COORDINATE:
                EditText editX = new EditText(requireContext());
                editX.setHint("X Coordinate");
                editX.setInputType(InputType.TYPE_CLASS_NUMBER);
                editX.setTag("input_x");
                layout.addView(editX);
                
                EditText editY = new EditText(requireContext());
                editY.setHint("Y Coordinate");
                editY.setInputType(InputType.TYPE_CLASS_NUMBER);
                editY.setTag("input_y");
                layout.addView(editY);
                break;
                
            case CLICK_IMAGE:
                EditText editImage = new EditText(requireContext());
                editImage.setHint("Image description or resource name");
                editImage.setTag("input_text");
                layout.addView(editImage);
                break;
            case DELAY:
                EditText editdelay = new EditText(requireContext());
                editdelay.setHint("delay timer");
                editdelay.setInputType(InputType.TYPE_CLASS_NUMBER);
                editdelay.setTag("timer");
                layout.addView(editdelay);
                break;
            case PRESS_BACK:
            case PRESS_HOME:
            case SWIPE_UP:
            case SWIPE_DOWN:
            case CLOSE_APP:
            case TOGGLE_AIRPLANE_MODE:
                TextView info = new TextView(requireContext());
                info.setText("This action requires no additional configuration.");
                info.setPadding(0, 20, 0, 20);
                layout.addView(info);
                break;
        }
        
        return layout;
    }
    
    private String getInputHint(Step.Type type) {
        switch (type) {
            case CLICK_TEXT: return "Text to click on";
            case INPUT_TEXT: return "Text to input";
            case LAUNCH_APP: return "App package name";
            default: return "Input value";
        }
    }
    
    private Step createStepFromConfig(Step.Type type, View configView) {
        if (configView == null) {
            return new Step(type, "");
        }
        
        LinearLayout layout = (LinearLayout) configView;
        
        switch (type) {
            case CLICK_TEXT:
            case INPUT_TEXT:
            case LAUNCH_APP:
            case CLICK_IMAGE:
                EditText editText = layout.findViewWithTag("input_text");
                if (editText != null) {
                    String value = editText.getText().toString().trim();
                    return new Step(type, value.isEmpty() ? "default" : value);
                }
                break;
                
            case CLICK_COORDINATE:
                EditText editX = layout.findViewWithTag("input_x");
                EditText editY = layout.findViewWithTag("input_y");
                if (editX != null && editY != null) {
                    try {
                        int x = Integer.parseInt(editX.getText().toString().trim());
                        int y = Integer.parseInt(editY.getText().toString().trim());
                        return new Step(type, x, y);
                    } catch (NumberFormatException e) {
                        return new Step(type, 0, 0);
                    }
                }
                break;
            case DELAY:
                EditText editdelay  = layout.findViewWithTag("timer");
                if (editdelay != null ) {
                    try {
                        int tg = Integer.parseInt(editdelay.getText().toString().trim());
                        return new Step(type, tg);
                    } catch (NumberFormatException e) {
                        return new Step(type, 0);
                    }
                }

                break;
            case PRESS_BACK:
            case PRESS_HOME:
            case SWIPE_UP:
            case SWIPE_DOWN:
            case CLOSE_APP:
            case TOGGLE_AIRPLANE_MODE:
                return new Step(type, "");
        }
        
        return new Step(type, "");
    }
}