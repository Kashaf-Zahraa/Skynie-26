package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skynie.R;
import com.example.skynie.models.Booking;
import com.example.skynie.models.Seat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class OrderDetailsActivity extends AppCompatActivity {

    // ===== UI =====
    private ImageButton btnBack, btnFilter;
    private ImageView ivMoviePoster;
    private TextView tvMovieTitle, tvPgRating, tvLanguage, tvScreenType, tvAudioFormat;
    private TextView tvTheater, tvHall, tvDate, tvTime, tvTotal;
    private LinearLayout llTicketsContainer;
    private AppCompatButton btnPay;

    // ===== Data from Intent =====
    private ArrayList<Seat> selectedSeats;
    private double totalPrice;
    private String showtimeId, showtimeTime, movieId, cinemaId;
    private String hallId, hallNumber, screenType, audioFormat;
    private String movieTitle, cinemaName, moviePoster;
    private int availableSeats, totalSeats;
    private double pricePerSeat;
    private String hallShowtimeId;
    private String date;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        initViews();
        getIntentData();
        displayOrderInfo();
        displayTicketRows();
        setClickListeners();
    }

    // ===== Initialize Views =====
    private void initViews() {
        btnBack        = findViewById(R.id.btnBack);
        btnFilter      = findViewById(R.id.btnFilter);
        ivMoviePoster  = findViewById(R.id.ivMoviePoster);
        tvMovieTitle   = findViewById(R.id.tvMovieTitle);
        tvPgRating     = findViewById(R.id.tvPgRating);
        tvLanguage     = findViewById(R.id.tvLanguage);
        tvScreenType   = findViewById(R.id.tvScreenType);
        tvAudioFormat  = findViewById(R.id.tvAudioFormat);
        tvTheater      = findViewById(R.id.tvTheater);
        tvHall         = findViewById(R.id.tvHall);
        tvDate         = findViewById(R.id.tvDate);
        tvTime         = findViewById(R.id.tvTime);
        tvTotal        = findViewById(R.id.tvTotal);
        llTicketsContainer = findViewById(R.id.llTicketsContainer);
        btnPay         = findViewById(R.id.btnPay);
    }

    // ===== Get data passed from SeatsActivity =====
    @SuppressWarnings("unchecked")
    private void getIntentData() {
        Intent intent = getIntent();

        selectedSeats  = (ArrayList<Seat>) intent.getSerializableExtra("selected_seats");
        totalPrice     = intent.getDoubleExtra("total_price", 0.0);
        showtimeId     = intent.getStringExtra("showtime_id");
        showtimeTime   = intent.getStringExtra("showtime_time");
        movieId        = intent.getStringExtra("movie_id");
        cinemaId       = intent.getStringExtra("cinema_id");
        hallId         = intent.getStringExtra("hall_id");
        hallNumber     = intent.getStringExtra("hall_number");
        screenType     = intent.getStringExtra("screen_type");
        audioFormat    = intent.getStringExtra("audio_format");
        movieTitle     = intent.getStringExtra("movie_title");
        cinemaName     = intent.getStringExtra("cinema_name");
        moviePoster    = intent.getStringExtra("movie_poster");
        availableSeats = intent.getIntExtra("available_seats", 0);
        totalSeats     = intent.getIntExtra("total_seats", 0);
        pricePerSeat   = intent.getDoubleExtra("price", 0.0);
        hallShowtimeId = intent.getStringExtra("hallShowtime_id");
        time           = intent.getStringExtra("time");

        // Null checks
        if (selectedSeats == null) selectedSeats = new ArrayList<>();
        if (movieTitle    == null) movieTitle     = "Movie";
        if (cinemaName    == null) cinemaName      = "Cinema";
        if (hallNumber    == null) hallNumber      = "-";
        if (screenType    == null) screenType      = "-";
        if (audioFormat   == null) audioFormat     = "-";
        if (time          == null) time            = showtimeTime != null ? showtimeTime : "-";
    }

    // ===== Display movie info, theater, date, time =====
    private void displayOrderInfo() {
        // Movie title
        tvMovieTitle.setText(movieTitle);

        // Screen type & audio
        tvScreenType.setText(screenType);
        tvAudioFormat.setText(audioFormat);

        // Theater & hall
        tvTheater.setText(cinemaName);
        tvHall.setText(hallNumber);

        // Time
        tvTime.setText(time);

        // Date — aaj ki date
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        // Movie poster — drawable name se load
        if (moviePoster != null && !moviePoster.isEmpty()) {
            int resId = getResources().getIdentifier(
                    moviePoster, "drawable", getPackageName());
            if (resId != 0) {
                ivMoviePoster.setImageResource(resId);
            }
        }

        // Total
        tvTotal.setText(String.format(Locale.getDefault(), "%.2f USD", totalPrice));
    }

    // ===== Dynamically add seat rows in Tickets section =====
    private void displayTicketRows() {
        llTicketsContainer.removeAllViews();

        for (Seat seat : selectedSeats) {
            // Inflate a row view
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_order_seat_row, llTicketsContainer, false);

            TextView tvSeatLabel = row.findViewById(R.id.tvSeatLabel);
            TextView tvSeatPrice = row.findViewById(R.id.tvSeatPrice);

            String label = "Row " + seat.row + ", Seat " + seat.seatNumber;
            tvSeatLabel.setText(label);
            tvSeatPrice.setText(String.format(Locale.getDefault(), "%.0f USD", pricePerSeat));

            llTicketsContainer.addView(row);
        }
    }

    // ===== Click Listeners =====
    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFilter.setOnClickListener(v ->
                Toast.makeText(this, "Filter", Toast.LENGTH_SHORT).show());

        btnPay.setOnClickListener(v -> confirmBooking());
    }

    // ===== Save booking to Firebase and go to Ticket Details =====
    private void confirmBooking() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String bookingId = UUID.randomUUID().toString();

        // Generate a short booking reference
        String bookingRef = "BK-" + bookingId.substring(0, 8).toUpperCase();

        // Build Booking object
        Booking booking = new Booking(
                bookingId,
                userId,
                showtimeId,
                System.currentTimeMillis(),
                (float) totalPrice,
                "confirmed",
                "card",           // payment method placeholder
                "paid",           // payment status
                bookingRef
        );

        // Save to Firebase
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance()
                .getReference("bookings").child(bookingId);

        bookingsRef.setValue(booking)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_SHORT).show();

                    // Navigate to Ticket Details
                    Intent intent = new Intent(this, TicketDetailsActivity.class);
                    intent.putExtra("booking_id", bookingId);
                    intent.putExtra("movie_title", movieTitle);
                    intent.putExtra("cinema_name", cinemaName);
                    intent.putExtra("hall_number", hallNumber);
                    intent.putExtra("screen_type", screenType);
                    intent.putExtra("audio_format", audioFormat);
                    intent.putExtra("time", time);
                    intent.putExtra("total_price", totalPrice);
                    intent.putExtra("selected_seats", selectedSeats);
                    intent.putExtra("movie_poster", moviePoster);

                    // Clear back stack up to MainActivity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Booking failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}