package com.example.skynie.views;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    private WebView webViewTrailer;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private TextView tvMovieTitle, tvMeta, badgeAge, badgeLang;
    private SeekBar seekBar;

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
        if (seekBar != null) seekBar.setEnabled(false); // display only
    }

    private void initViews() {
        webViewTrailer = findViewById(R.id.webViewTrailer);
        progressBar    = findViewById(R.id.progressBar);
        btnBack        = findViewById(R.id.btnBack);
        tvMovieTitle   = findViewById(R.id.tvMovieTitle);
        tvMeta         = findViewById(R.id.tvMeta);
        badgeAge       = findViewById(R.id.badgeAge);
        badgeLang      = findViewById(R.id.badgeLang);
        seekBar        = findViewById(R.id.seekBar);
    }

    private void populateFromIntent() {
        String movieId    = getIntent().getStringExtra("movie_id");
        String movieTitle = getIntent().getStringExtra("movie_title");
        String trailerUrl = getIntent().getStringExtra("trailer_url");
        String pgRating   = getIntent().getStringExtra("pg_rating");
        String language   = getIntent().getStringExtra("language");
        int    duration   = getIntent().getIntExtra("movie_duration", 0);
        String genre      = getIntent().getStringExtra("movie_genre");

        // Title
        if (tvMovieTitle != null && movieTitle != null) tvMovieTitle.setText(movieTitle);

        // Meta: "NEW · Mystery · PG-13 · 1h 56m"
        StringBuilder meta = new StringBuilder("NEW");
        if (genre    != null && !genre.isEmpty())    meta.append(" · ").append(genre);
        if (pgRating != null && !pgRating.isEmpty()) meta.append(" · ").append(pgRating);
        if (duration > 0) {
            int h = duration / 60, m = duration % 60;
            meta.append(" · ").append(h).append("h ").append(m).append("m");
        }
        if (tvMeta != null) tvMeta.setText(meta.toString());

        // Language badge (EN)
        if (badgeLang != null && language != null && !language.isEmpty()) {
            String lang = language.length() >= 2
                    ? language.substring(0, 2).toUpperCase() : language.toUpperCase();
            badgeLang.setText(lang);
        }

        // Age badge (PG-13 → show as-is)
        if (badgeAge != null && pgRating != null && !pgRating.isEmpty())
            badgeAge.setText(pgRating);

        // Load trailer
        if (trailerUrl != null && !trailerUrl.isEmpty()) {
            playTrailer(trailerUrl);
        } else if (movieId != null) {
            fetchAndPlayTrailer(movieId);
        } else {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
    }

    private void fetchAndPlayTrailer(String movieId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("movies").child(movieId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String url = snap.child("trailer_url").getValue(String.class);
                        if (url != null && !url.isEmpty()) playTrailer(url);
                        else if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void playTrailer(String url) {
        String videoId = extractYouTubeId(url);
        String loadUrl = (videoId != null)
                ? "https://www.youtube.com/embed/" + videoId + "?autoplay=1&controls=1&rel=0"
                : url;

        WebSettings s = webViewTrailer.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        webViewTrailer.setWebChromeClient(new WebChromeClient());
        webViewTrailer.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String u) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
        webViewTrailer.loadUrl(loadUrl);
    }

    private String extractYouTubeId(String url) {
        if (url == null) return null;
        if (url.contains("youtu.be/")) {
            String[] p = url.split("youtu.be/");
            if (p.length > 1) { String id = p[1]; if (id.contains("?")) id = id.substring(0, id.indexOf("?")); return id.trim(); }
        }
        if (url.contains("watch?v=")) { String id = Uri.parse(url).getQueryParameter("v"); if (id != null) return id; }
        if (url.contains("embed/")) {
            String[] p = url.split("embed/");
            if (p.length > 1) { String id = p[1]; if (id.contains("?")) id = id.substring(0, id.indexOf("?")); return id.trim(); }
        }
        return null;
    }

    @Override protected void onPause()   { super.onPause();   if (webViewTrailer != null) webViewTrailer.onPause(); }
    @Override protected void onResume()  { super.onResume();  if (webViewTrailer != null) webViewTrailer.onResume(); }
    @Override protected void onDestroy() {
        if (webViewTrailer != null) { webViewTrailer.stopLoading(); webViewTrailer.destroy(); }
        super.onDestroy();
    }
}