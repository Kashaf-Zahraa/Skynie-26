package com.example.skynie.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skynie.R;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput;
    EditText passwordInput;
    AppCompatButton loginButton;
    AppCompatButton googleButton;
    AppCompatButton facebookButton;
    TextView forgotPasswordLink;
    TextView signupLink;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }

    private void init(){
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginButton = findViewById(R.id.loginButton);
            googleButton = findViewById(R.id.googleButton);
            facebookButton = findViewById(R.id.facebookButton);
            forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
            signupLink = findViewById(R.id.signupLink);
            backButton = findViewById(R.id.backButton);
        }
}