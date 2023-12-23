package com.waterfall.wheresmytutor.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.ViewMeetingActivity;
import com.waterfall.wheresmytutor.models.Meeting;

import java.util.ArrayList;
import java.util.List;

public class MeetingsViewAdapter extends RecyclerView.Adapter<MeetingsViewAdapter.MeetingsViewHolder>{
    private List<Meeting> meetings = new ArrayList<>();
    private final Context context;
    private final ItemDeleteCallback callback;
    private final String userType;
    public MeetingsViewAdapter(Context context, String userType, ItemDeleteCallback callback) {
        this.context = context;
        this.callback = callback;
        this.userType = userType;
    }

    @NonNull
    @Override
    public MeetingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_list_item, parent, false);
        return new MeetingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingsViewHolder holder, int position) {
        Meeting meeting = meetings.get(position);
        holder.name.setText(userType.equals(context.getString(R.string.user_type_tutor)) ? meeting.getStudentName() : meeting.getTutorName());
        holder.smallText.setText(String.format("%s\t\t\t%s", meeting.getDate(), meeting.getTimeSlot()));

        Intent viewMeetingIntent = new Intent(context, ViewMeetingActivity.class);
        viewMeetingIntent.putExtra(context.getString(R.string.meetingid_intent), meeting.getMeetingId());
        viewMeetingIntent.putExtra(context.getString(R.string.usertype_intent), userType);
        holder.parent.setOnClickListener(v -> context.startActivity(viewMeetingIntent));

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
        return meetings.size();
    }

    public void setMeetings(List<Meeting> meetings) {
        this.meetings = meetings;
        notifyDataSetChanged();
    }

    public interface ItemDeleteCallback {
        void onCallback(int position);
    }

    public class MeetingsViewHolder extends RecyclerView.ViewHolder {
        private final CardView parent;
        private final TextView name;
        private final TextView smallText;
        private final MaterialButton optionsBtn;
        private PopupMenu deletePopupMenu;

        public MeetingsViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.meetingListParent);
            name = itemView.findViewById(R.id.meetingNameItem);
            smallText = itemView.findViewById(R.id.meetingSmallTextItem);
            optionsBtn = itemView.findViewById(R.id.meetingItemBtn);
        }
    }
}
