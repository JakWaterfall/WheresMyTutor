package com.waterfall.wheresmytutor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.waterfall.wheresmytutor.R;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityTimeSlotViewAdapter extends RecyclerView.Adapter<AvailabilityTimeSlotViewAdapter.AvailabilityTimeSlotViewHolder>{

    private List<String> timeSlots = new ArrayList<>();
    private final Context context;
    private final ItemDeleteCallback callback;

    public AvailabilityTimeSlotViewAdapter(Context context, ItemDeleteCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public AvailabilityTimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.avaliability_list_item, parent, false);
        return new AvailabilityTimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailabilityTimeSlotViewHolder holder, int position) {
        holder.timeSlotTxt.setText(timeSlots.get(position));

        holder.deletePopupMenu = new PopupMenu(context, holder.optionsBtn);
        holder.deletePopupMenu.getMenuInflater().inflate(R.menu.delete_menu, holder.deletePopupMenu.getMenu());
        holder.deletePopupMenu.setOnMenuItemClickListener(item -> {
            callback.onCallback(holder.getAdapterPosition());
            return true;
        });

        holder.optionsBtn.setOnClickListener(v -> holder.deletePopupMenu.show());
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public void setTimeSlots(List<String> timeSlots) {
        this.timeSlots = timeSlots;
        notifyDataSetChanged();
    }

    public interface ItemDeleteCallback {
        void onCallback(int position);
    }

    public class AvailabilityTimeSlotViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeSlotTxt;
        private final MaterialButton optionsBtn;
        private PopupMenu deletePopupMenu;


        public AvailabilityTimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeSlotTxt = itemView.findViewById(R.id.timeFrame);
            optionsBtn = itemView.findViewById(R.id.availabilityListItemMenuBtn);
        }
    }
}
