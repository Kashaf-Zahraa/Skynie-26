package com.example.skynie.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skynie.R;

public class VerifyOTP_Activity extends AppCompatActivity {
    EditText[] otpFields = new EditText[4];
    Button confirmButton;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }
    private void init() {

        otpFields[0] = findViewById(R.id.otpField1);
        otpFields[1] = findViewById(R.id.otpField2);
        otpFields[2] = findViewById(R.id.otpField3);
        otpFields[3] = findViewById(R.id.otpField4);

        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.backButton);
    }
}