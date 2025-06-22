package com.thebluecode.trxautophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced dialog for selecting step types with categorization
 */
public class StepTypeDialog extends DialogFragment {
    private OnStepTypeSelectedListener listener;

    public interface OnStepTypeSelectedListener {
        void onStepTypeSelected(Step.StepType type);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (OnStepTypeSelectedListener) getParentFragment();
            if (listener == null) {
                listener = (OnStepTypeSelectedListener) requireActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent must implement OnStepTypeSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.select_step_type);

        // Create and setup RecyclerView
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new StepTypeAdapter(createStepTypeCategories()));

        builder.setView(recyclerView);
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    /**
     * Create categorized step types
     */
    private Map<String, List<Step.StepType>> createStepTypeCategories() {
        Map<String, List<Step.StepType>> categories = new LinkedHashMap<>();

        // Basic Actions
        List<Step.StepType> basicActions = new ArrayList<>();
        basicActions.add(Step.StepType.TAP);
        basicActions.add(Step.StepType.LONG_PRESS);
        basicActions.add(Step.StepType.SWIPE);
        categories.put("Basic Actions", basicActions);

        // Search & Input
        List<Step.StepType> searchInput = new ArrayList<>();
        searchInput.add(Step.StepType.TEXT_SEARCH);
        searchInput.add(Step.StepType.IMAGE_SEARCH);
        searchInput.add(Step.StepType.INPUT_TEXT);
        categories.put("Search & Input", searchInput);

        // Flow Control
        List<Step.StepType> flowControl = new ArrayList<>();
        flowControl.add(Step.StepType.DELAY);
        flowControl.add(Step.StepType.CONDITION);
        flowControl.add(Step.StepType.LOOP);
        categories.put("Flow Control", flowControl);

        // System Actions
        List<Step.StepType> systemActions = new ArrayList<>();
        systemActions.add(Step.StepType.SYSTEM_KEY);
        systemActions.add(Step.StepType.PRESS_BACK);
        systemActions.add(Step.StepType.PRESS_HOME);
        categories.put("System Actions", systemActions);

        // App Control
        List<Step.StepType> appControl = new ArrayList<>();
        appControl.add(Step.StepType.LAUNCH_APP);
        appControl.add(Step.StepType.CLOSE_APP);
        categories.put("App Control", appControl);

        // Advanced
        List<Step.StepType> advanced = new ArrayList<>();
        advanced.add(Step.StepType.SCREENSHOT);
        advanced.add(Step.StepType.WAIT_FOR_ELEMENT);
        advanced.add(Step.StepType.SCROLL);
        categories.put("Advanced", advanced);

        return categories;
    }

    /**
     * Adapter for displaying categorized step types
     */
    private class StepTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_CATEGORY = 0;
        private static final int TYPE_ITEM = 1;

        private final List<Object> items = new ArrayList<>();

        StepTypeAdapter(Map<String, List<Step.StepType>> categories) {
            // Flatten categories and items into a single list
            for (Map.Entry<String, List<Step.StepType>> entry : categories.entrySet()) {
                items.add(entry.getKey()); // Category header
                items.addAll(entry.getValue()); // Step types in category
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof String ? TYPE_CATEGORY : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_CATEGORY) {
                return new CategoryViewHolder(inflater.inflate(
                    R.layout.item_step_type_category, parent, false));
            } else {
                return new ItemViewHolder(inflater.inflate(
                    R.layout.item_step_type, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_CATEGORY) {
                ((CategoryViewHolder) holder).bind((String) items.get(position));
            } else {
                ((ItemViewHolder) holder).bind((Step.StepType) items.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * ViewHolder for category headers
     */
    private static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryText;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
        }

        void bind(String category) {
            categoryText.setText(category);
        }
    }

    /**
     * ViewHolder for step type items
     */
    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView typeIcon;
        private final TextView typeText;
        private final TextView descriptionText;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIcon = itemView.findViewById(R.id.typeIcon);
            typeText = itemView.findViewById(R.id.typeText);
            descriptionText = itemView.findViewById(R.id.descriptionText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && 
                    items.get(position) instanceof Step.StepType) {
                    listener.onStepTypeSelected((Step.StepType) items.get(position));
                    dismiss();
                }
            });
        }

        void bind(Step.StepType type) {
            typeIcon.setImageResource(getIconForStepType(type));
            typeText.setText(type.getDisplayName());
            descriptionText.setText(type.getDescription());
        }

        /**
         * Get icon resource for step type
         */
        private int getIconForStepType(Step.StepType type) {
            switch (type) {
                case TAP:
                    return R.drawable.ic_tap;
                case LONG_PRESS:
                    return R.drawable.ic_long_press;
                case SWIPE:
                    return R.drawable.ic_swipe;
                case TEXT_SEARCH:
                    return R.drawable.ic_text_search;
                case IMAGE_SEARCH:
                    return R.drawable.ic_image_search;
                case DELAY:
                    return R.drawable.ic_delay;
                case CONDITION:
                    return R.drawable.ic_condition;
                case LOOP:
                    return R.drawable.ic_loop;
                case INPUT_TEXT:
                    return R.drawable.ic_input;
                case SYSTEM_KEY:
                    return R.drawable.ic_system;
                case LAUNCH_APP:
                    return R.drawable.ic_launch;
                case CLOSE_APP:
                    return R.drawable.ic_close;
                case PRESS_BACK:
                    return R.drawable.ic_back;
                case PRESS_HOME:
                    return R.drawable.ic_home;
                case TOGGLE_AIRPLANE_MODE:
                    return R.drawable.ic_airplane;
                case SCREENSHOT:
                    return R.drawable.ic_screenshot;
                case WAIT_FOR_ELEMENT:
                    return R.drawable.ic_wait;
                case SCROLL:
                    return R.drawable.ic_scroll;
                default:
                    return R.drawable.ic_step_default;
            }
        }
    }
}
