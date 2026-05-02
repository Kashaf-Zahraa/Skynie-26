package com.example.skynie.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
    private TextView tvTrailerNotAvailable;
    private SeekBar seekBar;

    private String resolvedTrailerUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trailer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        populateFromIntent();
        btnBack.setOnClickListener(v -> finish());
        if (seekBar != null) seekBar.setEnabled(false);
    }

    private void initViews() {
        progressBar         = findViewById(R.id.progressBar);
        btnBack             = findViewById(R.id.btnBack);
        tvMovieTitle        = findViewById(R.id.tvMovieTitle);
        tvMeta              = findViewById(R.id.tvMeta);
        badgeAge            = findViewById(R.id.badgeAge);
        badgeLang           = findViewById(R.id.badgeLang);
        seekBar             = findViewById(R.id.seekBar);
        tvTrailerNotAvailable = findViewById(R.id.tvTrailerNotAvailable);
    }

    private void populateFromIntent() {
        String movieId    = getIntent().getStringExtra("movie_id");
        String movieTitle = getIntent().getStringExtra("movie_title");
        String trailerUrl = getIntent().getStringExtra("trailer_url");
        String pgRating   = getIntent().getStringExtra("pg_rating");
        String language   = getIntent().getStringExtra("language");
        int    duration   = getIntent().getIntExtra("movie_duration", 0);
        String genre      = getIntent().getStringExtra("movie_genre");

        if (tvMovieTitle != null && movieTitle != null) tvMovieTitle.setText(movieTitle);

        StringBuilder meta = new StringBuilder("NEW");
        if (genre    != null && !genre.isEmpty())    meta.append(" · ").append(genre);
        if (pgRating != null && !pgRating.isEmpty()) meta.append(" · ").append(pgRating);
        if (duration > 0) {
            int h = duration / 60, m = duration % 60;
            meta.append(" · ").append(h).append("h ").append(m).append("m");
        }
        if (tvMeta != null) tvMeta.setText(meta.toString());

        if (badgeLang != null && language != null && !language.isEmpty()) {
            String lang = language.length() >= 2
                    ? language.substring(0, 2).toUpperCase() : language.toUpperCase();
            badgeLang.setText(lang);
        }

        if (badgeAge != null && pgRating != null && !pgRating.isEmpty())
            badgeAge.setText(pgRating);

        if (trailerUrl != null && !trailerUrl.isEmpty()) {
            resolvedTrailerUrl = trailerUrl;
            openTrailer(trailerUrl);
        } else if (movieId != null) {
            fetchAndOpenTrailer(movieId);
        } else {
            showNotAvailable();
        }
    }

    private void fetchAndOpenTrailer(String movieId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("movies").child(movieId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        String url = snap.child("trailer_url").getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            resolvedTrailerUrl = url;
                            openTrailer(url);
                        } else {
                            showNotAvailable();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        showNotAvailable();
                    }
                });
    }

    /**
     * ✅ FIXED: Seedha browser mein open karo - No WebView, No Error 153
     */
    private void openTrailer(String url) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);

        String videoId = extractYouTubeId(url);
        String openUrl = (videoId != null)
                ? "https://www.youtube.com/watch?v=" + videoId
                : url;

        if (openUrl == null || openUrl.isEmpty()) {
            showNotAvailable();
            return;
        }

        try {
            // ✅ Seedha browser/YouTube mein open karo
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(openUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Activity mein message dikhao
            showOpenedExternally(openUrl);

        } catch (Exception e) {
            showNotAvailable();
        }
    }

    private void showOpenedExternally(final String url) {
        View videoContainer = findViewById(R.id.videoContainer);
        if (videoContainer != null) {
            videoContainer.setBackgroundColor(0xFF1A1A1A);
        }

        if (tvTrailerNotAvailable != null) {
            tvTrailerNotAvailable.setText("▶  Tap to watch trailer");
            tvTrailerNotAvailable.setTextSize(16f);
            tvTrailerNotAvailable.setTextColor(0xFFE53935);
            tvTrailerNotAvailable.setVisibility(View.VISIBLE);
            tvTrailerNotAvailable.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception ignored) {}
            });
        }
    }

    private void showNotAvailable() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (tvTrailerNotAvailable != null) {
            tvTrailerNotAvailable.setText("Trailer not available");
            tvTrailerNotAvailable.setTextSize(14f);
            tvTrailerNotAvailable.setTextColor(0xFFFFFFFF);
            tvTrailerNotAvailable.setVisibility(View.VISIBLE);
            tvTrailerNotAvailable.setOnClickListener(null);
        }
        Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
    }

    private String extractYouTubeId(String url) {
        if (url == null) return null;

        // Handle youtu.be format
        if (url.contains("youtu.be/")) {
            String[] p = url.split("youtu.be/");
            if (p.length > 1) {
                String id = p[1];
                if (id.contains("?")) id = id.substring(0, id.indexOf("?"));
                if (id.contains("&")) id = id.substring(0, id.indexOf("&"));
                return id.trim();
            }
        }

        // Handle youtube.com/watch?v= format
        if (url.contains("watch?v=")) {
            String id = Uri.parse(url).getQueryParameter("v");
            if (id != null) return id;
        }

        // Handle youtube.com/embed/ format
        if (url.contains("embed/")) {
            String[] p = url.split("embed/");
            if (p.length > 1) {
                String id = p[1];
                if (id.contains("?")) id = id.substring(0, id.indexOf("?"));
                if (id.contains("&")) id = id.substring(0, id.indexOf("&"));
                return id.trim();
            }
        }

        // Handle shorts format
        if (url.contains("/shorts/")) {
            String[] p = url.split("/shorts/");
            if (p.length > 1) {
                String id = p[1];
                if (id.contains("?")) id = id.substring(0, id.indexOf("?"));
                return id.trim();
            }
        }

        return null;
    }
}