package com.example.skynie.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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

public class FilmDetailsActivity extends AppCompatActivity {
    ImageButton btnBack, btnFav, btnPlay;
    AppCompatButton btnBuyTicket;
    TextView tvFilmTitle, tvDescription, tvWriter, tvDirector, tvDuration;
    FrameLayout movieBg;
    RecyclerView rvVideos, rvStars, rvRecommendations;
    CoordinatorLayout main;
    ArrayList<String> directors, writers, actors;
    DatabaseReference databaseReference;

    // Store movie details as class variables
    private String movie_id, movie_title, movie_poster, movie_backdrop, movie_rating, movie_description;
    private int movie_duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_film_details);

        addMoviesToFirebase();

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

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(FilmDetailsActivity.this, TrailerActivity.class);
            intent.putExtra("movie_id", movie_id);
            intent.putExtra("movie_title", movie_title);
            intent.putExtra("movie_duration", movie_duration);
            intent.putExtra("trailer_url", finalTrailerUrl);
            intent.putExtra("pg_rating", finalPgRating);
            intent.putExtra("language", finalLanguage);
            startActivity(intent);
        });

        // ✅ FIXED: btnBuyTicket - Create final copies for lambda
        final String finalMovieId = movie_id;
        final String finalMovieTitle = movie_title;
        final String finalMoviePoster = movie_poster;
        final String finalMovieBackdrop = movie_backdrop;
        final String finalMovieRating = movie_rating;
        final int finalMovieDuration = movie_duration;
        final String finalMovieDescription = movie_description;

        btnBuyTicket.setOnClickListener((v) -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("movie_id", finalMovieId);
            intent.putExtra("movie_title", finalMovieTitle);
            intent.putExtra("movie_poster", finalMoviePoster);
            intent.putExtra("movie_backdrop", finalMovieBackdrop);
            intent.putExtra("movie_rating", finalMovieRating);
            intent.putExtra("movie_duration", finalMovieDuration);
            intent.putExtra("movie_description", finalMovieDescription);
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

    private void addMoviesToFirebase() {
        DatabaseReference moviesRef = FirebaseDatabase.getInstance()
                .getReference("movie_details");

        // MOVIE 3: Inside Out 2
        ArrayList<String> directors3 = new ArrayList<>();
        directors3.add("Kelsey Mann");

        ArrayList<String> writers3 = new ArrayList<>();
        writers3.add("Meg LeFauve");
        writers3.add("Dave Holstein");

        ArrayList<String> actors3 = new ArrayList<>();
        actors3.add("Amy Poehler");
        actors3.add("Maya Hawke");
        actors3.add("Kensington Tallman");
        actors3.add("Liza Lapira");
        actors3.add("Tony Hale");

        Cast movie3 = new Cast(actors3, "movie3", directors3, writers3);

        // MOVIE 4: Deadpool & Wolverine
        ArrayList<String> directors4 = new ArrayList<>();
        directors4.add("Shawn Levy");

        ArrayList<String> writers4 = new ArrayList<>();
        writers4.add("Ryan Reynolds");
        writers4.add("Rhett Reese");
        writers4.add("Paul Wernick");
        writers4.add("Zeb Wells");
        writers4.add("Shawn Levy");

        ArrayList<String> actors4 = new ArrayList<>();
        actors4.add("Ryan Reynolds");
        actors4.add("Hugh Jackman");
        actors4.add("Emma Corrin");
        actors4.add("Morena Baccarin");
        actors4.add("Rob Delaney");
        actors4.add("Leslie Uggams");

        Cast movie4 = new Cast(actors4, "movie4", directors4, writers4);

        // MOVIE 5: Gladiator II
        ArrayList<String> directors5 = new ArrayList<>();
        directors5.add("Ridley Scott");

        ArrayList<String> writers5 = new ArrayList<>();
        writers5.add("David Scarpa");

        ArrayList<String> actors5 = new ArrayList<>();
        actors5.add("Paul Mescal");
        actors5.add("Denzel Washington");
        actors5.add("Pedro Pascal");
        actors5.add("Joseph Quinn");
        actors5.add("Connie Nielsen");
        actors5.add("Derek Jacobi");

        Cast movie5 = new Cast(actors5, "movie5", directors5, writers5);

        // MOVIE 6: Joker 2
        ArrayList<String> directors6 = new ArrayList<>();
        directors6.add("Todd Phillips");

        ArrayList<String> writers6 = new ArrayList<>();
        writers6.add("Todd Phillips");
        writers6.add("Scott Silver");

        ArrayList<String> actors6 = new ArrayList<>();
        actors6.add("Joaquin Phoenix");
        actors6.add("Lady Gaga");
        actors6.add("Zazie Beetz");
        actors6.add("Brendan Gleeson");
        actors6.add("Catherine Keener");

        Cast movie6 = new Cast(actors6, "movie6", directors6, writers6);

        // MOVIE 7: Kraven the Hunter
        ArrayList<String> directors7 = new ArrayList<>();
        directors7.add("J.C. Chandor");

        ArrayList<String> writers7 = new ArrayList<>();
        writers7.add("Richard Wenk");
        writers7.add("Art Marcum");
        writers7.add("Matt Holloway");

        ArrayList<String> actors7 = new ArrayList<>();
        actors7.add("Aaron Taylor-Johnson");
        actors7.add("Russell Crowe");
        actors7.add("Ariana DeBose");
        actors7.add("Fred Hechinger");
        actors7.add("Alessandro Nivola");

        Cast movie7 = new Cast(actors7, "movie7", directors7, writers7);

        // MOVIE 8: Mickey 17
        ArrayList<String> directors8 = new ArrayList<>();
        directors8.add("Bong Joon-ho");

        ArrayList<String> writers8 = new ArrayList<>();
        writers8.add("Bong Joon-ho");

        ArrayList<String> actors8 = new ArrayList<>();
        actors8.add("Robert Pattinson");
        actors8.add("Naomi Ackie");
        actors8.add("Steven Yeun");
        actors8.add("Toni Collette");
        actors8.add("Mark Ruffalo");

        Cast movie8 = new Cast(actors8, "movie8", directors8, writers8);

        // MOVIE 9: Snow White
        ArrayList<String> directors9 = new ArrayList<>();
        directors9.add("Marc Webb");

        ArrayList<String> writers9 = new ArrayList<>();
        writers9.add("Greta Gerwig");
        writers9.add("Erin Cressida Wilson");

        ArrayList<String> actors9 = new ArrayList<>();
        actors9.add("Rachel Zegler");
        actors9.add("Gal Gadot");
        actors9.add("Andrew Burnap");
        actors9.add("Ansu Kabia");

        Cast movie9 = new Cast(actors9, "movie9", directors9, writers9);

        moviesRef.child("movie3").setValue(movie3);
        moviesRef.child("movie4").setValue(movie4);
        moviesRef.child("movie5").setValue(movie5);
        moviesRef.child("movie6").setValue(movie6);
        moviesRef.child("movie7").setValue(movie7);
        moviesRef.child("movie8").setValue(movie8);
        moviesRef.child("movie9").setValue(movie9);
    }
}