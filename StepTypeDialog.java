package com.thebluecode.trxautophone.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.thebluecode.trxautophone.models.Step;

public class StepTypeDialog extends DialogFragment {
    
    public interface OnStepTypeSelectedListener {
        void onStepTypeSelected(Step.Type type);
    }
    
    private OnStepTypeSelectedListener listener;
    
    public StepTypeDialog(OnStepTypeSelectedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] stepTypes = {
            "Click Text",
            "Click Image", 
            "Click Coordinate",
            "Input Text",
            "Swipe Up",
            "Swipe Down",
            "Launch App",
            "Close App",
            "Toggle Airplane Mode",
                "PRESS_BACK",
                "PRESS_HOME"
        };
        
        Step.Type[] types = {
            Step.Type.CLICK_TEXT,
            Step.Type.CLICK_IMAGE,
            Step.Type.CLICK_COORDINATE,
            Step.Type.INPUT_TEXT,
            Step.Type.SWIPE_UP,
            Step.Type.SWIPE_DOWN,
            Step.Type.LAUNCH_APP,
            Step.Type.CLOSE_APP,
            Step.Type.TOGGLE_AIRPLANE_MODE,
           Step.Type.PRESS_BACK,
            Step.Type.PRESS_HOME
        };
        
        return new AlertDialog.Builder(requireContext())
            .setTitle("Select Step Type")
            .setItems(stepTypes, (dialog, which) -> {
                if (listener != null) {
                    listener.onStepTypeSelected(types[which]);
                }
            })
            .setNegativeButton("Cancel", null)
            .create();
    }
}