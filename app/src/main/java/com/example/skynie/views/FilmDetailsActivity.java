package com.example.skynie.views;

import android.os.Bundle;
import android.widget.ImageButton;
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

public class FilmDetailsActivity extends AppCompatActivity {
    ImageButton btnBack,btnFav,btnPlay;
    AppCompatButton btnBuyTicket;
    TextView tvFilmTitle, tvDescription, tvSeeMore;
    RecyclerView rvVideos, rvStars, rvRecommendations;
    CoordinatorLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_film_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    private void init(){
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnFav = findViewById(R.id.btn_fav);
        ImageButton btnPlay = findViewById(R.id.btn_play);
        AppCompatButton btnBuyTicket = findViewById(R.id.btn_buy_ticket);
        TextView tvFilmTitle = findViewById(R.id.tv_film_title);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvSeeMore = findViewById(R.id.tv_see_more);
        RecyclerView rvVideos = findViewById(R.id.rv_videos);
        RecyclerView rvStars = findViewById(R.id.rv_stars);
        RecyclerView rvRecommendations = findViewById(R.id.rv_recommendations);
        CoordinatorLayout main = findViewById(R.id.main);
    }
}