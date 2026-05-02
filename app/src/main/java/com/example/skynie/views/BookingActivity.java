package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skynie.R;
import com.example.skynie.adapters.DateAdapter;
import com.example.skynie.adapters.FormatAdapter;
import com.example.skynie.adapters.HallShowTimeAdapter;
import com.example.skynie.models.Cinema;
import com.example.skynie.models.Hall;
import com.example.skynie.models.HallShowTime;
import com.example.skynie.models.Showtime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BookingActivity extends AppCompatActivity {
    CoordinatorLayout main;
    ImageButton btnBack;
    RecyclerView rvDays, rvFormats, rvShowTimes;
    HallShowTimeAdapter adapter;
    DateAdapter dateAdapter;
    FormatAdapter formatAdapter;
    ImageView ivMoviePoster, ivLocationArrow;
    TextView tvMovieTitle, tvDuration;
    AppCompatButton btnTrailer;
    // Movie data from Intent
    String movieId, movieTitle, moviePoster, movieBackdrop, movieRating, movieDuration, movieDescription;
    Cinema currentCinema;
    List<HallShowTime> hallShowTimes = new ArrayList<>();
    List<Hall> halls = new ArrayList<>();
    List<Showtime> showtimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        getIntentData();
        setupMovieInfo();
        loadDataFromFirebase();
        setupClickListeners();
    }

    private void init() {
        main = findViewById(R.id.main);
        btnBack = findViewById(R.id.btn_back);
        rvDays = findViewById(R.id.rv_days);
        rvFormats = findViewById(R.id.rv_formats);
        rvShowTimes = findViewById(R.id.rv_show_times);
        ivMoviePoster = findViewById(R.id.iv_movie_poster);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvDuration = findViewById(R.id.tv_duration);
        btnTrailer = findViewById(R.id.btn_trailer);
        ivLocationArrow = findViewById(R.id.iv_location_arrow);

        // Showtimes RecyclerView
        rvShowTimes.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new HallShowTimeAdapter(hallShowTimes, this);
        rvShowTimes.setAdapter(adapter);

        // Date Adapter
        rvDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateAdapter = new DateAdapter(this);
        dateAdapter.setOnDateChangeListener(() -> {
            filterShowtimesByDateAndFormat();
        });
        rvDays.setAdapter(dateAdapter);

        // Format Adapter - WITH LISTENER
        rvFormats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        formatAdapter = new FormatAdapter(this, (format, position) -> {
            filterShowtimesByDateAndFormat();
        });
        rvFormats.setAdapter(formatAdapter);
    }
    private void getIntentData() {
        Intent intent = getIntent();
        movieId = intent.getStringExtra("movie_id");
        movieTitle = intent.getStringExtra("movie_title");
        moviePoster = intent.getStringExtra("movie_poster");
        movieBackdrop = intent.getStringExtra("movie_backdrop");
        movieRating = intent.getStringExtra("movie_rating");
        movieDuration = String.valueOf(intent.getIntExtra("movie_duration", 0));
        movieDescription = intent.getStringExtra("movie_description");
    }

    private void setupMovieInfo() {
        if (movieTitle != null) {
            tvMovieTitle.setText(movieTitle);
        }
        if (movieDuration != null && !movieDuration.isEmpty()) {
            tvDuration.setText(movieDuration + " min");
        }

        if (moviePoster != null && !moviePoster.isEmpty()) {
            int resourceId = getResources().getIdentifier(moviePoster, "drawable", getPackageName());
            if (resourceId != 0) {
                Glide.with(this)
                        .load(resourceId)
                        .placeholder(R.drawable.ic_movie_placeholder)
                        .error(R.drawable.ic_movie_placeholder)
                        .into(ivMoviePoster);
            } else {
                ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder);
            }
        }
    }

    private void loadDataFromFirebase() {
        Toast.makeText(this, "Loading showtimes...", Toast.LENGTH_SHORT).show();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child("cinemas").child("c1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cinemaSnapshot) {
                if (cinemaSnapshot.exists()) {
                    List<String> hallShowtimeIds = new ArrayList<>();
                    DataSnapshot hallShowtimeIdsSnapshot = cinemaSnapshot.child("hallShowtimeIds");
                    for (DataSnapshot idSnapshot : hallShowtimeIdsSnapshot.getChildren()) {
                        hallShowtimeIds.add(idSnapshot.getValue(String.class));
                    }

                    if (hallShowtimeIds.isEmpty()) {
                        Toast.makeText(BookingActivity.this, "No showtimes available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    fetchHallShowtimes(hallShowtimeIds);
                } else {
                    Toast.makeText(BookingActivity.this, "Cinema not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHallShowtimes(List<String> hallShowtimeIds) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        List<HallShowTime> hallShowtimesList = new ArrayList<>();
        int[] counter = {0};

        for (String hstId : hallShowtimeIds) {
            databaseRef.child("hallShowtimes").child(hstId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    HallShowTime hallShowtime = snapshot.getValue(HallShowTime.class);
                    if (hallShowtime != null) {
                        hallShowtimesList.add(hallShowtime);
                    }

                    counter[0]++;
                    if (counter[0] == hallShowtimeIds.size()) {
                        fetchRelatedData(hallShowtimesList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    counter[0]++;
                    if (counter[0] == hallShowtimeIds.size()) {
                        fetchRelatedData(hallShowtimesList);
                    }
                }
            });
        }
    }

    private void fetchRelatedData(List<HallShowTime> hallShowtimesList) {
        if (hallShowtimesList.isEmpty()) {
            Toast.makeText(this, "No showtimes found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> hallIds = new ArrayList<>();
        List<String> showtimeIds = new ArrayList<>();

        for (HallShowTime hst : hallShowtimesList) {
            if (!hallIds.contains(hst.getHallId())) {
                hallIds.add(hst.getHallId());
            }
            if (!showtimeIds.contains(hst.getShowtimeId())) {
                showtimeIds.add(hst.getShowtimeId());
            }
        }

        fetchHalls(hallIds, hallShowtimesList, showtimeIds);
    }

    private void fetchHalls(List<String> hallIds, List<HallShowTime> hallShowtimesList, List<String> showtimeIds) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        List<Hall> hallsList = new ArrayList<>();
        int[] counter = {0};

        for (String hallId : hallIds) {
            databaseRef.child("halls").child(hallId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Hall hall = snapshot.getValue(Hall.class);
                    if (hall != null) {
                        hallsList.add(hall);
                    }

                    counter[0]++;
                    if (counter[0] == hallIds.size()) {
                        fetchShowtimes(showtimeIds, hallShowtimesList, hallsList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    counter[0]++;
                    if (counter[0] == hallIds.size()) {
                        fetchShowtimes(showtimeIds, hallShowtimesList, hallsList);
                    }
                }
            });
        }
    }

    private void fetchShowtimes(List<String> showtimeIds, List<HallShowTime> hallShowtimesList, List<Hall> hallsList) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        List<Showtime> showtimesList = new ArrayList<>();
        int[] counter = {0};

        if (showtimeIds.isEmpty()) {
            updateAdapterWithData(hallShowtimesList, hallsList, showtimesList);
            return;
        }

        for (String showtimeId : showtimeIds) {
            databaseRef.child("showtimes").child(showtimeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Showtime showtime = snapshot.getValue(Showtime.class);
                        if (showtime != null && showtime.movieId != null && showtime.movieId.equals(movieId)) {
                            showtimesList.add(showtime);
                        }
                    } catch (Exception e) {
                        Log.e("BookingActivity", "Error parsing showtime: " + e.getMessage());
                    }

                    counter[0]++;
                    if (counter[0] == showtimeIds.size()) {
                        updateAdapterWithData(hallShowtimesList, hallsList, showtimesList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    counter[0]++;
                    if (counter[0] == showtimeIds.size()) {
                        updateAdapterWithData(hallShowtimesList, hallsList, showtimesList);
                    }
                }
            });
        }
    }

    private void filterShowtimesByDateAndFormat() {
        if (dateAdapter == null || formatAdapter == null) return;

        String selectedDate = dateAdapter.getSelectedDateString();
        String selectedFormat = formatAdapter.getSelectedFormat();

        List<HallShowTime> filteredHallShowtimes = new ArrayList<>();
        List<Showtime> filteredShowtimes = new ArrayList<>();

        for (Showtime showtime : showtimes) {
            // Filter by date - direct string comparison
            if (showtime.date == null || !showtime.date.equals(selectedDate)) {
                continue;
            }

            // Find hall for this showtime
            Hall hall = null;
            for (Hall h : halls) {
                if (h.id.equals(showtime.hallId)) {
                    hall = h;
                    break;
                }
            }

            // Filter by format
            if (hall != null) {
                if (selectedFormat.equals("ALL") || hall.screenType.equals(selectedFormat)) {
                    filteredShowtimes.add(showtime);

                    for (HallShowTime hst : hallShowTimes) {
                        if (hst.getShowtimeId().equals(showtime.id)) {
                            filteredHallShowtimes.add(hst);
                            break;
                        }
                    }
                }
            }
        }

        runOnUiThread(() -> {
            if (filteredHallShowtimes.isEmpty()) {
                adapter.updateItems(new ArrayList<>(), halls, new ArrayList<>());
                if (showtimes.size() > 0) {
                    Toast.makeText(this, "No showtimes for selected date and format", Toast.LENGTH_SHORT).show();
                }
            } else {
                adapter.updateItems(filteredHallShowtimes, halls, filteredShowtimes);
            }
        });
    }

    private void updateAdapterWithData(List<HallShowTime> hallShowtimesList, List<Hall> hallsList, List<Showtime> showtimesList) {
        // Store data for filtering
        this.hallShowTimes = hallShowtimesList;
        this.halls = hallsList;
        this.showtimes = showtimesList;

        // Filter HallShowTime objects to only include those with matching showtimes
        List<HallShowTime> filteredHallShowtimes = new ArrayList<>();
        List<Showtime> validShowtimes = new ArrayList<>();

        for (HallShowTime hst : hallShowtimesList) {
            boolean hasMatchingShowtime = false;
            for (Showtime st : showtimesList) {
                if (st.id.equals(hst.getShowtimeId())) {
                    hasMatchingShowtime = true;
                    validShowtimes.add(st);
                    break;
                }
            }
            if (hasMatchingShowtime) {
                filteredHallShowtimes.add(hst);
            }
        }

        // Update adapter with filtered data
        runOnUiThread(() -> {
            if (filteredHallShowtimes.isEmpty()) {
                Toast.makeText(BookingActivity.this, "No showtimes available for this movie", Toast.LENGTH_SHORT).show();
                adapter.updateItems(new ArrayList<>(), hallsList, new ArrayList<>());
            } else {
                adapter.updateItems(filteredHallShowtimes, hallsList, validShowtimes);
            }
        });

        // Filter by current selected date and format after data loads
        filterShowtimesByDateAndFormat();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, FilmDetailsActivity.class));
            finish();
        });

        btnTrailer.setOnClickListener(v -> {
            Toast.makeText(this, "Play Trailer", Toast.LENGTH_SHORT).show();
        });
    }
}