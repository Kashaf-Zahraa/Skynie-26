package com.example.skynie.views;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class CinemaDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_detail);

        String cinemaId      = getIntent().getStringExtra("cinema_id");
        String cinemaName    = getIntent().getStringExtra("cinema_name");
        String cinemaAddress = getIntent().getStringExtra("cinema_address");
        String screenTypes   = getIntent().getStringExtra("screen_types"); // "IMAX,GOLD,ScreenX"

        ImageButton btnBack    = findViewById(R.id.btnBack);
        ImageView   ivCinema   = findViewById(R.id.ivCinema);
        TextView    tvName     = findViewById(R.id.tvCinemaName);
        TextView    tvAddress  = findViewById(R.id.tvCinemaAddress);
        ChipGroup   cgFormats  = findViewById(R.id.cgScreenFormats);

        btnBack.setOnClickListener(v -> finish());

        if (tvName    != null && cinemaName    != null) tvName.setText(cinemaName);
        if (tvAddress != null && cinemaAddress != null) tvAddress.setText(cinemaAddress);

        // Cinema image — cinema id se drawable try karo
        if (ivCinema != null && cinemaId != null) {
            int resId = getResources().getIdentifier(
                    "cinema_" + cinemaId, "drawable", getPackageName());
            if (resId != 0) {
                ivCinema.setImageResource(resId);
            } else {
                ivCinema.setImageResource(R.drawable.ic_movie_placeholder);
            }
        }

        // Screen type chips
        if (cgFormats != null && screenTypes != null && !screenTypes.isEmpty()) {
            String[] types = screenTypes.split(",");
            for (String type : types) {
                Chip chip = new Chip(this);
                chip.setText(type.trim());
                chip.setChipBackgroundColorResource(R.color.chip_dark);
                chip.setTextColor(0xFFFFFFFF);
                chip.setClickable(false);
                cgFormats.addView(chip);
            }
        }
    }
}