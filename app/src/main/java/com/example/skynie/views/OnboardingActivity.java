package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;

public class OnboardingActivity extends AppCompatActivity {

    Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        btnGetStarted = findViewById(R.id.btn_get_started);

        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
        });
    }
}