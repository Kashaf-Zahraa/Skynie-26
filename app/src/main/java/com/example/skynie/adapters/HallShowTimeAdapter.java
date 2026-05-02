package com.example.skynie.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.models.Hall;
import com.example.skynie.models.HallShowTime;
import com.example.skynie.models.Showtime;
import com.example.skynie.views.SeatsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HallShowTimeAdapter extends RecyclerView.Adapter<HallShowTimeAdapter.ViewHolder> {
    private List<HallShowTime> items = new ArrayList<>();
    private Map<String, Hall> hallMap = new HashMap<>();      // Cache for halls by ID
    private Map<String, Showtime> showtimeMap = new HashMap<>(); // Cache for showtimes by ID
    String movieTitle, cinemaName;
    String selectedDate;  // ✅ ADD THIS - to store date
    private Context context;

    public HallShowTimeAdapter(List<HallShowTime> items, Context context) {
        this.items = items != null ? items : new ArrayList<>();
        this.context = context;
    }

    // Update items and also provide the related hall and showtime data
    public void updateItems(List<HallShowTime> newItems, List<Hall> halls, List<Showtime> showtimes) {
        this.items = newItems != null ? newItems : new ArrayList<>();

        // Build hall map for quick lookup
        hallMap.clear();
        if (halls != null) {
            for (Hall hall : halls) {
                hallMap.put(hall.id, hall);
            }
        }

        // Build showtime map for quick lookup
        showtimeMap.clear();
        if (showtimes != null) {
            for (Showtime showtime : showtimes) {
                showtimeMap.put(showtime.id, showtime);
            }
        }

        notifyDataSetChanged();
    }

    // ✅ ADD THIS METHOD to set the selected date
    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_screen_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HallShowTime hallShowTime = items.get(position);

        // Get the actual Hall and Showtime objects from the maps
        String hallShowTimeId = hallShowTime.getId();
        Hall hall = hallMap.get(hallShowTime.getHallId());
        Showtime showtime = showtimeMap.get(hallShowTime.getShowtimeId());

        // Check if data exists
        if (hall == null || showtime == null) {
            holder.itemView.setVisibility(View.GONE);
            return;
        }

        holder.itemView.setVisibility(View.VISIBLE);

        // Set data
        holder.tvFormatType.setText(hall.screenType);
        holder.tvShowtime.setText(showtime.time);
        holder.tvHallNumber.setText(hall.hallNumber);
        holder.tvSeatsAvailable.setText(String.valueOf(showtime.availableSeats));
        holder.tvTotalSeats.setText(String.valueOf(hall.totalSeats) + " Available");

        // Calculate and set availability color
        int availabilityPercent = (showtime.availableSeats * 100) / hall.totalSeats;
        if (availabilityPercent < 20) {
            holder.tvSeatsAvailable.setTextColor(holder.itemView.getContext().getColor(R.color.skynie_red));
            holder.tvTotalSeats.setTextColor(holder.itemView.getContext().getColor(R.color.skynie_red));
        } else if (availabilityPercent < 50) {
            holder.tvSeatsAvailable.setTextColor(holder.itemView.getContext().getColor(R.color.availability_color));
            holder.tvTotalSeats.setTextColor(holder.itemView.getContext().getColor(R.color.availability_color));
        } else {
            holder.tvSeatsAvailable.setTextColor(holder.itemView.getContext().getColor(R.color.chip_green));
            holder.tvTotalSeats.setTextColor(holder.itemView.getContext().getColor(R.color.chip_green));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SeatsActivity.class);

            intent.putExtra("hallShowTime_id", hallShowTimeId);

            intent.putExtra("showtime_id", showtime.id);
            intent.putExtra("showtime_time", showtime.time);
            intent.putExtra("showtime_date", selectedDate);  // ✅ ADD THIS LINE - pass date
            intent.putExtra("available_seats", showtime.availableSeats);
            intent.putExtra("price", showtime.price);
            intent.putExtra("movie_id", showtime.movieId);
            intent.putExtra("cinema_id", showtime.cinemaId);
            intent.putExtra("movie_title", movieTitle);
            intent.putExtra("cinema_name", cinemaName);

            intent.putExtra("hall_id", hall.id);
            intent.putExtra("hall_number", hall.hallNumber);
            intent.putExtra("screen_type", hall.screenType);
            intent.putExtra("total_seats", hall.totalSeats);

            intent.putExtra("audio_format", hallShowTime.getAudioFormat());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // This method is already in your adapter
    public void setMovieAndCinema(String movieTitle, String cinemaName) {
        this.movieTitle = movieTitle;
        this.cinemaName = cinemaName;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvShowtime, tvHallNumber, tvFormatType;
        TextView tvSeatsAvailable, tvTotalSeats;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShowtime = itemView.findViewById(R.id.tv_showtime);
            tvFormatType = itemView.findViewById(R.id.tv_format_type);
            tvHallNumber = itemView.findViewById(R.id.tv_hall_number);
            tvSeatsAvailable = itemView.findViewById(R.id.tv_seats_available);
            tvTotalSeats = itemView.findViewById(R.id.tv_total_seats);
        }
    }
}