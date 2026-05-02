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
        videoContainer = findViewById(R.id.videoContainer);
        thumbnailContainer = findViewById(R.id.thumbnailContainer);
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

        // Play button click → Show embedded YouTube player
        if (btnPlayYouTube != null) {
            btnPlayYouTube.setOnClickListener(v -> {
                if (trailerUrl != null && !trailerUrl.isEmpty()) {
                    showEmbeddedYouTubePlayer(trailerUrl);
                } else if (movieId != null) {
                    fetchUrlThenShowPlayer(movieId);
                } else {
                    Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    /**
     * Show embedded YouTube player using WebView with CORS proxy
     */
    private void showEmbeddedYouTubePlayer(String url) {
        String videoId = extractYouTubeId(url);

        if (videoId == null) {
            Toast.makeText(this, "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide thumbnail container and show video container
        if (thumbnailContainer != null) thumbnailContainer.setVisibility(View.GONE);
        if (videoContainer != null) videoContainer.setVisibility(View.VISIBLE);

        // Create and setup WebView if not already created
        if (webView == null) {
            webView = new WebView(this);
            setupWebView();
            videoContainer.addView(webView);
        }

        // Load video with CORS proxy
        String proxyUrl = "https://corsproxy.io/?url=https://www.youtube.com/embed/" + videoId + "?autoplay=1&playsinline=1";
        webView.loadUrl(proxyUrl);

        isVideoPlaying = true;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Auto-play
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);

        // Enable hardware acceleration
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

        // Handle WebView events
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Keep all navigation inside WebView
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        // Set layout parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        webView.setLayoutParams(params);
    }

    private void fetchUrlThenShowPlayer(String movieId) {
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
                            showEmbeddedYouTubePlayer(url);
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

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video when activity is not visible
        if (webView != null && isVideoPlaying) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video when activity is visible
        if (webView != null && isVideoPlaying) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up WebView to prevent memory leaks
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}