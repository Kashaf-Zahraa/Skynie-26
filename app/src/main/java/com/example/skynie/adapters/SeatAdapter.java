package com.example.skynie.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.models.Seat;

import java.util.ArrayList;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder>{

    Context context;
    ArrayList<Seat> seats=new ArrayList<>();
    ArrayList<Seat> selectedSeats=new ArrayList<>();

    public SeatAdapter(Context context, ArrayList<Seat> seats) {
        this.context = context;
        this.seats = seats;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_seat,parent,false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat item=seats.get(position);
        holder.seatNumber.setText(item.getFullSeatNumber());

        if (item.status.equals("Booked")) {
            holder.seatNumber.setBackgroundColor(ContextCompat.getColor(context, R.color.seat_booked));
        }
        else if (item.status.equals("Available")) {
            holder.seatNumber.setBackgroundColor(ContextCompat.getColor(context, R.color.seat_available));
        }

        holder.seatNumber.setOnClickListener((v)->{
            if(item.userId.isEmpty() && item.status.equals("Available")) {
                // Select seat
                holder.seatNumber.setBackgroundColor(ContextCompat.getColor(context, R.color.seat_selected));
                item.status = "Selected";
                selectedSeats.add(item);
            }
            else if (item.status.equals("Selected")) {
                holder.seatNumber.setBackgroundColor(ContextCompat.getColor(context, R.color.seat_available));
                item.status = "Available";
                selectedSeats.remove(item);
            }
            else if (item.status.equals("Booked")) {
                Toast.makeText(context, "Seat " + item.getFullSeatNumber() + " is already booked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return seats.size();
    }

    public ArrayList<Seat> getSelectedSeats() {
        return selectedSeats;
    }
    public static class SeatViewHolder extends RecyclerView.ViewHolder{
        TextView seatNumber;
        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            seatNumber=itemView.findViewById(R.id.seat_number);

        }
    }
}
