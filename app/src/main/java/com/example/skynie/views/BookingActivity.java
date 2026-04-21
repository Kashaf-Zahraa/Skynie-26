package com.example.skynie.views;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;

public class BookingActivity extends AppCompatActivity {
    CoordinatorLayout main;
    ImageButton btnBack;
    RecyclerView rvDays,rvFormats,rvShowTimes;
    ImageView ivMoviePoster,ivLocationArrow;
    TextView tvMovieTitle;
    AppCompatButton btnTrailer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    private void init(){
        main = findViewById(R.id.main);
        btnBack = findViewById(R.id.btn_back);
        rvDays = findViewById(R.id.rv_days);
        rvFormats = findViewById(R.id.rv_formats);
        rvShowTimes = findViewById(R.id.rv_show_times);
        ivMoviePoster = findViewById(R.id.iv_movie_poster);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        btnTrailer = findViewById(R.id.btn_trailer);
        ivLocationArrow = findViewById(R.id.iv_location_arrow);
    }
}