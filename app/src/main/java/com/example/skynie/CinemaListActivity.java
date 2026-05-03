package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.models.Cinema;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CinemaListActivity extends AppCompatActivity {

    private RecyclerView rvCinemas;
    private ProgressBar progressBar;
    private CinemaPickAdapter adapter;
    private final List<Cinema> cinemaList = new ArrayList<>();

    // BookingActivity se jo data aaya usse preserve karna hai
    private String movieId, movieTitle, moviePoster, movieBackdrop,
            movieRating, movieDuration, movieDescription,
            trailerUrl, pgRating, language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_list);

        // BookingActivity se saara data receive karo
        movieId          = getIntent().getStringExtra("movie_id");
        movieTitle       = getIntent().getStringExtra("movie_title");
        moviePoster      = getIntent().getStringExtra("movie_poster");
        movieBackdrop    = getIntent().getStringExtra("movie_backdrop");
        movieRating      = getIntent().getStringExtra("movie_rating");
        movieDuration    = getIntent().getStringExtra("movie_duration");
        movieDescription = getIntent().getStringExtra("movie_description");
        trailerUrl       = getIntent().getStringExtra("trailer_url");
        pgRating         = getIntent().getStringExtra("pg_rating");
        language         = getIntent().getStringExtra("language");

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        rvCinemas   = findViewById(R.id.rvCinemas);
        rvCinemas.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CinemaPickAdapter(cinemaList);
        rvCinemas.setAdapter(adapter);

        loadCinemas();
    }

    private void loadCinemas() {
        progressBar.setVisibility(View.VISIBLE);
        rvCinemas.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("cinemas")
                .orderByChild("is_active").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cinemaList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Cinema c = data.getValue(Cinema.class);
                            if (c != null) cinemaList.add(c);
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        rvCinemas.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void onCinemaSelected(Cinema cinema) {
        // ✅ BookingActivity wapas open karo selected cinema ke saath
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("movie_id",          movieId);
        intent.putExtra("movie_title",       movieTitle);
        intent.putExtra("movie_poster",      moviePoster);
        intent.putExtra("movie_backdrop",    movieBackdrop);
        intent.putExtra("movie_rating",      movieRating);
        intent.putExtra("movie_duration",    movieDuration != null ? Integer.parseInt(movieDuration) : 0);
        intent.putExtra("movie_description", movieDescription);
        intent.putExtra("trailer_url",       trailerUrl);
        intent.putExtra("pg_rating",         pgRating);
        intent.putExtra("language",          language);
        // ✅ Selected cinema ka ID pass karo
        intent.putExtra("selected_cinema_id", cinema.id);
        intent.putExtra("selected_cinema_name", cinema.name);
        // Stack clear karo taake back press se CinemaList pe na jaaye
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ─── Adapter ────────────────────────────────────────────────
    private class CinemaPickAdapter extends RecyclerView.Adapter<CinemaPickAdapter.VH> {
        private final List<Cinema> list;
        CinemaPickAdapter(List<Cinema> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cinema_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Cinema c = list.get(position);
            holder.tvName.setText(c.name);
            holder.tvAddress.setText(c.address);

            // Screen types dikhao
            if (c.screenTypes != null && !c.screenTypes.isEmpty()) {
                holder.tvDistance.setText(String.join(" • ", c.screenTypes));
                holder.tvDistance.setVisibility(View.VISIBLE);
            } else {
                holder.tvDistance.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> onCinemaSelected(c));
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress, tvDistance;
            VH(@NonNull View v) {
                super(v);
                tvName     = v.findViewById(R.id.tvCinemaName);
                tvAddress  = v.findViewById(R.id.tvCinemaAddress);
                tvDistance = v.findViewById(R.id.tvDistance);
            }
        }
    }
}