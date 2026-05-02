package com.example.skynie.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skynie.R;
import java.util.List;

public class ActorAdapter extends RecyclerView.Adapter<ActorAdapter.ActorViewHolder> {
    private List<String> actors;
    private boolean isLimited = true;
    private OnSeeMoreListener seeMoreListener;

    public interface OnSeeMoreListener {
        void onSeeMoreClicked(List<String> allActors);
    }

    public ActorAdapter(List<String> actors, OnSeeMoreListener listener) {
        this.actors = actors;
        this.seeMoreListener = listener;
    }

    @NonNull
    @Override
    public ActorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actor, parent, false);
        return new ActorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActorViewHolder holder, int position) {
        // Check if this is the "See More" button position
        if (isLimited && position == 3) {
            holder.tvActorName.setText("+ See More");
            holder.tvActorName.setTextColor(holder.itemView.getContext().getColor(R.color.skynie_red));
            holder.itemView.setOnClickListener(v -> {
                if (seeMoreListener != null) {
                    seeMoreListener.onSeeMoreClicked(actors);
                }
            });
        } else {
            String actor = actors.get(position);
            holder.tvActorName.setText(actor);
            holder.tvActorName.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        if (actors == null) return 0;
        // Show only 3 actors + 1 "See More" button if more than 3
        if (isLimited && actors.size() > 3) {
            return 4; // 3 actors + see more button
        }
        return actors.size();
    }

    static class ActorViewHolder extends RecyclerView.ViewHolder {
        TextView tvActorName;

        ActorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActorName = itemView.findViewById(R.id.tv_actor_name);
        }
    }
}