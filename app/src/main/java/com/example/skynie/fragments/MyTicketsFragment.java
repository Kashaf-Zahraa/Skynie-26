package com.example.skynie.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.adapters.TicketAdapter;
import com.example.skynie.models.Booking;
import com.example.skynie.views.TicketDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsFragment extends Fragment {

    private RecyclerView rvTickets;
    private LinearLayout llEmptyState;
    private TicketAdapter ticketAdapter;
    private List<Booking> bookingsList = new ArrayList<>();
    private DatabaseReference bookingsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_tickets, container, false);

        rvTickets = view.findViewById(R.id.rvTickets);
        llEmptyState = view.findViewById(R.id.llEmptyState);

        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");

        setupRecyclerView();
        loadUserTickets();

        return view;
    }

    private void setupRecyclerView() {
        rvTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        ticketAdapter = new TicketAdapter(bookingsList, booking -> {
            // Navigate to Ticket Details
            Intent intent = new Intent(getActivity(), TicketDetailsActivity.class);
            intent.putExtra("booking_id", booking.id);
            startActivity(intent);
        });
        rvTickets.setAdapter(ticketAdapter);
    }

    private void loadUserTickets() {
        // CRITICAL: Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Show empty state when not logged in
            rvTickets.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            return; // Exit early to prevent crash
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        bookingsRef.orderByChild("user_id").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookingsList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Booking booking = data.getValue(Booking.class);
                            if (booking != null && booking.status.equals("confirmed")) {
                                bookingsList.add(booking);
                            }
                        }

                        if (bookingsList.isEmpty()) {
                            rvTickets.setVisibility(View.GONE);
                            llEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            rvTickets.setVisibility(View.VISIBLE);
                            llEmptyState.setVisibility(View.GONE);
                        }

                        ticketAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Show empty state on error
                        rvTickets.setVisibility(View.GONE);
                        llEmptyState.setVisibility(View.VISIBLE);
                    }
                });
    }
}