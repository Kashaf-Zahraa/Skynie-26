package com.example.skynie.views;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.fragments.AccountFragment;
import com.example.skynie.fragments.MyTicketsFragment;
import com.example.skynie.fragments.SearchFragment;
import com.example.skynie.models.Movie;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference moviesRef;

    // ── Home content views ────────────────────────────────────────
    private View homeContent;
    private View loadingOverlay;  // ✅ ADDED
    private ViewPager2 vpFeaturedBanner;
    private TextView tvFeaturedTitle, tvFeaturedMeta;
    private LinearLayout dotsContainer;
    private List<Movie> featuredMovies  = new ArrayList<>();
    private int currentFeaturedIndex    = 0;
    private Handler autoSwipeHandler    = new Handler();
    private Runnable autoSwipeRunnable;

    private ImageView imgOurPickBanner;
    private TextView tvOurPickTitle, tvOurPickGenreTags, tvOurPickRating;
    private List<Movie> ourPickMovies   = new ArrayList<>();
    private int currentOurPickIndex     = 0;

    private LinearLayout llNowShowingCards;
    private LinearLayout llComingSoonCards;

    // Navigation Drawer
    private DrawerLayout drawerLayout;

    // ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        moviesRef = FirebaseDatabase.getInstance().getReference("movies");

        initViews();
        setupBottomNavigation();
        setupClickListeners();
        setupDrawerItems();

        // ✅ Pehle content hide karo, spinner dikhao
        if (homeContent != null) homeContent.setVisibility(View.GONE);
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        seedThenLoad();
    }

    // ─────────────────────────────────────────────────────────────
    private void initViews() {
        homeContent        = findViewById(R.id.nestedScrollView);
        loadingOverlay     = findViewById(R.id.loadingOverlay);  // ✅ ADDED
        vpFeaturedBanner   = findViewById(R.id.vpFeaturedBanner);
        tvFeaturedTitle    = findViewById(R.id.tvFeaturedTitle);
        tvFeaturedMeta     = findViewById(R.id.tvFeaturedMeta);
        dotsContainer      = findViewById(R.id.dotsContainer);

        imgOurPickBanner   = findViewById(R.id.imgOurPickBanner);
        tvOurPickTitle     = findViewById(R.id.tvOurPickTitle);
        tvOurPickGenreTags = findViewById(R.id.tvOurPickGenreTags);
        tvOurPickRating    = findViewById(R.id.tvOurPickRating);

        llNowShowingCards  = findViewById(R.id.llNowShowingCards);
        llComingSoonCards  = findViewById(R.id.llComingSoonCards);

        drawerLayout = findViewById(R.id.drawerLayout);
    }

    // ─────────────────────────────────────────────────────────────
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showHomeContent();
                return true;
            } else if (id == R.id.nav_search) {
                loadFragment(new SearchFragment());
                return true;
            } else if (id == R.id.nav_tickets) {
                loadFragment(new MyTicketsFragment());
                return true;
            } else if (id == R.id.nav_account) {
                loadFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void loadFragment(Fragment fragment) {
        if (homeContent != null) homeContent.setVisibility(View.GONE);
        View container = findViewById(R.id.fragment_container);
        if (container != null) container.setVisibility(View.VISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void showHomeContent() {
        View container = findViewById(R.id.fragment_container);
        if (container != null) container.setVisibility(View.GONE);
        if (homeContent != null) homeContent.setVisibility(View.VISIBLE);
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current != null) {
            getSupportFragmentManager().beginTransaction().remove(current).commit();
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void setupDrawerItems() {
        LinearLayout drawerHome = findViewById(R.id.drawerHome);
        if (drawerHome != null) {
            drawerHome.setOnClickListener(v -> {
                showHomeContent();
                if (drawerLayout != null) drawerLayout.closeDrawers();
                BottomNavigationView bn = findViewById(R.id.bottomNavigationView);
                if (bn != null) bn.setSelectedItemId(R.id.nav_home);
            });
        }

        LinearLayout drawerSearch = findViewById(R.id.drawerSearch);
        if (drawerSearch != null) {
            drawerSearch.setOnClickListener(v -> {
                loadFragment(new SearchFragment());
                if (drawerLayout != null) drawerLayout.closeDrawers();
                BottomNavigationView bn = findViewById(R.id.bottomNavigationView);
                if (bn != null) bn.setSelectedItemId(R.id.nav_search);
            });
        }

        LinearLayout drawerTickets = findViewById(R.id.drawerTickets);
        if (drawerTickets != null) {
            drawerTickets.setOnClickListener(v -> {
                loadFragment(new MyTicketsFragment());
                if (drawerLayout != null) drawerLayout.closeDrawers();
                BottomNavigationView bn = findViewById(R.id.bottomNavigationView);
                if (bn != null) bn.setSelectedItemId(R.id.nav_tickets);
            });
        }

        LinearLayout drawerProfile = findViewById(R.id.drawerProfile);
        if (drawerProfile != null) {
            drawerProfile.setOnClickListener(v -> {
                loadFragment(new AccountFragment());
                if (drawerLayout != null) drawerLayout.closeDrawers();
                BottomNavigationView bn = findViewById(R.id.bottomNavigationView);
                if (bn != null) bn.setSelectedItemId(R.id.nav_account);
            });
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ UPDATED seedThenLoad
    private void seedThenLoad() {
        moviesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    addSampleMovies();
                    new Handler().postDelayed(() -> loadMoviesThenShow(), 1500);
                } else {
                    loadMoviesThenShow();
                }
            }
            @Override public void onCancelled(DatabaseError error) {
                hideLoadingAndShow();
            }
        });
    }

    // ✅ NEW METHOD - loadMoviesThenShow
    private void loadMoviesThenShow() {
        final int[] pending = {2};

        moviesRef.orderByChild("is_now_showing").equalTo("true")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snapshot) {
                        llNowShowingCards.removeAllViews();
                        featuredMovies.clear();
                        ourPickMovies.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            Movie movie = data.getValue(Movie.class);
                            if (movie != null) {
                                featuredMovies.add(movie);
                                ourPickMovies.add(movie);
                            }
                        }

                        if (!featuredMovies.isEmpty()) setupViewPagerBanner();
                        if (!ourPickMovies.isEmpty()) {
                            currentOurPickIndex = 0;
                            displayOurPick(ourPickMovies.get(0));
                            setupOurPickSwipe();
                        }
                        for (Movie m : featuredMovies) addNowShowingCard(m);

                        if (--pending[0] == 0) hideLoadingAndShow();
                    }
                    @Override public void onCancelled(DatabaseError error) {
                        if (--pending[0] == 0) hideLoadingAndShow();
                    }
                });

        moviesRef.orderByChild("is_coming_soon").equalTo("true")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snapshot) {
                        llComingSoonCards.removeAllViews();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Movie movie = data.getValue(Movie.class);
                            if (movie != null) addComingSoonCard(movie);
                        }
                        if (--pending[0] == 0) hideLoadingAndShow();
                    }
                    @Override public void onCancelled(DatabaseError error) {
                        if (--pending[0] == 0) hideLoadingAndShow();
                    }
                });
    }

    // ✅ NEW METHOD - hideLoadingAndShow
    private void hideLoadingAndShow() {
        runOnUiThread(() -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            if (homeContent != null) homeContent.setVisibility(View.VISIBLE);
        });
    }

    private void addSampleMovies() {
        Movie m1 = new Movie("movie1", "Arrival",
                "A linguist works with the military to communicate with alien visitors.",
                "arrival_poster", "arrival_banner", "https://www.youtube.com/watch?v=example1",
                8.2f, 116, "English", 1478736000000L, "PG-13", "true", "false");

        Movie m2 = new Movie("movie2", "Dune: Part Two",
                "Paul Atreides unites with Chani and the Fremen.",
                "dune_poster", "dune_banner", "https://www.youtube.com/watch?v=example2",
                8.7f, 166, "English", 1708387200000L, "PG-13", "true", "false");

        Movie m6 = new Movie("movie6", "Joker 2",
                "Joker/Arthur Fleck awaits trial at Arkham.",
                "joker_poster", "joker_banner", "https://www.youtube.com/watch?v=example6",
                6.0f, 138, "English", 1728000000000L, "R", "true", "false");

        Movie m7 = new Movie("movie7", "Kraven the Hunter",
                "Kraven Kravinoff's complicated relationship with his father.",
                "kraven_poster", "kraven_banner", "https://www.youtube.com/watch?v=example7",
                5.2f, 127, "English", 1733961600000L, "R", "true", "false");

        Movie m8 = new Movie("movie8", "Mickey 17",
                "An expendable employee colonizes an ice world.",
                "mickey_poster", "mickey_banner", "https://www.youtube.com/watch?v=example8",
                7.1f, 137, "English", 1741046400000L, "PG-13", "true", "false");

        Movie m3 = new Movie("movie3", "Inside Out 2",
                "New emotions enter Headquarters.",
                "inside_out_poster", "inside_out_banner", "https://www.youtube.com/watch?v=example3",
                8.5f, 100, "English", 1718150400000L, "PG", "false", "true");

        Movie m4 = new Movie("movie4", "Deadpool & Wolverine",
                "Wade Wilson teams up with Wolverine.",
                "deadpool_poster", "deadpool_banner", "https://www.youtube.com/watch?v=example4",
                8.9f, 127, "English", 1722470400000L, "R", "false", "true");

        Movie m5 = new Movie("movie5", "Gladiator II",
                "Follows Lucius as he enters the Colosseum.",
                "gladiator_poster", "gladiator_banner", "https://www.youtube.com/watch?v=example5",
                0.0f, 150, "English", 1732492800000L, "R", "false", "true");

        Movie m9 = new Movie("movie9", "Snow White",
                "Snow White and the seven dwarfs reimagined.",
                "snowwhite_poster", "snowwhite_banner", "https://www.youtube.com/watch?v=example9",
                5.3f, 109, "English", 1742256000000L, "PG", "false", "true");

        moviesRef.child("movie1").setValue(m1);
        moviesRef.child("movie2").setValue(m2);
        moviesRef.child("movie3").setValue(m3);
        moviesRef.child("movie4").setValue(m4);
        moviesRef.child("movie5").setValue(m5);
        moviesRef.child("movie6").setValue(m6);
        moviesRef.child("movie7").setValue(m7);
        moviesRef.child("movie8").setValue(m8);
        moviesRef.child("movie9").setValue(m9);
    }

    // ════════════════════════════════════════════════════
    //  VIEWPAGER BANNER
    // ════════════════════════════════════════════════════
    private void setupViewPagerBanner() {
        BannerPagerAdapter adapter = new BannerPagerAdapter(featuredMovies);
        vpFeaturedBanner.setAdapter(adapter);
        updateBannerOverlay(featuredMovies.get(0));
        buildDots(featuredMovies.size());

        vpFeaturedBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                currentFeaturedIndex = position;
                updateBannerOverlay(featuredMovies.get(position));
                updateDots(position);
                resetAutoSwipe();
            }
        });
        startAutoSwipe();
    }

    private void updateBannerOverlay(Movie movie) {
        tvFeaturedTitle.setText(movie.title);
        tvFeaturedMeta.setText(movie.rating + " ★ • " + movie.duration_minutes + " min • " + movie.language);
    }

    private void startAutoSwipe() {
        autoSwipeRunnable = () -> {
            if (featuredMovies.isEmpty()) return;
            int next = (vpFeaturedBanner.getCurrentItem() + 1) % featuredMovies.size();
            vpFeaturedBanner.setCurrentItem(next, true);
            autoSwipeHandler.postDelayed(autoSwipeRunnable, 4000);
        };
        autoSwipeHandler.postDelayed(autoSwipeRunnable, 4000);
    }

    private void resetAutoSwipe() {
        autoSwipeHandler.removeCallbacks(autoSwipeRunnable);
        autoSwipeHandler.postDelayed(autoSwipeRunnable, 4000);
    }

    private void buildDots(int count) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    i == 0 ? dpToPx(16) : dpToPx(6), dpToPx(6));
            lp.setMarginEnd(dpToPx(4));
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    private void updateDots(int activeIndex) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            ViewGroup.LayoutParams lp = dot.getLayoutParams();
            lp.width = (i == activeIndex) ? dpToPx(16) : dpToPx(6);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(i == activeIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private class BannerPagerAdapter extends RecyclerView.Adapter<BannerPagerAdapter.BannerVH> {
        private final List<Movie> movies;
        BannerPagerAdapter(List<Movie> movies) { this.movies = movies; }

        @NonNull @Override
        public BannerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new BannerVH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerVH holder, int position) {
            Movie movie = movies.get(position);
            int resId = getDrawableId(movie.backdrop_drawable);
            holder.iv.setImageResource(resId != 0 ? resId : R.color.dark_gray);
            holder.iv.setOnClickListener(v -> navigateToFilmDetails(movie));
        }

        @Override public int getItemCount() { return movies.size(); }

        class BannerVH extends RecyclerView.ViewHolder {
            ImageView iv;
            BannerVH(ImageView iv) { super(iv); this.iv = iv; }
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void setupOurPickSwipe() {
        android.view.GestureDetector gd = new android.view.GestureDetector(this,
                new android.view.GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(android.view.MotionEvent e1,
                                           android.view.MotionEvent e2, float vX, float vY) {
                        float dX = e2.getX() - e1.getX();
                        if (Math.abs(dX) > 80) {
                            if (dX < 0)
                                currentOurPickIndex = (currentOurPickIndex + 1) % ourPickMovies.size();
                            else
                                currentOurPickIndex = (currentOurPickIndex - 1 + ourPickMovies.size()) % ourPickMovies.size();
                            displayOurPick(ourPickMovies.get(currentOurPickIndex));
                            return true;
                        }
                        return false;
                    }
                    @Override public boolean onDown(android.view.MotionEvent e) { return true; }
                });
        imgOurPickBanner.setOnTouchListener((v, event) -> { gd.onTouchEvent(event); return true; });
    }

    private void filterOurPickByGenre(String genre) {
        List<Movie> filtered = new ArrayList<>();
        for (Movie m : featuredMovies)
            if (getGenreForMovie(m.title).toLowerCase().contains(genre.toLowerCase()))
                filtered.add(m);
        if (filtered.isEmpty()) filtered.addAll(featuredMovies);
        ourPickMovies.clear();
        ourPickMovies.addAll(filtered);
        currentOurPickIndex = 0;
        if (!ourPickMovies.isEmpty()) displayOurPick(ourPickMovies.get(0));
    }

    private void displayOurPick(Movie movie) {
        int id = getDrawableId(movie.backdrop_drawable);
        imgOurPickBanner.setImageResource(id != 0 ? id : R.color.dark_gray);
        tvOurPickTitle.setText(movie.title);
        tvOurPickGenreTags.setText("🔥 " + getGenreForMovie(movie.title));
        Typeface poppinsBold = ResourcesCompat.getFont(this, R.font.poppins_bold);
        if (poppinsBold != null) tvOurPickRating.setTypeface(poppinsBold);
        tvOurPickRating.setText(movie.rating + "/10  IMDb");
    }

    private String getGenreForMovie(String title) {
        switch (title) {
            case "Arrival":              return "Sci-Fi • Drama";
            case "Dune: Part Two":       return "Adventure • Sci-Fi";
            case "Inside Out 2":         return "Comedy • Adventure";
            case "Deadpool & Wolverine": return "Action • Comedy";
            case "Joker 2":             return "Crime • Drama";
            case "Kraven the Hunter":   return "Action • Adventure";
            case "Mickey 17":           return "Sci-Fi • Comedy";
            case "Snow White":          return "Fantasy • Adventure";
            default:                    return "Action • Adventure";
        }
    }

    private int getDrawableId(String name) {
        if (name == null || name.isEmpty()) return R.color.dark_gray;
        if (name.contains(".")) name = name.substring(0, name.lastIndexOf("."));
        int resId = getResources().getIdentifier(name, "drawable", getPackageName());
        return resId != 0 ? resId : R.color.dark_gray;
    }

    // ─────────────────────────────────────────────────────────────
    private void addNowShowingCard(Movie movie) {
        View card = getLayoutInflater().inflate(R.layout.item_movie_card, llNowShowingCards, false);
        ImageView iv = card.findViewById(R.id.ivMoviePoster);
        int id = getDrawableId(movie.poster_drawable);
        iv.setImageResource(id != 0 ? id : R.color.dark_gray);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(v -> navigateToFilmDetails(movie));
        llNowShowingCards.addView(card);
    }

    private void addComingSoonCard(Movie movie) {
        View card = getLayoutInflater().inflate(R.layout.item_coming_soon_card, llComingSoonCards, false);
        ImageView iv = card.findViewById(R.id.ivPoster);
        int id = getDrawableId(movie.poster_drawable);
        iv.setImageResource(id != 0 ? id : R.color.dark_gray);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(v -> navigateToFilmDetails(movie));
        llComingSoonCards.addView(card);
    }

    // ─────────────────────────────────────────────────────────────
    private void setupClickListeners() {
        ImageButton btnSearch = findViewById(R.id.btnSearch);
       // ImageButton btnSearch = findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MovieSearchActivity.class);
                startActivity(intent);
            });
        }

        TextView tvMoreNowShowing = findViewById(R.id.tvMoreNowShowing);
        if (tvMoreNowShowing != null)
            tvMoreNowShowing.setOnClickListener(v -> {
                Intent i = new Intent(this, AllMoviesActivity.class);
                i.putExtra("section", "now_showing");
                startActivity(i);
            });

        TextView tvMoreComingSoon = findViewById(R.id.tvMoreComingSoon);
        if (tvMoreComingSoon != null)
            tvMoreComingSoon.setOnClickListener(v -> {
                Intent i = new Intent(this, AllMoviesActivity.class);
                i.putExtra("section", "coming_soon");
                startActivity(i);
            });

        LinearLayout chipAdventure = findViewById(R.id.chipAdventure);
        if (chipAdventure != null) chipAdventure.setOnClickListener(v -> filterOurPickByGenre("Adventure"));
        LinearLayout chipComedy = findViewById(R.id.chipComedy);
        if (chipComedy != null) chipComedy.setOnClickListener(v -> filterOurPickByGenre("Comedy"));
        LinearLayout chipRomance = findViewById(R.id.chipRomance);
        if (chipRomance != null) chipRomance.setOnClickListener(v -> filterOurPickByGenre("Romance"));
        LinearLayout chipAction = findViewById(R.id.chipAction);
        if (chipAction != null) chipAction.setOnClickListener(v -> filterOurPickByGenre("Action"));

        TextView tvAllGenres = findViewById(R.id.tvAllGenres);
        if (tvAllGenres != null) tvAllGenres.setOnClickListener(v -> {});

        // MENU BUTTON - Opens Drawer
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        ImageButton btnAddWishlist = findViewById(R.id.btnAddWishlist);
        if (btnAddWishlist != null) btnAddWishlist.setOnClickListener(v -> {});
        ImageButton btnInfo = findViewById(R.id.btnInfo);
        if (btnInfo != null) btnInfo.setOnClickListener(v -> {});

        // TRAILER BUTTON
        LinearLayout btnTrailer = findViewById(R.id.btnTrailer);
        if (btnTrailer != null) {
            btnTrailer.setOnClickListener(v -> {
                if (featuredMovies.isEmpty()) {
                    Toast.makeText(this, "No movie selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                Movie currentMovie = featuredMovies.get(currentFeaturedIndex);

                if (currentMovie.trailer_url == null || currentMovie.trailer_url.isEmpty()) {
                    Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(this, TrailerActivity.class);
                intent.putExtra("movie_id",       currentMovie.id);
                intent.putExtra("movie_title",    currentMovie.title);
                intent.putExtra("trailer_url",    currentMovie.trailer_url);
                intent.putExtra("movie_poster",   currentMovie.poster_drawable);
                intent.putExtra("pg_rating",      currentMovie.pg_rating);
                intent.putExtra("language",       currentMovie.language);
                intent.putExtra("movie_duration", currentMovie.duration_minutes);
                startActivity(intent);
            });
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void navigateToFilmDetails(Movie movie) {
        Intent intent = new Intent(this, FilmDetailsActivity.class);
        intent.putExtra("movie_id",          movie.id);
        intent.putExtra("movie_title",       movie.title);
        intent.putExtra("movie_poster",      movie.poster_drawable);
        intent.putExtra("movie_backdrop",    movie.backdrop_drawable);
        intent.putExtra("movie_rating",      movie.rating);
        intent.putExtra("movie_duration",    movie.duration_minutes);
        intent.putExtra("movie_description", movie.description);
        intent.putExtra("trailer_url",       movie.trailer_url);
        intent.putExtra("pg_rating",         movie.pg_rating);
        intent.putExtra("language",          movie.language);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoSwipeRunnable != null)
            autoSwipeHandler.removeCallbacks(autoSwipeRunnable);
    }
}