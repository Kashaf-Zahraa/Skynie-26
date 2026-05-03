package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    ImageView ivMoviePoster, ivChooseCinemaArrow;
    TextView tvMovieTitle, tvDuration, tvSelectedCinema;
    AppCompatButton btnTrailer;
    ProgressBar progressBar;

    // Empty state views
    LinearLayout emptyStateLayout;
    AppCompatButton btnTryDifferentCinema;

    String movieId, movieTitle, moviePoster, movieBackdrop, movieRating, movieDuration, movieDescription;
    String selectedCinemaId = "c1";
    String selectedCinemaName = "Stars (90°Mall)";
    String selectedCinemaAddress = "";

    List<HallShowTime> hallShowTimes = new ArrayList<>();
    List<Hall> halls = new ArrayList<>();
    List<Showtime> showtimes = new ArrayList<>();

    private final ActivityResultLauncher<Intent> cinemaSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedCinemaId = data.getStringExtra("selected_cinema_id");
                    selectedCinemaName = data.getStringExtra("selected_cinema_name");
                    selectedCinemaAddress = data.getStringExtra("selected_cinema_address");

                    if (selectedCinemaName != null) {
                        tvSelectedCinema.setText(selectedCinemaName);
                        loadDataFromFirebase();
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    selectedCinemaId = "c1";
                    selectedCinemaName = "Stars (90°Mall)";
                    tvSelectedCinema.setText(selectedCinemaName);
                }
            });

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
        setupClickListeners();
        loadDataFromFirebase();
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
        progressBar = findViewById(R.id.progressBar);
        ivChooseCinemaArrow = findViewById(R.id.iv_choose_cinema_arrow);
        tvSelectedCinema = findViewById(R.id.tv_selected_cinema);

        // Empty state views
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnTryDifferentCinema = findViewById(R.id.btnTryDifferentCinema);

        // Try different cinema button
        btnTryDifferentCinema.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, SelectCinemaActivity.class);
            intent.putExtra("movie_id", movieId);
            intent.putExtra("movie_title", movieTitle);
            intent.putExtra("movie_poster", moviePoster);
            intent.putExtra("movie_backdrop", movieBackdrop);
            intent.putExtra("movie_rating", movieRating);
            intent.putExtra("movie_duration", movieDuration != null ? Integer.parseInt(movieDuration) : 0);
            intent.putExtra("movie_description", movieDescription);
            cinemaSelectionLauncher.launch(intent);
        });

        // Cinema selector click
        ivChooseCinemaArrow.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, SelectCinemaActivity.class);
            intent.putExtra("movie_id", movieId);
            intent.putExtra("movie_title", movieTitle);
            intent.putExtra("movie_poster", moviePoster);
            intent.putExtra("movie_backdrop", movieBackdrop);
            intent.putExtra("movie_rating", movieRating);
            intent.putExtra("movie_duration", movieDuration != null ? Integer.parseInt(movieDuration) : 0);
            intent.putExtra("movie_description", movieDescription);
            cinemaSelectionLauncher.launch(intent);
        });

        // Showtimes RecyclerView
        rvShowTimes.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new HallShowTimeAdapter(hallShowTimes, this);
        rvShowTimes.setAdapter(adapter);

        // Date Adapter
        rvDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateAdapter = new DateAdapter(this);
        dateAdapter.setOnDateChangeListener(this::filterShowtimesByDateAndFormat);
        rvDays.setAdapter(dateAdapter);

        // Format Adapter
        rvFormats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        formatAdapter = new FormatAdapter(this, (format, position) -> filterShowtimesByDateAndFormat());
        rvFormats.setAdapter(formatAdapter);

        tvSelectedCinema.setText(selectedCinemaName);
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
        if (movieTitle != null) tvMovieTitle.setText(movieTitle);
        if (movieDuration != null && !movieDuration.isEmpty())
            tvDuration.setText(movieDuration + " min");

        if (moviePoster != null && !moviePoster.isEmpty()) {
            int resourceId = getResources().getIdentifier(moviePoster, "drawable", getPackageName());
            if (resourceId != 0) {
                Glide.with(this).load(resourceId)
                        .placeholder(R.drawable.ic_movie_placeholder)
                        .into(ivMoviePoster);
            } else {
                ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder);
            }
        }
    }

    private void showContent() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            rvShowTimes.setVisibility(View.VISIBLE);
            rvDays.setVisibility(View.VISIBLE);
            rvFormats.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        });
    }

    private void showEmptyState(String title, String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            rvShowTimes.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);

            ImageView ivEmptyState = findViewById(R.id.ivEmptyState);

        });
    }

    private void loadDataFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        rvShowTimes.setVisibility(View.GONE);
        rvDays.setVisibility(View.GONE);
        rvFormats.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        hallShowTimes.clear();
        halls.clear();
        showtimes.clear();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child("cinemas").child(selectedCinemaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot cinemaSnapshot) {
                        if (!cinemaSnapshot.exists()) {
                            showEmptyState("Cinema Not Found",
                                    "The selected cinema could not be found. Please try a different cinema.");
                            return;
                        }

                        String name = cinemaSnapshot.child("name").getValue(String.class);
                        if (name != null) {
                            selectedCinemaName = name;
                            tvSelectedCinema.setText(selectedCinemaName);
                        }

                        if (movieTitle != null && !selectedCinemaName.isEmpty())
                            adapter.setMovieAndCinema(movieTitle, selectedCinemaName);

                        List<String> hallShowtimeIds = new ArrayList<>();
                        for (DataSnapshot idSnap : cinemaSnapshot.child("hallShowtimeIds").getChildren()) {
                            String val = idSnap.getValue(String.class);
                            if (val != null) hallShowtimeIds.add(val);
                        }

                        if (hallShowtimeIds.isEmpty()) {
                            showEmptyState("No Showtimes Available",
                                    "No showtimes are currently available at " + selectedCinemaName + " for this movie.");
                            return;
                        }

                        fetchHallShowtimes(hallShowtimeIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showEmptyState("Loading Failed",
                                "Failed to load cinema data. Please check your connection and try again.");
                    }
                });
    }

    private void fetchHallShowtimes(List<String> ids) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        List<HallShowTime> list = new ArrayList<>();
        int[] done = {0};

        if (ids.isEmpty()) {
            showEmptyState("No Showtimes Available",
                    "No showtimes are currently available at " + selectedCinemaName);
            return;
        }

        for (String id : ids) {
            db.child("hallShowtimes").child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            HallShowTime h = snap.getValue(HallShowTime.class);
                            if (h != null) list.add(h);
                            if (++done[0] == ids.size()) fetchRelatedData(list);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {
                            if (++done[0] == ids.size()) fetchRelatedData(list);
                        }
                    });
        }
    }

    private void fetchRelatedData(List<HallShowTime> hstList) {
        if (hstList.isEmpty()) {
            showEmptyState("No Showtimes Available",
                    "No showtimes are currently available at " + selectedCinemaName);
            return;
        }

        List<String> hallIds = new ArrayList<>(), stIds = new ArrayList<>();
        for (HallShowTime h : hstList) {
            if (!hallIds.contains(h.getHallId())) hallIds.add(h.getHallId());
            if (!stIds.contains(h.getShowtimeId())) stIds.add(h.getShowtimeId());
        }
        fetchHalls(hallIds, hstList, stIds);
    }

    private void fetchHalls(List<String> ids, List<HallShowTime> hstList, List<String> stIds) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        List<Hall> list = new ArrayList<>();
        int[] done = {0};

        if (ids.isEmpty()) {
            fetchShowtimes(stIds, hstList, list);
            return;
        }

        for (String id : ids) {
            db.child("halls").child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            Hall h = snap.getValue(Hall.class);
                            if (h != null) {
                                h.id = id;
                                list.add(h);
                            }
                            if (++done[0] == ids.size()) fetchShowtimes(stIds, hstList, list);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {
                            if (++done[0] == ids.size()) fetchShowtimes(stIds, hstList, list);
                        }
                    });
        }
    }

    private void fetchShowtimes(List<String> ids, List<HallShowTime> hstList, List<Hall> hallList) {
        if (ids.isEmpty()) {
            updateAdapter(hstList, hallList, new ArrayList<>());
            return;
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        List<Showtime> list = new ArrayList<>();
        int[] done = {0};

        for (String id : ids) {
            db.child("showtimes").child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            try {
                                Showtime st = snap.getValue(Showtime.class);
                                if (st != null && movieId != null && movieId.equals(st.movieId)) {
                                    st.id = id;
                                    list.add(st);
                                }
                            } catch (Exception e) {
                                Log.e("BookingActivity", "Error parsing showtime: " + e.getMessage());
                            }
                            if (++done[0] == ids.size()) updateAdapter(hstList, hallList, list);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {
                            if (++done[0] == ids.size()) updateAdapter(hstList, hallList, list);
                        }
                    });
        }
    }

    private void updateAdapter(List<HallShowTime> hstList, List<Hall> hallList, List<Showtime> stList) {
        this.hallShowTimes = hstList;
        this.halls = hallList;
        this.showtimes = stList;

        runOnUiThread(() -> {
            if (showtimes.isEmpty()) {
                showEmptyState("No Showtimes Available",
                        "No showtimes are currently available for this movie at " + selectedCinemaName);
                return;
            }

            showContent();
            filterShowtimesByDateAndFormat();
        });
    }

    private void filterShowtimesByDateAndFormat() {
        if (dateAdapter == null || formatAdapter == null || showtimes.isEmpty()) return;

        String selDate = dateAdapter.getSelectedDateString();
        String selFormat = formatAdapter.getSelectedFormat();

        List<HallShowTime> filtHst = new ArrayList<>();
        List<Showtime> filtSt = new ArrayList<>();

        java.util.Map<String, Hall> hallMap = new java.util.HashMap<>();
        for (Hall h : halls) {
            hallMap.put(h.id, h);
        }

        java.util.Map<String, HallShowTime> hstMap = new java.util.HashMap<>();
        for (HallShowTime hst : hallShowTimes) {
            hstMap.put(hst.getShowtimeId(), hst);
        }

        for (Showtime st : showtimes) {
            if (st.date == null || !st.date.equals(selDate)) continue;

            Hall hall = hallMap.get(st.hallId);
            if (hall != null) {
                boolean formatMatches = selFormat.equals("ALL") ||
                        (hall.screenType != null && hall.screenType.equals(selFormat));
                if (formatMatches) {
                    filtSt.add(st);
                    HallShowTime hst = hstMap.get(st.id);
                    if (hst != null) {
                        filtHst.add(hst);
                    }
                }
            }
        }

        if (filtSt.isEmpty()) {
            showEmptyState("No Showtimes",
                    "No showtimes available for " + selDate + " at " + selectedCinemaName);
        } else {
            showContent();
            runOnUiThread(() -> adapter.updateItems(filtHst, halls, filtSt));
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(BookingActivity.this, FilmDetailsActivity.class));
            finish();
        });

        btnTrailer.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrailerActivity.class);
            intent.putExtra("movie_id", movieId);
            intent.putExtra("movie_title", movieTitle);
            intent.putExtra("movie_duration", movieDuration != null ? Integer.parseInt(movieDuration) : 0);
            startActivity(intent);
        });
    }
}