package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;
import com.google.firebase.auth.FirebaseAuth;

public class OnboardingActivity extends AppCompatActivity {

    Button btnGetStarted;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        mAuth = FirebaseAuth.getInstance();

        btnGetStarted = findViewById(R.id.btn_get_started);

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // User is already logged in - skip onboarding and go directly to MainActivity
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // User is not logged in - show onboarding and then go to AuthActivity
            btnGetStarted.setOnClickListener(v -> {
                startActivity(new Intent(OnboardingActivity.this, AuthActivity.class));
                finish();
            });
        }
    }
}