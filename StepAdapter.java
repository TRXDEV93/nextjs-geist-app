package com.thebluecode.trxautophone.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Step;

public class StepAdapter extends ListAdapter<Step, StepAdapter.StepViewHolder> {

    public interface StepActionListener {
        void onStepEdit(Step step, int position);
        void onStepDelete(Step step, int position);
        void onStepMoveUp(int position);
        void onStepMoveDown(int position);
    }

    private final StepActionListener listener;
    private final Gson gson = new Gson();

    public StepAdapter(StepActionListener listener) {
        super(new StepDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = getItem(position);
        holder.bind(step, position);
    }

    class StepViewHolder extends RecyclerView.ViewHolder {
        private final ImageView stepTypeIcon;
        private final TextView stepType;
        private final TextView stepDetails;
        private final TextView stepDelay;
        private final ImageButton btnMoveUp;
        private final ImageButton btnMoveDown;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;
        private final View dragHandle;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepTypeIcon = itemView.findViewById(R.id.stepTypeIcon);
            stepType = itemView.findViewById(R.id.stepType);
            stepDetails = itemView.findViewById(R.id.stepDetails);
            stepDelay = itemView.findViewById(R.id.stepDelay);
            btnMoveUp = itemView.findViewById(R.id.btnMoveUp);
            btnMoveDown = itemView.findViewById(R.id.btnMoveDown);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            dragHandle = itemView.findViewById(R.id.dragHandle);
        }

        void bind(Step step, int position) {
            // Set step type icon based on step type
            setStepTypeIcon(step.getType());
            
            // Set step type text
            stepType.setText(getStepTypeText(step.getType()));
            
            // Set step details
            stepDetails.setText(getStepDetails(step));
            
            // Set delay if present
            if (step.getDelay() > 0) {
                stepDelay.setVisibility(View.VISIBLE);
                stepDelay.setText("Delay: " + step.getDelay() + "ms");
            } else {
                stepDelay.setVisibility(View.GONE);
            }

            // Configure move buttons visibility
            btnMoveUp.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
            btnMoveDown.setVisibility(position < getItemCount() - 1 ? View.VISIBLE : View.INVISIBLE);

            // Set click listeners
            btnMoveUp.setOnClickListener(v -> listener.onStepMoveUp(position));
            btnMoveDown.setOnClickListener(v -> listener.onStepMoveDown(position));
            btnEdit.setOnClickListener(v -> listener.onStepEdit(step, position));
            btnDelete.setOnClickListener(v -> listener.onStepDelete(step, position));
        }

        private void setStepTypeIcon(Step.StepType type) {
            int iconRes;
            switch (type) {
                case SYSTEM_KEY:
                    iconRes = android.R.drawable.ic_menu_preferences;
                    break;
                case TAP:
                    iconRes = android.R.drawable.ic_menu_compass;
                    break;
                case LONG_PRESS:
                    iconRes = android.R.drawable.ic_menu_edit;
                    break;
                case SWIPE:
                    iconRes = android.R.drawable.ic_menu_send;
                    break;
                case TEXT_SEARCH:
                    iconRes = android.R.drawable.ic_menu_search;
                    break;
                case IMAGE_SEARCH:
                    iconRes = android.R.drawable.ic_menu_gallery;
                    break;
                case DELAY:
                    iconRes = android.R.drawable.ic_menu_recent_history;
                    break;
                case CONDITION:
                    iconRes = android.R.drawable.ic_menu_help;
                    break;
                default:
                    iconRes = android.R.drawable.ic_menu_help;
            }
            stepTypeIcon.setImageResource(iconRes);
        }

        private String getStepTypeText(Step.StepType type) {
            switch (type) {
                case SYSTEM_KEY:
                    return "System Action";
                case TAP:
                    return "Tap";
                case LONG_PRESS:
                    return "Long Press";
                case SWIPE:
                    return "Swipe";
                case TEXT_SEARCH:
                    return "Find Text";
                case IMAGE_SEARCH:
                    return "Find Image";
                case DELAY:
                    return "Delay";
                case CONDITION:
                    return "Condition";
                default:
                    return "Unknown Action";
            }
        }

        private String getStepDetails(Step step) {
            try {
                JsonObject actionData = gson.fromJson(step.getActionData(), JsonObject.class);
                switch (step.getType()) {
                    case SYSTEM_KEY:
                        return "Action: " + getSystemKeyAction(actionData.get("action").getAsInt());
                    case TAP:
                        return String.format("Tap at (%d, %d)",
                            actionData.get("x").getAsInt(),
                            actionData.get("y").getAsInt());
                    case LONG_PRESS:
                        return String.format("Long press at (%d, %d) for %dms",
                            actionData.get("x").getAsInt(),
                            actionData.get("y").getAsInt(),
                            actionData.get("duration").getAsLong());
                    case SWIPE:
                        return String.format("Swipe from (%d, %d) to (%d, %d)",
                            actionData.get("startX").getAsInt(),
                            actionData.get("startY").getAsInt(),
                            actionData.get("endX").getAsInt(),
                            actionData.get("endY").getAsInt());
                    case TEXT_SEARCH:
                        return "Find text: " + actionData.get("text").getAsString();
                    case IMAGE_SEARCH:
                        return "Find image: " + actionData.get("imageId").getAsString();
                    case DELAY:
                        return "Wait for " + actionData.get("duration").getAsLong() + "ms";
                    case CONDITION:
                        return "If condition: " + actionData.get("condition").getAsString();
                    default:
                        return "No details available";
                }
            } catch (Exception e) {
                return "Error reading step details";
            }
        }

        private String getSystemKeyAction(int action) {
            switch (action) {
                case 1:
                    return "Back";
                case 2:
                    return "Home";
                case 3:
                    return "Recent Apps";
                default:
                    return "Unknown";
            }
        }
    }

    private static class StepDiffCallback extends DiffUtil.ItemCallback<Step> {
        @Override
        public boolean areItemsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
            return oldItem.getType() == newItem.getType() &&
                   oldItem.getActionData().equals(newItem.getActionData()) &&
                   oldItem.getDelay() == newItem.getDelay() &&
                   oldItem.getOrder() == newItem.getOrder();
        }
    }
}
