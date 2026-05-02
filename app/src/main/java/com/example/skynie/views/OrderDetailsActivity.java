package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skynie.R;
import com.example.skynie.models.Booking;
import com.example.skynie.models.Seat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class OrderDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack, btnFilter;
    private ImageView ivMoviePoster;
    private TextView tvMovieTitle, tvPgRating, tvLanguage, tvScreenType, tvAudioFormat;
    private TextView tvTheater, tvHall, tvDate, tvTime, tvTotal;
    private LinearLayout llTicketsContainer;
    private AppCompatButton btnPay;

    private ArrayList<Seat> selectedSeats;
    private double totalPrice, pricePerSeat;
    private String showtimeId, showtimeTime, movieId, cinemaId;
    private String hallId, hallNumber, screenType, audioFormat;
    private String movieTitle, cinemaName, moviePoster, pgRating, language;
    private String hallShowtimeId, time, showtimeDate;
    private int availableSeats, totalSeats;

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

    private void initViews() {
        btnBack            = findViewById(R.id.btnBack);
        btnFilter          = findViewById(R.id.btnFilter);
        ivMoviePoster      = findViewById(R.id.ivMoviePoster);
        tvMovieTitle       = findViewById(R.id.tvMovieTitle);
        tvPgRating         = findViewById(R.id.tvPgRating);
        tvLanguage         = findViewById(R.id.tvLanguage);
        tvScreenType       = findViewById(R.id.tvScreenType);
        tvAudioFormat      = findViewById(R.id.tvAudioFormat);
        tvTheater          = findViewById(R.id.tvTheater);
        tvHall             = findViewById(R.id.tvHall);
        tvDate             = findViewById(R.id.tvDate);
        tvTime             = findViewById(R.id.tvTime);
        tvTotal            = findViewById(R.id.tvTotal);
        llTicketsContainer = findViewById(R.id.llTicketsContainer);
        btnPay             = findViewById(R.id.btnPay);
    }

    @SuppressWarnings("unchecked")
    private void getIntentData() {
        Intent i = getIntent();
        selectedSeats  = (ArrayList<Seat>) i.getSerializableExtra("selected_seats");
        totalPrice     = i.getDoubleExtra("total_price", 0.0);
        pricePerSeat   = i.getDoubleExtra("price", 0.0);
        showtimeId     = i.getStringExtra("showtime_id");
        showtimeTime   = i.getStringExtra("showtime_time");
        movieId        = i.getStringExtra("movie_id");
        cinemaId       = i.getStringExtra("cinema_id");
        hallId         = i.getStringExtra("hall_id");
        hallNumber     = i.getStringExtra("hall_number");
        screenType     = i.getStringExtra("screen_type");
        audioFormat    = i.getStringExtra("audio_format");
        movieTitle     = i.getStringExtra("movie_title");
        cinemaName     = i.getStringExtra("cinema_name");
        moviePoster    = i.getStringExtra("movie_poster");
        pgRating       = i.getStringExtra("pg_rating");
        language       = i.getStringExtra("language");
        hallShowtimeId = i.getStringExtra("hallShowtime_id");
        time           = i.getStringExtra("time");
        showtimeDate   = i.getStringExtra("showtime_date");
        availableSeats = i.getIntExtra("available_seats", 0);
        totalSeats     = i.getIntExtra("total_seats", 0);

        if (selectedSeats == null) selectedSeats = new ArrayList<>();
        if (movieTitle == null)    movieTitle    = "";
        if (cinemaName == null)    cinemaName    = "";
        if (hallNumber == null)    hallNumber    = "";
        if (screenType == null)    screenType    = "";
        if (audioFormat == null)   audioFormat   = "";
        if (pgRating == null)      pgRating      = "";
        if (language == null)      language      = "";
        if (time == null)          time          = showtimeTime != null ? showtimeTime : "";
    }

    private void displayOrderInfo() {
        tvMovieTitle.setText(movieTitle);

        tvPgRating.setText(pgRating);
        tvPgRating.setVisibility(pgRating.isEmpty() ? View.GONE : View.VISIBLE);

        tvLanguage.setText(language);
        tvLanguage.setVisibility(language.isEmpty() ? View.GONE : View.VISIBLE);

        // screenType = e.g. "ScreenX", audioFormat = e.g. "Dolby Atmos"
        tvScreenType.setText(screenType);
        tvScreenType.setVisibility(screenType.isEmpty() ? View.GONE : View.VISIBLE);

        tvAudioFormat.setText(audioFormat);
        tvAudioFormat.setVisibility(audioFormat.isEmpty() ? View.GONE : View.VISIBLE);

        // Real cinema name — no "Cinema" fallback
        tvTheater.setText(cinemaName);
        tvHall.setText(hallNumber);
        tvTime.setText(time);

        // Date — use showtime date if passed, else today
        if (showtimeDate != null && !showtimeDate.isEmpty()) {
            try {
                SimpleDateFormat inFmt  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outFmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date d = inFmt.parse(showtimeDate);
                tvDate.setText(d != null ? outFmt.format(d) : showtimeDate);
            } catch (Exception e) {
                tvDate.setText(showtimeDate);
            }
        } else {
            tvDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
        }

        // Poster from drawable name
        if (moviePoster != null && !moviePoster.isEmpty()) {
            int resId = getResources().getIdentifier(moviePoster, "drawable", getPackageName());
            if (resId != 0) ivMoviePoster.setImageResource(resId);
        }

        tvTotal.setText(String.format(Locale.getDefault(), "%.2f USD", totalPrice));
    }

    private void displayTicketRows() {
        llTicketsContainer.removeAllViews();
        for (Seat seat : selectedSeats) {
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_order_seat_row, llTicketsContainer, false);
            ((TextView) row.findViewById(R.id.tvSeatLabel))
                    .setText("Row " + seat.row + ", Seat " + seat.seatNumber);
            ((TextView) row.findViewById(R.id.tvSeatPrice))
                    .setText(String.format(Locale.getDefault(), "%.0f USD", pricePerSeat));
            llTicketsContainer.addView(row);
        }
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish()); // sirf back — no new activity

        btnFilter.setOnClickListener(v ->
                Toast.makeText(this, "Filter", Toast.LENGTH_SHORT).show());

        btnPay.setOnClickListener(v -> confirmBooking());
    }

    private void confirmBooking() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId    = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String bookingId = UUID.randomUUID().toString();
        String bookingRef = "BK-" + bookingId.substring(0, 8).toUpperCase();

        Booking booking = new Booking(
                bookingId, userId, showtimeId,
                System.currentTimeMillis(), (float) totalPrice,
                "confirmed", "card", "paid", bookingRef
        );

        FirebaseDatabase.getInstance().getReference("bookings")
                .child(bookingId)
                .setValue(booking)
                .addOnSuccessListener(unused -> {
                    // Build seats summary string: "A5, A6, B3"
                    StringBuilder sb = new StringBuilder();
                    for (int idx = 0; idx < selectedSeats.size(); idx++) {
                        if (idx > 0) sb.append(", ");
                        Seat s = selectedSeats.get(idx);
                        sb.append(s.row).append(s.seatNumber);
                    }

                    Intent intent = new Intent(this, TicketDetailsActivity.class);
                    intent.putExtra("booking_id",    bookingId);
                    intent.putExtra("booking_ref",   bookingRef);
                    intent.putExtra("movie_title",   movieTitle);
                    intent.putExtra("cinema_name",   cinemaName);
                    intent.putExtra("hall_number",   hallNumber);
                    intent.putExtra("screen_type",   screenType);
                    intent.putExtra("audio_format",  audioFormat);
                    intent.putExtra("pg_rating",     pgRating);
                    intent.putExtra("language",      language);
                    intent.putExtra("time",          time);
                    intent.putExtra("showtime_date", showtimeDate);
                    intent.putExtra("total_price",   totalPrice);
                    intent.putExtra("seat_count",    selectedSeats.size());
                    intent.putExtra("seats_label",   sb.toString());
                    intent.putExtra("movie_poster",  moviePoster);
                    intent.putExtra("selected_seats", selectedSeats);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Booking failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}