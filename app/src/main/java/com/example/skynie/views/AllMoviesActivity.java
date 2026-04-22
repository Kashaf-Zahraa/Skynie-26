package com.example.skynie.views;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.models.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllMoviesActivity extends AppCompatActivity {

    private DatabaseReference moviesRef;
    private RecyclerView rvAllMovies;
    private GridMovieAdapter adapter;
    private final List<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_movies);

        // Intent se section lo: "now_showing" ya "coming_soon"
        String section = getIntent().getStringExtra("section");
        if (section == null) section = "now_showing";

        // Title set karo
        TextView tvTitle = findViewById(R.id.tvSectionTitle);
        tvTitle.setText(section.equals("now_showing") ? "Now Showing" : "Coming Soon");

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // RecyclerView setup — 3 columns grid
        rvAllMovies = findViewById(R.id.rvAllMovies);
        rvAllMovies.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new GridMovieAdapter(movieList);
        rvAllMovies.setAdapter(adapter);

        moviesRef = FirebaseDatabase.getInstance().getReference("movies");

        // Firebase se movies load karo based on section
        String filterField = section.equals("now_showing") ? "is_now_showing" : "is_coming_soon";
        loadMovies(filterField);

    }

    private void loadMovies(String filterField) {
        moviesRef.orderByChild(filterField).equalTo("true")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        movieList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Movie movie = data.getValue(Movie.class);
                            if (movie != null) movieList.add(movie);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Index missing hoga toh yahan error aata hai
                        // Firebase Console mein index add karo (README mein bataya hai)
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  GRID ADAPTER  (item_grid_movie.xml use karta hai)
    // ─────────────────────────────────────────────────────────────
    private class GridMovieAdapter extends RecyclerView.Adapter<GridMovieAdapter.VH> {

        private final List<Movie> list;
        GridMovieAdapter(List<Movie> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grid_movie, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Movie movie = list.get(position);
            holder.tvTitle.setText(movie.title);

            // Drawable se poster load karo
            int resId = getDrawableId(movie.poster_drawable);
            holder.ivPoster.setImageResource(resId != 0 ? resId : R.color.dark_gray);

            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent =
                        new android.content.Intent(AllMoviesActivity.this, FilmDetailsActivity.class);
                intent.putExtra("movie_id",          movie.id);
                intent.putExtra("movie_title",       movie.title);
                intent.putExtra("movie_poster",      movie.poster_drawable);
                intent.putExtra("movie_backdrop",    movie.backdrop_drawable);
                intent.putExtra("movie_rating",      movie.rating);
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

    // ─────────────────────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────────────────────
    private int getDrawableId(String name) {
        if (name == null || name.isEmpty()) return R.color.dark_gray;
        if (name.contains(".")) name = name.substring(0, name.lastIndexOf("."));
        int resId = getResources().getIdentifier(name, "drawable", getPackageName());
        return resId != 0 ? resId : R.color.dark_gray;
    }

}