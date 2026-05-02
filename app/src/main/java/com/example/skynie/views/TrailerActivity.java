package com.example.skynie.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skynie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrailerActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageButton btnBack;
    private TextView tvMovieTitle, tvMeta, badgeAge, badgeLang;
    private SeekBar seekBar;
    private ImageView ivThumbnail;
    private LinearLayout btnPlayYouTube;

    private String movieId, movieTitle, trailerUrl, pgRating, language, moviePoster;
    private int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trailer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        initViews();
        getIntentData();
        populateUI();

        btnBack.setOnClickListener(v -> finish());
        if (seekBar != null) seekBar.setEnabled(false);
    }

    private void initViews() {
        progressBar    = findViewById(R.id.progressBar);
        btnBack        = findViewById(R.id.btnBack);
        tvMovieTitle   = findViewById(R.id.tvMovieTitle);
        tvMeta         = findViewById(R.id.tvMeta);
        badgeAge       = findViewById(R.id.badgeAge);
        badgeLang      = findViewById(R.id.badgeLang);

        ivThumbnail    = findViewById(R.id.ivThumbnail);
        btnPlayYouTube = findViewById(R.id.btnPlayYouTube);
    }

    private void getIntentData() {
        movieId     = getIntent().getStringExtra("movie_id");
        movieTitle  = getIntent().getStringExtra("movie_title");
        trailerUrl  = getIntent().getStringExtra("trailer_url");
        pgRating    = getIntent().getStringExtra("pg_rating");
        language    = getIntent().getStringExtra("language");
        moviePoster = getIntent().getStringExtra("movie_poster");
        duration    = getIntent().getIntExtra("movie_duration", 0);
    }

    private void populateUI() {
        // ── Title ──
        if (tvMovieTitle != null && movieTitle != null)
            tvMovieTitle.setText(movieTitle);

        // ── Meta ──
        StringBuilder meta = new StringBuilder("NEW");
        if (pgRating != null && !pgRating.isEmpty()) meta.append(" · ").append(pgRating);
        if (duration > 0) {
            int h = duration / 60, m = duration % 60;
            meta.append(" · ").append(h).append("h ").append(m).append("m");
        }
        if (tvMeta != null) tvMeta.setText(meta.toString());

        // ── Language badge ──
        if (badgeLang != null && language != null && !language.isEmpty()) {
            String lang = language.length() >= 2
                    ? language.substring(0, 2).toUpperCase() : language.toUpperCase();
            badgeLang.setText(lang);
        }

        // ── Age badge ──
        if (badgeAge != null && pgRating != null && !pgRating.isEmpty())
            badgeAge.setText(pgRating);

        // ── Movie thumbnail (poster ya backdrop se) ──
        if (ivThumbnail != null && moviePoster != null && !moviePoster.isEmpty()) {
            int resId = getResources().getIdentifier(moviePoster, "drawable", getPackageName());
            if (resId != 0) ivThumbnail.setImageResource(resId);
        }

        // ── Play button click → YouTube ──
        if (btnPlayYouTube != null) {
            btnPlayYouTube.setOnClickListener(v -> {
                if (trailerUrl != null && !trailerUrl.isEmpty()) {
                    openYouTube(trailerUrl);
                } else if (movieId != null) {
                    fetchUrlThenOpen(movieId);
                } else {
                    Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    /**
     * YouTube app ya browser mein video open karo.
     * Jaise WhatsApp pe link click karte hain — YouTube app open hoti hai directly.
     *
     * Flow:
     * 1. vnd.youtube:VIDEO_ID → YouTube app (agar installed hai)
     * 2. Fallback → Browser mein YouTube open
     */
    private void openYouTube(String url) {
        String videoId = extractYouTubeId(url);

        if (videoId != null) {
            // YouTube app pe bhejo
            Intent appIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("vnd.youtube:" + videoId));

            try {
                startActivity(appIntent); // YouTube app open hogi
                return;
            } catch (Exception e) {
                // YouTube app nahi hai — browser fallback
            }
        }

        // Browser fallback
        try {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open trailer", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUrlThenOpen(String movieId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnPlayYouTube != null) btnPlayYouTube.setEnabled(false);

        FirebaseDatabase.getInstance().getReference("movies").child(movieId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (btnPlayYouTube != null) btnPlayYouTube.setEnabled(true);

                        String url = snap.child("trailer_url").getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            trailerUrl = url;
                            openYouTube(url);
                        } else {
                            Toast.makeText(TrailerActivity.this,
                                    "Trailer not available", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (btnPlayYouTube != null) btnPlayYouTube.setEnabled(true);
                        Toast.makeText(TrailerActivity.this,
                                "Failed to load trailer", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String extractYouTubeId(String url) {
        if (url == null) return null;

        // youtu.be/VIDEO_ID
        if (url.contains("youtu.be/")) {
            String[] p = url.split("youtu.be/");
            if (p.length > 1) {
                String id = p[1];
                if (id.contains("?")) id = id.substring(0, id.indexOf("?"));
                if (id.contains("&")) id = id.substring(0, id.indexOf("&"));
                return id.trim();
            }
        }

        // youtube.com/watch?v=VIDEO_ID
        if (url.contains("watch?v=")) {
            try {
                String id = Uri.parse(url).getQueryParameter("v");
                if (id != null && !id.isEmpty()) return id;
            } catch (Exception ignored) {}
        }

        return null;
    }
}