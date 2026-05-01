package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.google.firebase.database.FirebaseDatabase;

public class SeatsActivity extends AppCompatActivity {
    ImageButton btnBack, btnFav;
    TextView tvFilmTitle, screenType, tvCinemaName, tvHallName, tvDate, tvTime;
    RecyclerView rvSeats;
    AppCompatButton btnProceedCheckout;

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
        init();
        getIntentValues();
        setOnClickListeners();
    }

    private void init() {
        btnBack = findViewById(R.id.btn_back);
        btnFav = findViewById(R.id.btn_fav);
        tvFilmTitle = findViewById(R.id.tv_film_title);
        screenType = findViewById(R.id.screenType);

//        tvCinemaName = findViewById(R.id.tv_cinema_name); // You need to add this ID in XML
//        tvHallName = findViewById(R.id.tv_hall_name); // You need to add this ID in XML
//        tvDate = findViewById(R.id.tv_date);
//        tvTime = findViewById(R.id.tv_time);
        rvSeats = findViewById(R.id.rv_seats);
        btnProceedCheckout = findViewById(R.id.btn_proceed_checkout);
    }
    private void getIntentValues() {
        Intent intent = getIntent();
        intent.getStringExtra("showtime_id");
        intent.getStringExtra("showtime_time");
        intent.getIntExtra("available_seats", 0);
        intent.getDoubleExtra("price", 0.0);
        intent.getStringExtra("movie_id");
        intent.getStringExtra("cinema_id");

        intent.getStringExtra("hall_id");
        intent.getStringExtra("hall_number");
        intent.getStringExtra("screen_type");
        intent.getIntExtra("total_seats", 0);

        intent.getStringExtra("audio_format");
    }
    private void setOnClickListeners() {
        btnBack.setOnClickListener((v) -> {
            startActivity(new Intent(this, BookingActivity.class));
            finish();
        });
        btnFav.setOnClickListener((v) -> {
            btnFav.setImageResource(R.drawable.ic_heart_filled);
            //for now
        });
        btnProceedCheckout.setOnClickListener((v) -> {
            startActivity(new Intent(this, OrderDetailsActivity.class));
            finish();
        });
    }

}