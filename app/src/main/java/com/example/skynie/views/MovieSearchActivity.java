package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.models.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieSearchActivity extends AppCompatActivity {

    private EditText etMovieSearch;
    private RecyclerView rvMovieResults;
    private LinearLayout llEmptyState, llNoResults;
    private MovieSearchAdapter adapter;

    private final List<Movie> allMovies    = new ArrayList<>();
    private final List<Movie> shownMovies  = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_search);

        ImageButton btnBack = findViewById(R.id.btnBack);
        etMovieSearch = findViewById(R.id.etMovieSearch);
        rvMovieResults = findViewById(R.id.rvMovieResults);
        llEmptyState  = findViewById(R.id.llEmptyState);
        llNoResults   = findViewById(R.id.llNoResults);

        btnBack.setOnClickListener(v -> finish());

        rvMovieResults.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new MovieSearchAdapter(shownMovies);
        rvMovieResults.setAdapter(adapter);

        // Show empty state initially
        showEmptyState();

        // Load all movies from Firebase
        loadAllMovies();

        // Search filter
        etMovieSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filterMovies(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Auto-focus keyboard
        etMovieSearch.requestFocus();
    }

    private void loadAllMovies() {
        FirebaseDatabase.getInstance().getReference("movies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allMovies.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Movie m = data.getValue(Movie.class);
                            if (m != null) allMovies.add(m);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void filterMovies(String query) {
        if (query.trim().isEmpty()) {
            showEmptyState();
            return;
        }

        String q = query.trim().toLowerCase(Locale.ROOT);
        shownMovies.clear();

        for (Movie m : allMovies) {
            if (m.title != null && m.title.toLowerCase(Locale.ROOT).contains(q)) {
                shownMovies.add(m);
            }
        }

        if (shownMovies.isEmpty()) {
            showNoResults();
        } else {
            showResults();
        }
        adapter.notifyDataSetChanged();
    }

    private void showEmptyState() {
        llEmptyState.setVisibility(View.VISIBLE);
        llNoResults.setVisibility(View.GONE);
        rvMovieResults.setVisibility(View.GONE);
    }

    private void showNoResults() {
        llEmptyState.setVisibility(View.GONE);
        llNoResults.setVisibility(View.VISIBLE);
        rvMovieResults.setVisibility(View.GONE);
    }

    private void showResults() {
        llEmptyState.setVisibility(View.GONE);
        llNoResults.setVisibility(View.GONE);
        rvMovieResults.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────
    //  GRID ADAPTER
    // ─────────────────────────────────────────────────────────────
    private class MovieSearchAdapter extends RecyclerView.Adapter<MovieSearchAdapter.VH> {

        private final List<Movie> list;
        MovieSearchAdapter(List<Movie> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grid_movie, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Movie movie = list.get(position);
            holder.tvTitle.setText(movie.title);

            int resId = getResources().getIdentifier(
                    movie.poster_drawable, "drawable", getPackageName());
            holder.ivPoster.setImageResource(resId != 0 ? resId : R.color.dark_gray);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MovieSearchActivity.this, FilmDetailsActivity.class);
                intent.putExtra("movie_id",          movie.id);
                intent.putExtra("movie_title",       movie.title);
                intent.putExtra("movie_poster",      movie.poster_drawable);
                intent.putExtra("movie_backdrop",    movie.backdrop_drawable);
                intent.putExtra("movie_rating",      String.valueOf(movie.rating));
                intent.putExtra("movie_duration",    movie.duration_minutes);
                intent.putExtra("movie_description", movie.description);
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivPoster;
            TextView tvTitle;
            VH(@NonNull View v) {
                super(v);
                ivPoster = v.findViewById(R.id.ivGridPoster);
                tvTitle  = v.findViewById(R.id.tvGridTitle);
            }
        }
    }
}