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
    ImageView ivMoviePoster;
    TextView tvMovieTitle, tvDuration;
    AppCompatButton btnTrailer;

    String movieId, movieTitle, moviePoster, movieBackdrop,movieRating, movieDuration, movieDescription;
    String cinemaName = "";

    List<HallShowTime> hallShowTimes = new ArrayList<>();
    List<Hall>         halls         = new ArrayList<>();
    List<Showtime>     showtimes     = new ArrayList<>();

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
        main       = findViewById(R.id.main);
        btnBack    = findViewById(R.id.btn_back);
        rvDays     = findViewById(R.id.rv_days);
        rvFormats  = findViewById(R.id.rv_formats);
        rvShowTimes = findViewById(R.id.rv_show_times);
        ivMoviePoster = findViewById(R.id.iv_movie_poster);
        tvMovieTitle  = findViewById(R.id.tv_movie_title);
        tvDuration    = findViewById(R.id.tv_duration);
        btnTrailer    = findViewById(R.id.btn_trailer);

        // Showtimes RecyclerView
        rvShowTimes.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new HallShowTimeAdapter(hallShowTimes, this);
        rvShowTimes.setAdapter(adapter);

        // Date Adapter
        rvDays.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateAdapter = new DateAdapter(this);
        dateAdapter.setOnDateChangeListener(this::filterShowtimesByDateAndFormat);
        rvDays.setAdapter(dateAdapter);

        // Format Adapter
        rvFormats.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        formatAdapter = new FormatAdapter(this, (format, position) ->
                filterShowtimesByDateAndFormat());
        rvFormats.setAdapter(formatAdapter);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        movieId          = intent.getStringExtra("movie_id");
        movieTitle       = intent.getStringExtra("movie_title");
        moviePoster      = intent.getStringExtra("movie_poster");
        movieBackdrop    = intent.getStringExtra("movie_backdrop");
        movieRating      = intent.getStringExtra("movie_rating");
        movieDuration    = String.valueOf(intent.getIntExtra("movie_duration", 0));
        movieDescription = intent.getStringExtra("movie_description");
    }

    private void setupMovieInfo() {
        if (movieTitle != null)   tvMovieTitle.setText(movieTitle);
        if (movieDuration != null && !movieDuration.isEmpty())
            tvDuration.setText(movieDuration + " min");

        if (moviePoster != null && !moviePoster.isEmpty()) {
            int resourceId = getResources().getIdentifier(
                    moviePoster, "drawable", getPackageName());
            if (resourceId != 0) {
                Glide.with(this).load(resourceId)
                        .placeholder(R.drawable.ic_movie_placeholder)
                        .into(ivMoviePoster);
            } else {
                ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder);
            }
        }
    }

    private void loadDataFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child("cinemas").child("c1")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot cinemaSnapshot) {
                        if (!cinemaSnapshot.exists()) {
                            Toast.makeText(BookingActivity.this,
                                    "Cinema not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        cinemaName = cinemaSnapshot.child("name").getValue(String.class);
                        if (cinemaName == null) cinemaName = "";

                        if (movieTitle != null && !cinemaName.isEmpty())
                            adapter.setMovieAndCinema(movieTitle, cinemaName);

                        List<String> hallShowtimeIds = new ArrayList<>();
                        for (DataSnapshot idSnap :
                                cinemaSnapshot.child("hallShowtimeIds").getChildren()) {
                            String val = idSnap.getValue(String.class);
                            if (val != null) hallShowtimeIds.add(val);
                        }

                        if (hallShowtimeIds.isEmpty()) {
                            Toast.makeText(BookingActivity.this,
                                    "No showtimes available", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        fetchHallShowtimes(hallShowtimeIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookingActivity.this,
                                "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchHallShowtimes(List<String> ids) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        List<HallShowTime> list = new ArrayList<>();
        int[] done = {0};

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
        if (hstList.isEmpty()) return;

        List<String> hallIds = new ArrayList<>(), stIds = new ArrayList<>();
        for (HallShowTime h : hstList) {
            if (!hallIds.contains(h.getHallId()))       hallIds.add(h.getHallId());
            if (!stIds.contains(h.getShowtimeId()))     stIds.add(h.getShowtimeId());
        }
        fetchHalls(hallIds, hstList, stIds);
    }

    private void fetchHalls(List<String> ids, List<HallShowTime> hstList,
                            List<String> stIds) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        List<Hall> list = new ArrayList<>();
        int[] done = {0};

        for (String id : ids) {
            db.child("halls").child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            Hall h = snap.getValue(Hall.class);
                            if (h != null) list.add(h);
                            if (++done[0] == ids.size()) fetchShowtimes(stIds, hstList, list);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {
                            if (++done[0] == ids.size()) fetchShowtimes(stIds, hstList, list);
                        }
                    });
        }
    }

    private void fetchShowtimes(List<String> ids, List<HallShowTime> hstList,
                                List<Hall> hallList) {
        if (ids.isEmpty()) { updateAdapter(hstList, hallList, new ArrayList<>()); return; }

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
                                if (st != null && movieId != null
                                        && movieId.equals(st.movieId)) list.add(st);
                            } catch (Exception e) {
                                Log.e("BookingActivity", e.getMessage());
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

    private void updateAdapter(List<HallShowTime> hstList, List<Hall> hallList,
                               List<Showtime> stList) {
        this.hallShowTimes = hstList;
        this.halls         = hallList;
        this.showtimes     = stList;

        List<HallShowTime> filtered = new ArrayList<>();
        List<Showtime>     valid    = new ArrayList<>();

        for (HallShowTime hst : hstList) {
            for (Showtime st : stList) {
                if (st.id.equals(hst.getShowtimeId())) {
                    filtered.add(hst);
                    valid.add(st);
                    break;
                }
            }
        }

        runOnUiThread(() -> adapter.updateItems(
                filtered.isEmpty() ? new ArrayList<>() : filtered,
                hallList,
                filtered.isEmpty() ? new ArrayList<>() : valid));

        filterShowtimesByDateAndFormat();
    }

    private void filterShowtimesByDateAndFormat() {
        if (dateAdapter == null || formatAdapter == null) return;

        String selDate   = dateAdapter.getSelectedDateString();
        String selFormat = formatAdapter.getSelectedFormat();

        List<HallShowTime> filtHst = new ArrayList<>();
        List<Showtime>     filtSt  = new ArrayList<>();

        for (Showtime st : showtimes) {
            if (st.date == null || !st.date.equals(selDate)) continue;

            Hall hall = null;
            for (Hall h : halls) {
                if (h.id.equals(st.hallId)) { hall = h; break; }
            }

            if (hall != null && (selFormat.equals("ALL")
                    || hall.screenType.equals(selFormat))) {
                filtSt.add(st);
                for (HallShowTime hst : hallShowTimes) {
                    if (hst.getShowtimeId().equals(st.id)) {
                        filtHst.add(hst);
                        break;
                    }
                }
            }
        }

        runOnUiThread(() -> adapter.updateItems(filtHst, halls, filtSt));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v ->{
            startActivity(new Intent(BookingActivity.this, FilmDetailsActivity.class));
            finish();
        });


        btnTrailer.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrailerActivity.class);
            intent.putExtra("movie_id",       movieId);
            intent.putExtra("movie_title",    movieTitle);
            intent.putExtra("movie_duration", movieDuration != null
                    ? Integer.parseInt(movieDuration) : 0);
            // trailer_url blank — TrailerActivity Firebase se fetch karega
            startActivity(intent);
        });
    }
}