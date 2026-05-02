package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.adapters.SeatAdapter;
import com.example.skynie.models.Seat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SeatsActivity extends AppCompatActivity {

    ImageButton btnBack, btnFav;
    TextView tvFilmTitle, tvscreenType, tvCinema, tvHall, tvDate, tvTime;
    RecyclerView rvSeats;
    SeatAdapter adapter;

    // ✅ ADDED showtimeDate here
    String showtimeId, showtimeTime, showtimeDate, movieId, cinemaId, hallId, hallNumber,
            screenType, audioFormat, movieTitle, cinemaName;
    int availableSeats, totalSeats;
    double price;
    ArrayList<Seat> seats = new ArrayList<>();
    ArrayList<Seat> selectedSeats = new ArrayList<>();
    AppCompatButton btnProceedCheckout;
    String hallShowTimeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentValues();
        init();
        setMovieInfo();
        getSeatsFromFirebase(hallShowTimeId);
        setOnClickListeners();
    }

    private void init() {
        btnBack             = findViewById(R.id.btn_back);
        btnFav              = findViewById(R.id.btn_fav);
        tvFilmTitle         = findViewById(R.id.tv_film_title);
        tvscreenType        = findViewById(R.id.screenType);
        tvCinema            = findViewById(R.id.tv_cinema);
        tvHall              = findViewById(R.id.tv_hall_number);
        tvDate              = findViewById(R.id.tv_date);
        tvTime              = findViewById(R.id.tv_time);
        rvSeats             = findViewById(R.id.rv_seats);
        btnProceedCheckout  = findViewById(R.id.btn_proceed_checkout);

        rvSeats.setHasFixedSize(true);
        rvSeats.setLayoutManager(new GridLayoutManager(this, 8));
        adapter = new SeatAdapter(this, seats);
        rvSeats.setAdapter(adapter);
    }

    private void getIntentValues() {
        Intent intent = getIntent();
        hallShowTimeId = intent.getStringExtra("hallShowTime_id");
        showtimeId     = intent.getStringExtra("showtime_id");
        showtimeTime   = intent.getStringExtra("showtime_time");
        showtimeDate   = intent.getStringExtra("showtime_date");  // ✅ ADD THIS LINE
        availableSeats = intent.getIntExtra("available_seats", 0);
        price          = intent.getDoubleExtra("price", 0.0);
        movieId        = intent.getStringExtra("movie_id");
        cinemaId       = intent.getStringExtra("cinema_id");
        hallId         = intent.getStringExtra("hall_id");
        hallNumber     = intent.getStringExtra("hall_number");
        screenType     = intent.getStringExtra("screen_type");
        totalSeats     = intent.getIntExtra("total_seats", 0);
        movieTitle     = intent.getStringExtra("movie_title");
        cinemaName     = intent.getStringExtra("cinema_name");
        audioFormat    = intent.getStringExtra("audio_format");

        // ✅ Set date on TextView if available
        if (tvDate != null && showtimeDate != null) {
            tvDate.setText(showtimeDate);
        }
    }

    private void setMovieInfo() {
        if (tvscreenType != null && screenType != null) tvscreenType.setText(screenType);
        if (tvHall       != null && hallNumber != null) tvHall.setText(hallNumber);
        if (tvTime       != null && showtimeTime != null) tvTime.setText(showtimeTime);
        if (tvFilmTitle  != null && movieTitle != null)  tvFilmTitle.setText(movieTitle);
        if (tvCinema     != null && cinemaName != null)  tvCinema.setText(cinemaName);
    }

    private void getSeatsFromFirebase(String hallShowTimeId) {
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference("seats");

        databaseReference.orderByChild("hallShowtimeId").equalTo(hallShowTimeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        seats.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot seatSnapshot : snapshot.getChildren()) {
                                Seat seat = seatSnapshot.getValue(Seat.class);
                                if (seat != null) seats.add(seat);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(SeatsActivity.this,
                                    "No seats found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SeatsActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setOnClickListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnFav.setOnClickListener(v ->
                btnFav.setImageResource(R.drawable.ic_heart_filled));

        btnProceedCheckout.setOnClickListener(v -> {
            selectedSeats = adapter.getSelectedSeats();

            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Please select at least one seat",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            double totalPrice = selectedSeats.size() * price;

            // Write booked seats to Firebase
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            String userId = "";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            final String finalUserId = userId;

            for (Seat seat : selectedSeats) {
                seat.status = "Booked";
                seat.userId = finalUserId;
                String key = seat.getHallShowtimeId() + "_" + seat.getFullSeatNumber();
                dbRef.child("seats").child(key).setValue(seat);
            }
            adapter.notifyDataSetChanged();

            // Build seats label
            StringBuilder sb = new StringBuilder();
            for (int idx = 0; idx < selectedSeats.size(); idx++) {
                if (idx > 0) sb.append(", ");
                sb.append(selectedSeats.get(idx).getFullSeatNumber());
            }

            Intent intent = new Intent(this, OrderDetailsActivity.class);
            intent.putExtra("selected_seats",   selectedSeats);
            intent.putExtra("total_price",       totalPrice);
            intent.putExtra("hallShowtime_id",   hallShowTimeId);
            intent.putExtra("showtime_id",       showtimeId);
            intent.putExtra("showtime_time",     showtimeTime);
            intent.putExtra("showtime_date",     showtimeDate);  // ✅ ADD THIS LINE
            intent.putExtra("available_seats",   availableSeats);
            intent.putExtra("price",             price);
            intent.putExtra("movie_id",          movieId);
            intent.putExtra("cinema_id",         cinemaId);
            intent.putExtra("hall_id",           hallId);
            intent.putExtra("hall_number",       hallNumber);
            intent.putExtra("screen_type",       screenType);
            intent.putExtra("total_seats",       totalSeats);
            intent.putExtra("audio_format",      audioFormat);
            intent.putExtra("movie_title",       movieTitle);
            intent.putExtra("cinema_name",       cinemaName);
            intent.putExtra("seats_label",       sb.toString());
            intent.putExtra("time",              tvTime.getText().toString().trim());
            startActivity(intent);
        });
    }
}