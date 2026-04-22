package com.example.skynie.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.models.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketVH> {

    private final List<Booking> bookings;
    private final OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Booking booking);
    }

    public TicketAdapter(List<Booking> bookings, OnTicketClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_card, parent, false);
        return new TicketVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketVH holder, int position) {
        Booking booking = bookings.get(position);

        // You'll need to fetch movie & showtime details from Firebase
        // For now, using placeholder data
        holder.tvMovieTitle.setText("Movie Title"); // Fetch from movies table
        holder.tvCinemaName.setText("Cinema Name"); // Fetch from cinema/hall

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault());
        holder.tvShowDateTime.setText(sdf.format(new Date(booking.booking_date)));

        // Fetch ticket count from tickets table
        holder.tvTicketCount.setText("Tickets"); // Count from booking_seats

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class TicketVH extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvMovieTitle, tvCinemaName, tvShowDateTime, tvTicketCount;

        TicketVH(View v) {
            super(v);
            ivPoster = v.findViewById(R.id.ivPoster);
            tvMovieTitle = v.findViewById(R.id.tvMovieTitle);
            tvCinemaName = v.findViewById(R.id.tvCinemaName);
            tvShowDateTime = v.findViewById(R.id.tvShowDateTime);
            tvTicketCount = v.findViewById(R.id.tvTicketCount);
        }
    }
}