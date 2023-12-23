package com.waterfall.wheresmytutor.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.student.ViewTutorActivity;
import com.waterfall.wheresmytutor.models.Tutor;

import java.util.ArrayList;
import java.util.List;


public class TutorsViewAdapter extends RecyclerView.Adapter<TutorsViewAdapter.TutorsViewHolder>{

    List<Tutor> tutors = new ArrayList<>();
    List<String> tutorsIdsWithAlternateIcon;
    private final Context context;
    private final PositionCallback callback;

    public TutorsViewAdapter(Context context, PositionCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public TutorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutor_list_item, parent, false);
        return new TutorsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorsViewHolder holder, int position) {
        Tutor tutor = tutors.get(position);
        holder.name.setText(tutor.getFullName());
        holder.status.setText(tutor.getStatus());

        setAlternateButtonIcon(holder, tutor);
        holder.button.setOnClickListener(v -> callback.onCallback(holder.getAdapterPosition()));

        holder.parent.setOnClickListener(v -> {
            Intent viewTutorIndent = new Intent(context, ViewTutorActivity.class);
            viewTutorIndent.putExtra( context.getString(R.string.tutorid_intent), tutor.getUserId());
            context.startActivity(viewTutorIndent);
        });
    }

    private void setAlternateButtonIcon(TutorsViewHolder holder, Tutor tutor)
    {
        if (tutorsIdsWithAlternateIcon == null)
        {
            holder.button.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_minus_icon));
            return;
        }

        if(tutorsIdsWithAlternateIcon.isEmpty()) { // current user has no tutor ids to give
            holder.button.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_add_icon));
            return;
        }

        if(!tutorsIdsWithAlternateIcon.contains(tutor.getUserId()))
        {
            holder.button.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_add_icon));
            return;
        }

        holder.button.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_minus_icon));
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    public void setTutors(List<Tutor> tutors) {
        this.tutors = tutors;
        notifyDataSetChanged();
    }

    public void setTutors(List<Tutor> tutors, List<String> tutorsIdsWithAlternateIcon) {
        this.tutors = tutors;
        this.tutorsIdsWithAlternateIcon = tutorsIdsWithAlternateIcon;
        notifyDataSetChanged();
    }

    public interface PositionCallback {
        void onCallback(int position);
    }

    public class TutorsViewHolder extends RecyclerView.ViewHolder {

        private final CardView parent;
        private final TextView name;
        private final TextView status;
        private final MaterialButton button;

        public TutorsViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.tutorListParent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                parent.setOutlineSpotShadowColor(context.getColor(R.color.primary_pink)); // On theme, shadow colour if phone supports it
            }
            name = itemView.findViewById(R.id.tutorNameItem);
            status = itemView.findViewById(R.id.tutorStatusItem);
            button = itemView.findViewById(R.id.tutorItemBtn);
        }
    }
}
