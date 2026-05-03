package com.example.skynie.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skynie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketDetailsActivity extends AppCompatActivity {

    private TextView tvMovieTitle, tvAmount, tvCost, tvHall, tvSeats, tvDate, tvTime, tvFormat, tvCinemaName;
    private ImageView ivQRCode, ivMoviePoster;
    private LinearLayout btnSendTicket;
    private ImageButton btnBack;

    // Store all data for email
    private String currentMovieTitle, currentCinemaName, currentHallNumber;
    private String currentScreenType, currentAudioFormat, currentTime, currentDate;
    private double currentTotalPrice;
    private int currentSeatCount;
    private String currentSeatsLabel, currentBookingRef, currentBookingId;
    private String currentMoviePoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);

        initViews();

        btnBack.setOnClickListener(v -> finish());

        // ✅ FIXED: Send email button ab kaam karega
        btnSendTicket.setOnClickListener(v -> sendTicketByEmail());

        String movieTitle = getIntent().getStringExtra("movie_title");
        String bookingId  = getIntent().getStringExtra("booking_id");

        if (movieTitle != null && !movieTitle.isEmpty()) {
            displayFromIntent();
        } else if (bookingId != null) {
            fetchAndDisplay(bookingId);
        }
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
        tvCinemaName  = findViewById(R.id.tvMovieBadge);
    }

    private void displayFromIntent() {
        currentBookingRef   = getIntent().getStringExtra("booking_ref");
        currentBookingId    = getIntent().getStringExtra("booking_id");
        currentMovieTitle   = getIntent().getStringExtra("movie_title");
        currentCinemaName   = getIntent().getStringExtra("cinema_name");
        currentHallNumber   = getIntent().getStringExtra("hall_number");
        currentScreenType   = getIntent().getStringExtra("screen_type");
        currentAudioFormat  = getIntent().getStringExtra("audio_format");
        currentTime         = getIntent().getStringExtra("time");
        currentDate         = getIntent().getStringExtra("showtime_date");
        currentTotalPrice   = getIntent().getDoubleExtra("total_price", 0.0);
        currentSeatCount    = getIntent().getIntExtra("seat_count", 0);
        currentSeatsLabel   = getIntent().getStringExtra("seats_label");
        currentMoviePoster  = getIntent().getStringExtra("movie_poster");
        String qrData       = currentBookingRef != null ? currentBookingRef : currentBookingId;

        populateUI(currentMovieTitle, currentCinemaName, currentHallNumber, currentScreenType,
                currentAudioFormat, currentTime, currentDate, currentTotalPrice, currentSeatCount,
                currentSeatsLabel, currentMoviePoster, qrData);
    }

    private void fetchAndDisplay(String bookingId) {
        currentBookingId = bookingId;
        if (tvMovieTitle != null) tvMovieTitle.setText("Loading...");

        FirebaseDatabase.getInstance().getReference("bookings").child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists()) return;

                        String showtimeId  = snap.child("showtime_id").getValue(String.class);
                        currentBookingRef = snap.child("booking_reference").getValue(String.class);
                        String userId     = snap.child("user_id").getValue(String.class);
                        Double tp         = snap.child("total_price").getValue(Double.class);
                        currentTotalPrice = tp != null ? tp : 0.0;
                        String qrData     = currentBookingRef != null ? currentBookingRef : bookingId;

                        if (showtimeId == null) return;

                        FirebaseDatabase.getInstance().getReference("showtimes").child(showtimeId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot stSnap) {
                                        String movieId  = stSnap.child("movieId").getValue(String.class);
                                        String cinemaId = stSnap.child("cinemaId").getValue(String.class);
                                        String hallId   = stSnap.child("hallId").getValue(String.class);
                                        currentTime    = stSnap.child("time").getValue(String.class);
                                        currentDate    = stSnap.child("date").getValue(String.class);

                                        if (movieId != null)
                                            fetchMovieAndBuild(movieId, cinemaId, hallId,
                                                    showtimeId, userId, currentTime, currentDate,
                                                    currentTotalPrice, qrData);
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                                });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void fetchMovieAndBuild(String movieId, String cinemaId, String hallId,
                                    String showtimeId, String userId, String time,
                                    String date, double totalPrice, String qrData) {
        FirebaseDatabase.getInstance().getReference("movies").child(movieId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot mv) {
                        currentMovieTitle = mv.child("title").getValue(String.class);
                        currentMoviePoster = mv.child("poster_drawable").getValue(String.class);

                        FirebaseDatabase.getInstance().getReference("cinemas").child(cinemaId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot ci) {
                                        currentCinemaName = ci.child("name").getValue(String.class);

                                        FirebaseDatabase.getInstance().getReference("halls").child(hallId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot hl) {
                                                        currentHallNumber = hl.child("hallNumber").getValue(String.class);
                                                        currentScreenType = hl.child("screenType").getValue(String.class);

                                                        countSeatsAndBuild(showtimeId, userId, hallId,
                                                                currentMovieTitle, currentCinemaName, currentHallNumber,
                                                                currentScreenType, null, time, date,
                                                                totalPrice, currentMoviePoster, qrData);
                                                    }
                                                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                                                });
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                                });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void countSeatsAndBuild(String showtimeId, String userId, String hallId,
                                    String movieTitle, String cinemaName, String hallNumber,
                                    String screenType, String audioFormat, String time,
                                    String date, double totalPrice, String poster, String qrData) {
        FirebaseDatabase.getInstance().getReference("hallShowtimes")
                .orderByChild("showtimeId").equalTo(showtimeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String hstId = null;
                        String audio = audioFormat;

                        for (DataSnapshot hst : snapshot.getChildren()) {
                            String hid = hst.child("hallId").getValue(String.class);
                            if (hallId != null && hallId.equals(hid)) {
                                hstId = hst.getKey();
                                if (audio == null)
                                    audio = hst.child("audioFormat").getValue(String.class);
                                break;
                            }
                        }
                        if (hstId == null && snapshot.getChildrenCount() > 0) {
                            DataSnapshot first = snapshot.getChildren().iterator().next();
                            hstId = first.getKey();
                            if (audio == null) audio = first.child("audioFormat").getValue(String.class);
                        }

                        final String finalHst  = hstId;
                        final String finalAudio = audio;
                        currentAudioFormat = finalAudio;

                        if (finalHst == null) {
                            populateUI(movieTitle, cinemaName, hallNumber, screenType,
                                    finalAudio, time, date, totalPrice, 0, "", poster, qrData);
                            return;
                        }

                        FirebaseDatabase.getInstance().getReference("seats")
                                .orderByChild("hallShowtimeId").equalTo(finalHst)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot seatsSnap) {
                                        int count = 0;
                                        StringBuilder sb = new StringBuilder();
                                        for (DataSnapshot s : seatsSnap.getChildren()) {
                                            String uid = s.child("userId").getValue(String.class);
                                            if (userId != null && userId.equals(uid)) {
                                                count++;
                                                String row = s.child("row").getValue(String.class);
                                                String num = s.child("seatNumber").getValue(String.class);
                                                if (sb.length() > 0) sb.append(", ");
                                                if (row != null && num != null) sb.append(row).append(num);
                                            }
                                        }
                                        currentSeatCount = count;
                                        currentSeatsLabel = sb.toString();
                                        populateUI(movieTitle, cinemaName, hallNumber, screenType,
                                                finalAudio, time, date, totalPrice, count,
                                                sb.toString(), poster, qrData);
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                                });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void populateUI(String movieTitle, String cinemaName, String hallNumber,
                            String screenType, String audioFormat, String time,
                            String showtimeDate, double totalPrice, int seatCount,
                            String seatsLabel, String moviePoster, String qrData) {
        runOnUiThread(() -> {
            if (movieTitle != null) tvMovieTitle.setText(movieTitle);
            if (tvCinemaName != null && cinemaName != null)
                tvCinemaName.setText("★ " + cinemaName);

            String fmt = "";
            if (screenType  != null && !screenType.isEmpty())  fmt = screenType;
            if (audioFormat != null && !audioFormat.isEmpty())
                fmt = fmt.isEmpty() ? audioFormat : fmt + " " + audioFormat;
            if (tvFormat != null) {
                tvFormat.setText(fmt);
                tvFormat.setVisibility(fmt.isEmpty() ? View.GONE : View.VISIBLE);
            }

            tvAmount.setText(seatCount + (seatCount == 1 ? " Ticket" : " Tickets"));
            tvCost.setText(String.format(Locale.getDefault(), "%.2f USD", totalPrice));
            if (tvHall  != null) tvHall.setText(hallNumber  != null ? hallNumber  : "");
            if (tvSeats != null) tvSeats.setText(seatsLabel != null ? seatsLabel : "");

            // ✅ DATE FORMAT FOR DISPLAY (dd.MM.yyyy)
            if (showtimeDate != null && !showtimeDate.isEmpty()) {
                try {
                    SimpleDateFormat inF  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outF = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    Date d = inF.parse(showtimeDate);
                    tvDate.setText(d != null ? outF.format(d) : showtimeDate);
                } catch (Exception e) { tvDate.setText(showtimeDate); }
            } else {
                tvDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
            }

            if (tvTime != null) tvTime.setText(time != null ? time : "");

            if (moviePoster != null && !moviePoster.isEmpty()) {
                int resId = getResources().getIdentifier(moviePoster, "drawable", getPackageName());
                if (resId != 0) ivMoviePoster.setImageResource(resId);
            }

            generateQRCode(qrData != null ? qrData : "SKYNIE");
        });
    }

    private void generateQRCode(String text) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 500, 500);
            int w = matrix.getWidth(), h = matrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            for (int x = 0; x < w; x++)
                for (int y = 0; y < h; y++)
                    bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            ivQRCode.setImageBitmap(bmp);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ EMAIL FUNCTION WITH DATE FIX (exactly as you said)
    // ─────────────────────────────────────────────────────────────
    // Helper method - class ke kisi bhi method ke bahar (class level) rakho
    private String safe(String s) {
        return (s != null && !s.isEmpty()) ? s : "—";
    }

    // Then sendTicketByEmail() mein directly use karo
    private void sendTicketByEmail() {
        // Date formatting
        String formattedDate = "";
        if (currentDate != null && !currentDate.isEmpty()) {
            try {
                SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                Date d = inFmt.parse(currentDate);
                formattedDate = (d != null) ? outFmt.format(d) : currentDate;
            } catch (Exception e) {
                formattedDate = currentDate;
            }
        } else {
            formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        }

        // Email body using safe() method
        String body = "Hi!\n\n"
                + "Your booking is confirmed:\n\n"
                + "Movie:    " + safe(currentMovieTitle) + "\n"
                + "Format:   " + safe(currentScreenType) + (currentAudioFormat != null && !currentAudioFormat.isEmpty() ? " - " + currentAudioFormat : "") + "\n"
                + "Theater:  " + safe(currentCinemaName) + "\n"
                + "Hall:     " + safe(currentHallNumber) + "\n"
                + "Date:     " + formattedDate + "\n"
                + "Time:     " + safe(currentTime) + "\n"
                + "Seats:    " + safe(currentSeatsLabel) + " (" + currentSeatCount + " ticket" + (currentSeatCount != 1 ? "s" : "") + ")\n"
                + "Total:    " + String.format(Locale.getDefault(), "%.2f USD", currentTotalPrice) + "\n\n"
                + "Booking Ref: " + safe(currentBookingRef != null ? currentBookingRef : currentBookingId) + "\n\n"
                + "Enjoy your movie!\n- Skynie";

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your Ticket Confirmation - " + safe(currentMovieTitle));
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
        }
    }
}