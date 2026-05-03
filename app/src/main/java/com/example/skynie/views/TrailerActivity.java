package com.example.skynie.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private ImageView ivThumbnail;
    private LinearLayout btnPlayYouTube;
    private FrameLayout thumbnailContainer;

    // WebView components
    private FrameLayout videoContainer;
    private WebView webView;
    private boolean isVideoPlaying = false;
    private boolean isLoading = false;
    private boolean hasAutoPlayed = false;  // To prevent multiple auto-play attempts

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

        // ✅ AUTO-PLAY: Start playing trailer as soon as activity opens
        autoPlayTrailer();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvMeta = findViewById(R.id.tvMeta);
        badgeAge = findViewById(R.id.badgeAge);
        badgeLang = findViewById(R.id.badgeLang);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        btnPlayYouTube = findViewById(R.id.btnPlayYouTube);
        videoContainer = findViewById(R.id.videoContainer);
        thumbnailContainer = findViewById(R.id.thumbnailContainer);
    }

    private void getIntentData() {
        movieId = getIntent().getStringExtra("movie_id");
        movieTitle = getIntent().getStringExtra("movie_title");
        trailerUrl = getIntent().getStringExtra("trailer_url");
        pgRating = getIntent().getStringExtra("pg_rating");
        language = getIntent().getStringExtra("language");
        moviePoster = getIntent().getStringExtra("movie_poster");
        duration = getIntent().getIntExtra("movie_duration", 0);
    }

    private void populateUI() {
        // Title
        if (tvMovieTitle != null && movieTitle != null)
            tvMovieTitle.setText(movieTitle);

        // Meta
        StringBuilder meta = new StringBuilder("NEW");
        if (pgRating != null && !pgRating.isEmpty()) meta.append(" · ").append(pgRating);
        if (duration > 0) {
            int h = duration / 60, m = duration % 60;
            meta.append(" · ").append(h).append("h ").append(m).append("m");
        }
        if (tvMeta != null) tvMeta.setText(meta.toString());

        // Language badge
        if (badgeLang != null && language != null && !language.isEmpty()) {
            String lang = language.length() >= 2
                    ? language.substring(0, 2).toUpperCase() : language.toUpperCase();
            badgeLang.setText(lang);
        }

        // Age badge
        if (badgeAge != null && pgRating != null && !pgRating.isEmpty())
            badgeAge.setText(pgRating);

        // Movie thumbnail
        if (ivThumbnail != null && moviePoster != null && !moviePoster.isEmpty()) {
            int resId = getResources().getIdentifier(moviePoster, "drawable", getPackageName());
            if (resId != 0) ivThumbnail.setImageResource(resId);
        }

        // Play button click - Manual play (as backup)
        if (btnPlayYouTube != null) {
            btnPlayYouTube.setOnClickListener(v -> {
                if (!isLoading && !hasAutoPlayed) {
                    if (trailerUrl != null && !trailerUrl.isEmpty()) {
                        showEmbeddedYouTubePlayer(trailerUrl);
                    } else if (movieId != null) {
                        fetchUrlThenShowPlayer(movieId);
                    } else {
                        Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Auto-play trailer when activity starts
     */
    private void autoPlayTrailer() {
        if (trailerUrl != null && !trailerUrl.isEmpty()) {
            showEmbeddedYouTubePlayer(trailerUrl);
        } else if (movieId != null) {
            fetchUrlThenShowPlayer(movieId);
        } else {
            Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show embedded YouTube player using WebView with CORS proxy
     */
    private void showEmbeddedYouTubePlayer(String url) {
        String videoId = extractYouTubeId(url);

        if (videoId == null) {
            Toast.makeText(this, "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
            hideLoading();
            return;
        }

        hasAutoPlayed = true;

        // Hide the play button layout completely since video will auto-play
        if (btnPlayYouTube != null) {
            btnPlayYouTube.setVisibility(View.GONE);
        }

        // Show loading
        showLoading();

        // Hide thumbnail container and show video container
        if (thumbnailContainer != null) thumbnailContainer.setVisibility(View.GONE);
        if (videoContainer != null) videoContainer.setVisibility(View.VISIBLE);

        // Create and setup WebView if not already created
        if (webView == null) {
            webView = new WebView(this);
            setupWebView();
            videoContainer.addView(webView);
        } else {
            webView.setVisibility(View.VISIBLE);
        }

        // Load video with CORS proxy - autoplay is enabled
        String embedUrl = "https://www.youtube.com/embed/" + videoId + "?autoplay=1&playsinline=1";
        String proxyUrl = "https://corsproxy.io/?url=" + Uri.encode(embedUrl);
        webView.loadUrl(proxyUrl);

        isVideoPlaying = true;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Auto-play without user gesture
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);

        // Enable hardware acceleration
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

        // Handle WebView events
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideLoading();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    hideLoading();
                }
            }
        });

        // Set layout parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        webView.setLayoutParams(params);
    }

    private void fetchUrlThenShowPlayer(String movieId) {
        showLoading();
        isLoading = true;

        FirebaseDatabase.getInstance().getReference("movies").child(movieId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        String url = snap.child("trailer_url").getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            trailerUrl = url;
                            showEmbeddedYouTubePlayer(url);
                        } else {
                            hideLoading();
                            isLoading = false;
                            Toast.makeText(TrailerActivity.this,
                                    "Trailer not available", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        hideLoading();
                        isLoading = false;
                        Toast.makeText(TrailerActivity.this,
                                "Failed to load trailer", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading() {
        isLoading = true;
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        isLoading = false;
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null && isVideoPlaying) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null && isVideoPlaying) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}