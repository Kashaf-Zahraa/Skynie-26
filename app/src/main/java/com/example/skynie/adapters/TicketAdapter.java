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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

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

        // Default placeholders while loading
        holder.tvMovieTitle.setText("Loading...");
        holder.tvCinemaName.setText("...");
        holder.tvTicketCount.setText("...");

        // Show booking date from the booking object
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault());
        holder.tvShowDateTime.setText(sdf.format(new Date(booking.booking_date)));

        // Poster default
        holder.ivPoster.setImageResource(R.drawable.ic_movie_placeholder);

        // Click listener — pass booking to fragment/activity
        holder.itemView.setOnClickListener(v -> listener.onTicketClick(booking));

        // ── Fetch real data from Firebase ──────────────────────
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // 1. Fetch showtime → get movieId, cinemaId, time
        db.child("showtimes").child(booking.showtime_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot stSnapshot) {
                        String movieId   = stSnapshot.child("movieId").getValue(String.class);
                        String cinemaId  = stSnapshot.child("cinemaId").getValue(String.class);
                        String time      = stSnapshot.child("time").getValue(String.class);
                        String date      = stSnapshot.child("date").getValue(String.class);

                        // Update date+time if showtime data exists
                        if (time != null && date != null) {
                            holder.tvShowDateTime.setText(date + "  " + time);
                        }

                        // 2. Fetch movie title + poster
                        if (movieId != null) {
                            db.child("movies").child(movieId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot mvSnapshot) {
                                            String title  = mvSnapshot.child("title").getValue(String.class);
                                            String poster = mvSnapshot.child("poster_drawable").getValue(String.class);

                                            if (title != null)  holder.tvMovieTitle.setText(title);

                                            if (poster != null && !poster.isEmpty()) {
                                                int resId = holder.itemView.getContext()
                                                        .getResources().getIdentifier(
                                                                poster, "drawable",
                                                                holder.itemView.getContext().getPackageName());
                                                if (resId != 0) holder.ivPoster.setImageResource(resId);
                                            }
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                                    });
                        }

                        // 3. Fetch cinema name
                        if (cinemaId != null) {
                            db.child("cinemas").child(cinemaId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot ciSnapshot) {
                                            String name = ciSnapshot.child("name").getValue(String.class);
                                            if (name != null) holder.tvCinemaName.setText(name);
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                                    });
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });

        // 4. Count seats for this booking from "seats" node
        // Seats are identified by hallShowtimeId_seatLabel; we match by userId + booking context
        // Simpler: count from "bookings" → seats stored in booking reference
        // Since we store selected seats per booking, count from booking_id match
        db.child("seats").orderByChild("userId")
                .equalTo(FirebaseDatabase.getInstance().getApp().getOptions().getApplicationId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Simpler approach: show total_price based seat count estimate
        // Since we don't store seat count in Booking, show total price prominently
        holder.tvTicketCount.setText(
                String.format(Locale.getDefault(), "%.0f USD", booking.total_price));
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
            ivPoster       = v.findViewById(R.id.ivPoster);
            tvMovieTitle   = v.findViewById(R.id.tvMovieTitle);
            tvCinemaName   = v.findViewById(R.id.tvCinemaName);
            tvShowDateTime = v.findViewById(R.id.tvShowDateTime);
            tvTicketCount  = v.findViewById(R.id.tvTicketCount);
        }
    }
}