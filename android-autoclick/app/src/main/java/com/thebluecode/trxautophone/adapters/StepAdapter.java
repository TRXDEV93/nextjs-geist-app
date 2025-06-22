package com.thebluecode.trxautophone.adapters;

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

import com.google.android.material.card.MaterialCardView;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Step;

/**
 * Enhanced StepAdapter with improved UI/UX and drag-drop support
 */
public class StepAdapter extends ListAdapter<Step, StepAdapter.StepViewHolder> {
    private final OnStepClickListener listener;
    private boolean isEnabled = true;

    public interface OnStepClickListener {
        void onStepClick(Step step, int position);
        void onStepDeleteClick(Step step, int position);
        void onStepToggleEnabled(Step step, int position);
    }

    public StepAdapter(OnStepClickListener listener) {
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

    /**
     * Enable/disable interaction
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        notifyDataSetChanged();
    }

    class StepViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView typeIcon;
        private final TextView typeText;
        private final TextView descriptionText;
        private final TextView delayText;
        private final ImageButton deleteButton;
        private final ImageButton toggleButton;
        private final ImageView dragHandle;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            typeIcon = itemView.findViewById(R.id.stepTypeIcon);
            typeText = itemView.findViewById(R.id.stepType);
            descriptionText = itemView.findViewById(R.id.stepDescription);
            delayText = itemView.findViewById(R.id.stepDelay);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            toggleButton = itemView.findViewById(R.id.toggleButton);
            dragHandle = itemView.findViewById(R.id.dragHandle);
        }

        void bind(Step step, int position) {
            // Set step type icon and text
            typeIcon.setImageResource(getIconForStepType(step.getType()));
            typeText.setText(step.getType().getDisplayName());

            // Set step description
            String description = step.getDescription();
            if (description != null && !description.isEmpty()) {
                descriptionText.setText(description);
                descriptionText.setVisibility(View.VISIBLE);
            } else {
                descriptionText.setVisibility(View.GONE);
            }

            // Set delay text if applicable
            long delay = step.getDelay();
            if (delay > 0) {
                delayText.setText(formatDelay(delay));
                delayText.setVisibility(View.VISIBLE);
            } else {
                delayText.setVisibility(View.GONE);
            }

            // Update enabled state
            boolean isStepEnabled = step.isEnabled();
            cardView.setEnabled(isEnabled && isStepEnabled);
            float alpha = (isEnabled && isStepEnabled) ? 1.0f : 0.5f;
            cardView.setAlpha(alpha);

            // Update toggle button icon
            toggleButton.setImageResource(isStepEnabled ? 
                R.drawable.ic_enabled : R.drawable.ic_disabled);

            // Setup click listeners
            cardView.setOnClickListener(v -> {
                if (isEnabled) {
                    listener.onStepClick(step, position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (isEnabled) {
                    listener.onStepDeleteClick(step, position);
                }
            });

            toggleButton.setOnClickListener(v -> {
                if (isEnabled) {
                    listener.onStepToggleEnabled(step, position);
                }
            });

            // Show drag handle only when enabled
            dragHandle.setVisibility(isEnabled ? View.VISIBLE : View.GONE);

            // Long press for additional options
            cardView.setOnLongClickListener(v -> {
                if (isEnabled) {
                    showStepOptionsMenu(step, position);
                    return true;
                }
                return false;
            });
        }

        /**
         * Show step options menu
         */
        private void showStepOptionsMenu(Step step, int position) {
            PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
            popup.inflate(R.menu.menu_step_options);

            // Update menu items based on step state
            Menu menu = popup.getMenu();
            menu.findItem(R.id.action_enable).setVisible(!step.isEnabled());
            menu.findItem(R.id.action_disable).setVisible(step.isEnabled());

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        listener.onStepClick(step, position);
                        return true;

                    case R.id.action_delete:
                        listener.onStepDeleteClick(step, position);
                        return true;

                    case R.id.action_enable:
                    case R.id.action_disable:
                        listener.onStepToggleEnabled(step, position);
                        return true;

                    default:
                        return false;
                }
            });

            popup.show();
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

        /**
         * Format delay time for display
         */
        private String formatDelay(long delayMs) {
            if (delayMs < 1000) {
                return delayMs + "ms";
            } else {
                float seconds = delayMs / 1000f;
                return String.format("%.1fs", seconds);
            }
        }
    }

    /**
     * DiffUtil callback for efficient updates
     */
    private static class StepDiffCallback extends DiffUtil.ItemCallback<Step> {
        @Override
        public boolean areItemsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
            return oldItem.getType() == newItem.getType() &&
                   oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.getDelay() == newItem.getDelay() &&
                   oldItem.isEnabled() == newItem.isEnabled() &&
                   oldItem.getOrder() == newItem.getOrder();
        }
    }
}
