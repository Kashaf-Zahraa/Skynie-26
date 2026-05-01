package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skynie.R;
import com.example.skynie.models.Seat;

import java.util.ArrayList;

public class OrderDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentValues();
    }

    private void getIntentValues() {
        Intent intent = getIntent();
        ArrayList<Seat> selectedSeats = (ArrayList<Seat>) intent.getSerializableExtra("selected_seats");
        double totalPrice = intent.getDoubleExtra("total_price", 0.0);
        String hallShowtimeId = intent.getStringExtra("hallShowtime_id");
        String showtimeId = intent.getStringExtra("showtime_id");
        String showtimeTime = intent.getStringExtra("showtime_time");
        int availableSeats = intent.getIntExtra("available_seats", 0);
        double price = intent.getDoubleExtra("price", 0.0);
        String movieId = intent.getStringExtra("movie_id");
        String movieTitle = intent.getStringExtra("movie_title");
        String cinemaId = intent.getStringExtra("cinema_id");
        String cinemaName = intent.getStringExtra("cinema_name");
        String hallId = intent.getStringExtra("hall_id");
        String hallNumber = intent.getStringExtra("hall_number");
        String screenType = intent.getStringExtra("screen_type");
        int totalSeats = intent.getIntExtra("total_seats", 0);
        String audioFormat = intent.getStringExtra("audio_format");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
    }
}