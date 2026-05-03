package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skynie.R;

import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText emailInput;
    AppCompatButton confirmButton;
    ImageButton backButton;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

    }

    private void init() {
        emailInput = findViewById(R.id.emailInput);
        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        confirmButton.setOnClickListener(v -> {
            email = emailInput.getText().toString().trim();

            if (isValidEmail(email)) {
                Intent i=new Intent(ForgotPasswordActivity.this, VerifyOTP_Activity.class);
                i.putExtra("email",email);
            } else {
                // Email is invalid
                Toast.makeText(ForgotPasswordActivity.this,
                        "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                emailInput.setError("Invalid email format");
                emailInput.requestFocus();
            }
        });
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);

        return pattern.matcher(email).matches();
    }
}