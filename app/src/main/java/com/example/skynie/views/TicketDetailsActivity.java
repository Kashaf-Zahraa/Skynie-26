package com.example.skynie.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketDetailsActivity extends AppCompatActivity {

    private TextView tvMovieTitle, tvAmount, tvCost, tvHall, tvSeats, tvDate, tvTime, tvFormat;
    private ImageView ivQRCode, ivMoviePoster;
    private LinearLayout btnSendTicket;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);

        initViews();
        displayFromIntent();

        btnBack.setOnClickListener(v -> finish());

        // ✅ FIXED: Send ticket via email with pre-filled To field
        btnSendTicket.setOnClickListener(v -> sendTicketByEmail());
    }

    private void initViews() {
        tvMovieTitle  = findViewById(R.id.tvMovieTitle);
        tvAmount      = findViewById(R.id.tvAmount);
        tvCost        = findViewById(R.id.tvCost);
        tvHall        = findViewById(R.id.tvHall);
        tvSeats       = findViewById(R.id.tvSeats);
        tvDate        = findViewById(R.id.tvDate);
        tvTime        = findViewById(R.id.tvTime);
        tvFormat      = findViewById(R.id.tvFormat);
        ivQRCode      = findViewById(R.id.ivQRCode);
        ivMoviePoster = findViewById(R.id.ivMoviePoster);
        btnSendTicket = findViewById(R.id.btnSendTicket);
        btnBack       = findViewById(R.id.btnBack);
    }

    @SuppressWarnings("unchecked")
    private void displayFromIntent() {
        // All data comes from OrderDetailsActivity — no Firebase fetch needed here
        String bookingId   = getIntent().getStringExtra("booking_id");
        String bookingRef  = getIntent().getStringExtra("booking_ref");
        String movieTitle  = getIntent().getStringExtra("movie_title");
        String cinemaName  = getIntent().getStringExtra("cinema_name");
        String hallNumber  = getIntent().getStringExtra("hall_number");
        String screenType  = getIntent().getStringExtra("screen_type");
        String audioFormat = getIntent().getStringExtra("audio_format");
        String time        = getIntent().getStringExtra("time");
        String showtimeDate= getIntent().getStringExtra("showtime_date");
        double totalPrice  = getIntent().getDoubleExtra("total_price", 0.0);
        int seatCount      = getIntent().getIntExtra("seat_count", 0);
        String seatsLabel  = getIntent().getStringExtra("seats_label");
        String moviePoster = getIntent().getStringExtra("movie_poster");

        // Store for email
        storeEmailData(bookingRef, movieTitle, cinemaName, hallNumber, screenType,
                audioFormat, time, showtimeDate, totalPrice, seatCount, seatsLabel);

        // Movie title
        if (movieTitle != null) tvMovieTitle.setText(movieTitle);

        // Format badge: screenType + audioFormat
        String formatStr = "";
        if (screenType != null && !screenType.isEmpty()) formatStr = screenType;
        if (audioFormat != null && !audioFormat.isEmpty()) {
            formatStr = formatStr.isEmpty() ? audioFormat : formatStr + " " + audioFormat;
        }
        if (tvFormat != null) {
            tvFormat.setText(formatStr);
            tvFormat.setVisibility(formatStr.isEmpty() ? View.GONE : View.VISIBLE);
        }

        // Amount: "3 Tickets" etc.
        tvAmount.setText(seatCount + (seatCount == 1 ? " Ticket" : " Tickets"));

        // Cost
        tvCost.setText(String.format(Locale.getDefault(), "%.2f USD", totalPrice));

        // Hall
        tvHall.setText(hallNumber != null ? hallNumber : "");

        // Seats label: "A5, A6, B3"
        tvSeats.setText(seatsLabel != null ? seatsLabel : "");

        // Date
        if (showtimeDate != null && !showtimeDate.isEmpty()) {
            try {
                SimpleDateFormat inFmt  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outFmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date d = inFmt.parse(showtimeDate);
                tvDate.setText(d != null ? outFmt.format(d) : showtimeDate);
            } catch (Exception e) {
                tvDate.setText(showtimeDate);
            }
        } else {
            tvDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
        }

        // Time
        tvTime.setText(time != null ? time : "");

        // Movie poster
        if (moviePoster != null && !moviePoster.isEmpty()) {
            int resId = getResources().getIdentifier(moviePoster, "drawable", getPackageName());
            if (resId != 0) ivMoviePoster.setImageResource(resId);
        }

        // QR code — encode booking reference
        String qrData = bookingRef != null ? bookingRef : (bookingId != null ? bookingId : "SKYNIE");
        generateQRCode(qrData);
    }

    // Store data for email
    private String emailBookingRef, emailMovieTitle, emailCinemaName, emailHallNumber;
    private String emailScreenType, emailAudioFormat, emailTime, emailDate, emailSeatsLabel;
    private double emailTotalPrice;
    private int emailSeatCount;

    private void storeEmailData(String bookingRef, String movieTitle, String cinemaName,
                                String hallNumber, String screenType, String audioFormat,
                                String time, String date, double totalPrice,
                                int seatCount, String seatsLabel) {
        this.emailBookingRef = bookingRef;
        this.emailMovieTitle = movieTitle;
        this.emailCinemaName = cinemaName;
        this.emailHallNumber = hallNumber;
        this.emailScreenType = screenType;
        this.emailAudioFormat = audioFormat;
        this.emailTime = time;
        this.emailDate = date;
        this.emailTotalPrice = totalPrice;
        this.emailSeatCount = seatCount;
        this.emailSeatsLabel = seatsLabel;
    }

    private void generateQRCode(String text) {
        try {
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(text, BarcodeFormat.QR_CODE, 500, 500);
            int w = matrix.getWidth(), h = matrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            for (int x = 0; x < w; x++)
                for (int y = 0; y < h; y++)
                    bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            ivQRCode.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ FIXED: Send ticket via email with pre-filled To field
    private void sendTicketByEmail() {
        // Firebase Auth se current user ki email lo
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No email found for account", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email subject
        String subject = "Your Skynie Ticket - " + safe(emailMovieTitle);

        // Email body
        String body = "Hi!\n\n"
                + "Your booking is confirmed. Details below:\n\n"
                + "Movie:    " + safe(emailMovieTitle) + "\n"
                + "Format:   " + safe(emailScreenType)
                + (emailAudioFormat != null && !emailAudioFormat.isEmpty() ? " - " + emailAudioFormat : "") + "\n"
                + "Theater:  " + safe(emailCinemaName) + "\n"
                + "Hall:     " + safe(emailHallNumber) + "\n"
                + "Date:     " + safe(emailDate) + "\n"
                + "Time:     " + safe(emailTime) + "\n"
                + "Seats:    " + safe(emailSeatsLabel)
                + " (" + emailSeatCount + " ticket" + (emailSeatCount != 1 ? "s" : "") + ")\n"
                + "Total:    " + String.format(Locale.getDefault(), "%.2f USD", emailTotalPrice) + "\n\n"
                + "Booking Ref: " + safe(emailBookingRef) + "\n\n"
                + "Enjoy your movie!\n- Skynie";

        // ✅ Gmail mein directly To: user ki apni email, ready to send
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(emailIntent);
            Toast.makeText(this, "Sending to " + userEmail, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Gmail nahi hai — generic chooser
            try {
                Intent fallback = new Intent(Intent.ACTION_SEND);
                fallback.setType("text/plain");
                fallback.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
                fallback.putExtra(Intent.EXTRA_SUBJECT, subject);
                fallback.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(fallback, "Send ticket via..."));
            } catch (Exception e2) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to handle null values
    private String safe(String val) {
        return val != null ? val : "";
    }
}