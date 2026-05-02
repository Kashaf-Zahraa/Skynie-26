// MovieAdapter.java
package com.example.skynie.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skynie.R;
import com.example.skynie.models.Movie;
import com.example.skynie.views.FilmDetailsActivity;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> movieList;

    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Convert string name to drawable resource ID
        int imageResId = getDrawableResourceId(movie.getPoster_drawable());

        // Load image using Glide
        Glide.with(context)
                .load(imageResId)  // Now using resource ID instead of string
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(holder.ivMoviePoster);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FilmDetailsActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie_title", movie.getTitle());
            intent.putExtra("movie_description", movie.getDescription());
            intent.putExtra("movie_poster", movie.getPoster_drawable());
            intent.putExtra("movie_backdrop", movie.getBackdrop_drawable());
            intent.putExtra("movie_rating", String.valueOf(movie.getRating()));
            intent.putExtra("movie_duration", movie.getDuration_minutes());
            intent.putExtra("trailer_url", movie.getTrailer_url());
            intent.putExtra("language", movie.getLanguage());
            intent.putExtra("release_date", movie.getRelease_date());
            intent.putExtra("pg_rating", movie.getPg_rating());
            intent.putExtra("is_now_showing", movie.getIs_now_showing());
            intent.putExtra("is_coming_soon", movie.getIs_coming_soon());
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    // Helper method to convert string name to drawable resource ID
    private int getDrawableResourceId(String drawableName) {
        if (drawableName == null || drawableName.isEmpty()) {
            return R.drawable.ic_movie_placeholder;
        }

        // Try to get resource ID from drawable name
        int resId = context.getResources().getIdentifier(
                drawableName,
                "drawable",
                context.getPackageName()
        );

        // Return placeholder if resource not found
        return resId != 0 ? resId : R.drawable.ic_movie_placeholder;
    }


    // Method to update data
    public void updateMovies(List<Movie> newMovies) {
        this.movieList = newMovies;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMoviePoster;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.ivMoviePoster);
        }
    }
}