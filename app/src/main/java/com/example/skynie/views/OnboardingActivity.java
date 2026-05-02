package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.FrameLayout;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;

public class OnboardingActivity extends AppCompatActivity {

    Button btnGetStarted;
    WebView webView;
    FrameLayout videoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views
        btnGetStarted = findViewById(R.id.btn_get_started);
        videoContainer = findViewById(R.id.video_container); // You'll add this to XML
        webView = new WebView(this);

        // Setup WebView with CORS proxy
        setupYouTubeTrailer();

        // Button click listener
        btnGetStarted.setOnClickListener(v -> {
            // Clean up WebView properly before moving to next activity
            if (webView != null) {
                webView.destroy();
            }
            startActivity(new Intent(OnboardingActivity.this, AuthActivity.class));
            finish();
        });
    }

    private void setupYouTubeTrailer() {
        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Auto-play
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);

        // Enable hardware acceleration for better video playback
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

        // Handle navigation within WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Keep all navigation inside WebView
                return false;
            }
        });

        // Add WebView to container
        videoContainer.addView(webView);

        // Load YouTube trailer with CORS proxy
        // Change the video ID to any trailer you want
        String videoId = "tFMo3UJ4B4g"; // Avengers Endgame trailer (Change this)

        // Using CORS proxy to bypass YouTube restrictions
        String proxyUrl = "https://corsproxy.io/?url=https://www.youtube.com/embed/" + videoId + "?autoplay=1&loop=1&playsinline=1";

        webView.loadUrl(proxyUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video when activity is not visible
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video when activity is visible
        if (webView != null) {
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