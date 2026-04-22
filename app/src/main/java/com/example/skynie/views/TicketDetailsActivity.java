package com.example.skynie.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;
import com.example.skynie.models.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketDetailsActivity extends AppCompatActivity {

    private TextView tvMovieTitle, tvAmount, tvCost, tvHall, tvSeats, tvDate, tvTime;
    private ImageView ivQRCode, ivMoviePoster;
    private LinearLayout btnSendTicket;
    private ImageButton btnBack;

    private DatabaseReference bookingsRef;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);

        initViews();
        bookingId = getIntent().getStringExtra("booking_id");
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");

        loadTicketDetails();

        btnBack.setOnClickListener(v -> finish());
        btnSendTicket.setOnClickListener(v -> {
            // Implement share/send functionality
        });
    }

    private void initViews() {
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvAmount = findViewById(R.id.tvAmount);
        tvCost = findViewById(R.id.tvCost);
        tvHall = findViewById(R.id.tvHall);
        tvSeats = findViewById(R.id.tvSeats);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        ivQRCode = findViewById(R.id.ivQRCode);
        ivMoviePoster = findViewById(R.id.ivMoviePoster);
        btnSendTicket = findViewById(R.id.btnSendTicket);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadTicketDetails() {
        bookingsRef.child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Booking booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    displayTicketDetails(booking);
                    generateQRCode(booking.booking_reference);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void displayTicketDetails(Booking booking) {
        // Fetch related data (movie, showtime, seats) from Firebase
        // For now using placeholder
        tvMovieTitle.setText("Oppenheimer");
        tvAmount.setText("3 Persind");
        tvCost.setText(booking.total_price + " USD");
        tvHall.setText("4th");
        tvSeats.setText("Row E (5,6,7)");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        tvDate.setText(dateFormat.format(new Date(booking.booking_date)));
        tvTime.setText(timeFormat.format(new Date(booking.booking_date)));
    }

    private void generateQRCode(String text) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 500, 500);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            ivQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}