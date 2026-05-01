package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;

public class SplashActivity extends AppCompatActivity {

    ImageView ivLogo;
    TextView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivLogo    = findViewById(R.id.iv_logo);
        tvAppName = findViewById(R.id.tv_app_name);

        startSplashAnimation();
    }

    private void startSplashAnimation() {

        // ye logo ko drop kr rhi hay oope rse nichay
        Animation dropAnim = AnimationUtils.loadAnimation(this, R.anim.logo_drop);

        dropAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a) {}
            @Override public void onAnimationRepeat(Animation a) {}

            @Override
            public void onAnimationEnd(Animation a) {

                // ye logo ko rotate kr rhi hay
                Animation rotateAnim = AnimationUtils.loadAnimation(
                        SplashActivity.this, R.anim.logo_rot);

                rotateAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation a) {}
                    @Override public void onAnimationRepeat(Animation a) {}

                    @Override
                    public void onAnimationEnd(Animation a) {

                        // yahan fading ho rhi hay
                        Animation fadeAnim = AnimationUtils.loadAnimation(
                                SplashActivity.this, R.anim.fadeoftext);

                        tvAppName.setVisibility(View.VISIBLE);
                        tvAppName.startAnimation(fadeAnim);

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            startActivity(new Intent(
                                    SplashActivity.this, OnboardingActivity.class));
                            finish();
                        }, 1200);
                    }
                });

                ivLogo.startAnimation(rotateAnim);
            }
        });

        ivLogo.startAnimation(dropAnim);
    }
}