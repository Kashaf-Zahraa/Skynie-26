package com.example.skynie.views;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.adapters.ActorAdapter;
import com.example.skynie.adapters.ActorDialog;
import com.example.skynie.adapters.MovieAdapter;
import com.example.skynie.models.Cast;
import com.example.skynie.models.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilmDetailsActivity extends AppCompatActivity {
    ImageButton btnBack, btnFav, btnPlay;
    AppCompatButton btnBuyTicket;
    TextView tvFilmTitle, tvDescription, tvWriter, tvDirector, tvDuration;
    FrameLayout movieBg;
    RecyclerView rvVideos, rvStars, rvRecommendations;
    CoordinatorLayout main;
    ArrayList<String> directors, writers, actors;
    DatabaseReference databaseReference;

    ProgressBar progressBar;

    // Store movie details as class variables
    private String movie_id, movie_title, movie_poster, movie_backdrop, movie_rating, movie_description;
    private int movie_duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_film_details);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        getAndSetValues();
    }

    private void init() {
        btnBack = findViewById(R.id.btn_back);
        btnFav = findViewById(R.id.btn_fav);
        btnPlay = findViewById(R.id.btn_play);
        btnBuyTicket = findViewById(R.id.btn_buy_ticket);
        tvFilmTitle = findViewById(R.id.tv_film_title);
        tvDescription = findViewById(R.id.tv_description);
        tvDuration = findViewById(R.id.tv_duration);
        tvWriter = findViewById(R.id.tv_writer);
        tvDirector = findViewById(R.id.tv_director);
        movieBg = findViewById(R.id.movie_bg);
        rvStars = findViewById(R.id.rv_stars);
        rvRecommendations = findViewById(R.id.rv_recommendations);
        main = findViewById(R.id.main);
        progressBar=findViewById(R.id.progressBar);

        btnBack.setOnClickListener((v) -> {
            startActivity(new Intent(FilmDetailsActivity.this, MainActivity.class));
            finish();
        });

        btnFav.setOnClickListener((v) -> {
            btnFav.setImageResource(R.drawable.ic_heart_filled);
            Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show();
        });
    }

    private void getAndSetValues() {
        Intent i = getIntent();
        movie_id = i.getStringExtra("movie_id");
        movie_title = i.getStringExtra("movie_title");
        movie_duration = i.getIntExtra("movie_duration", 0);
        movie_description = i.getStringExtra("movie_description");
        String duration_st = String.valueOf(movie_duration) + " mins";
        movie_poster = i.getStringExtra("movie_poster");
        movie_backdrop = i.getStringExtra("movie_backdrop");
        movie_rating = i.getStringExtra("movie_rating");

        // Get additional data for trailer
        String trailerUrl = i.getStringExtra("trailer_url");
        String pgRating = i.getStringExtra("pg_rating");
        String language = i.getStringExtra("language");

        tvFilmTitle.setText(movie_title);
        tvDescription.setText(movie_description);
        tvDuration.setText(duration_st);

        // Convert string name to drawable resource ID
        int imageResId = getDrawableResourceId(movie_poster);
        movieBg.setBackgroundResource(imageResId);

        // ✅ FIXED: btnPlay - Create final copies for lambda
        final String finalTrailerUrl = trailerUrl;
        final String finalPgRating = pgRating;
        final String finalLanguage = language;
        final String finalMovieId = movie_id;
        final String finalMovieTitle = movie_title;
        final int finalMovieDuration = movie_duration;

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(FilmDetailsActivity.this, TrailerActivity.class);
            intent.putExtra("movie_id", finalMovieId);
            intent.putExtra("movie_title", finalMovieTitle);
            intent.putExtra("movie_duration", finalMovieDuration);
            intent.putExtra("trailer_url", finalTrailerUrl);
            intent.putExtra("pg_rating", finalPgRating);
            intent.putExtra("language", finalLanguage);
            startActivity(intent);
        });

        final String finalMovieId2 = movie_id;
        final String finalMovieTitle2 = movie_title;
        final String finalMoviePoster = movie_poster;
        final String finalMovieBackdrop = movie_backdrop;
        final String finalMovieRating = movie_rating;
        final int finalMovieDuration2 = movie_duration;
        final String finalMovieDescription = movie_description;
        final String finalTrailerUrl2 = trailerUrl;
        final String finalPgRating2 = pgRating;
        final String finalLanguage2 = language;

        btnBuyTicket.setOnClickListener((v) -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("movie_id", finalMovieId2);
            intent.putExtra("movie_title", finalMovieTitle2);
            intent.putExtra("movie_poster", finalMoviePoster);
            intent.putExtra("movie_backdrop", finalMovieBackdrop);
            intent.putExtra("movie_rating", finalMovieRating);
            intent.putExtra("movie_duration", finalMovieDuration2);
            intent.putExtra("movie_description", finalMovieDescription);
            intent.putExtra("trailer_url", finalTrailerUrl2);      // ✅ ADDED
            intent.putExtra("pg_rating", finalPgRating2);          // ✅ ADDED
            intent.putExtra("language", finalLanguage2);           // ✅ ADDED
            startActivity(intent);
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("movie_details");
        databaseReference.child(movie_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Cast cast = snapshot.getValue(Cast.class);

                    if (cast != null) {
                        actors = cast.getActors();
                        writers = cast.getWriters();
                        directors = cast.getDirectors();

                        // Set directors and writers
                        String directorsString = TextUtils.join(", ", directors);
                        String writersString = TextUtils.join("\n", writers);
                        tvDirector.setText(directorsString);
                        tvWriter.setText(writersString);

                        // Set up actors RecyclerView
                        setupActorsRecyclerView(actors);
                        loadRecommendations(movie_id);
                    }
                } else {
                    Toast.makeText(FilmDetailsActivity.this, "Movie not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FilmDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getDrawableResourceId(String drawableName) {
        if (drawableName == null || drawableName.isEmpty()) {
            return R.drawable.ic_movie_placeholder;
        }

        int resId = this.getResources().getIdentifier(
                drawableName,
                "drawable",
                this.getPackageName()
        );

        return resId != 0 ? resId : R.drawable.ic_movie_placeholder;
    }

    private void setupActorsRecyclerView(ArrayList<String> actors) {
        if (actors == null || actors.isEmpty()) {
            rvStars.setVisibility(View.GONE);
            return;
        }

        ActorAdapter adapter = new ActorAdapter(actors, allActors -> {
            ActorDialog dialog = new ActorDialog(FilmDetailsActivity.this, allActors, "Full Cast");
            dialog.show();
        });

        rvStars.setLayoutManager(new LinearLayoutManager(this));
        rvStars.setAdapter(adapter);
    }

    private void loadRecommendations(String currentMovieId) {

        progressBar.setVisibility(View.VISIBLE);
        rvRecommendations.setVisibility(View.GONE);
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("movies");

        moviesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Movie> recommendations = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    String movieId = data.child("id").getValue(String.class);

                    if (movieId != null && !movieId.equals(currentMovieId)) {
                        String title = data.child("title").getValue(String.class);
                        String description = data.child("description").getValue(String.class);
                        String posterDrawable = data.child("poster_drawable").getValue(String.class);
                        String backdropDrawable = data.child("backdrop_drawable").getValue(String.class);
                        String trailerUrl = data.child("trailer_url").getValue(String.class);
                        Float rating = data.child("rating").getValue(Float.class);
                        Integer durationMinutes = data.child("duration_minutes").getValue(Integer.class);
                        String language = data.child("language").getValue(String.class);
                        Long releaseDate = data.child("release_date").getValue(Long.class);
                        String pgRating = data.child("pg_rating").getValue(String.class);
                        String isNowShowing = data.child("is_now_showing").getValue(String.class);
                        String isComingSoon = data.child("is_coming_soon").getValue(String.class);

                        Movie movie = new Movie(
                                movieId, title, description, posterDrawable,
                                backdropDrawable, trailerUrl, rating != null ? rating : 0f,
                                durationMinutes != null ? durationMinutes : 0, language,
                                releaseDate != null ? releaseDate : 0, pgRating,
                                isNowShowing, isComingSoon
                        );
                        recommendations.add(movie);
                    }
                }

                if (recommendations.size() > 10) {
                    recommendations = recommendations.subList(0, 10);
                }

                setupRecommendations(recommendations);


                progressBar.setVisibility(View.GONE);
                rvRecommendations.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Recommendations", "Error loading: " + error.getMessage());
                Toast.makeText(FilmDetailsActivity.this, "Failed to load recommendations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecommendations(List<Movie> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            rvRecommendations.setVisibility(View.GONE);
            return;
        }

        rvRecommendations.setVisibility(View.VISIBLE);
        MovieAdapter adapter = new MovieAdapter(this, recommendations);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setAdapter(adapter);
    }

}