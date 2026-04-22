package com.example.skynie.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;
import com.example.skynie.models.Cinema;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private DatabaseReference cinemasRef;
    private FusedLocationProviderClient fusedLocationClient;

    private RecyclerView rvCinemas;
    private EditText etSearch;

    private CinemaAdapter adapter;
    private final List<Cinema> allCinemas   = new ArrayList<>();
    private final List<Cinema> shownCinemas = new ArrayList<>();

    private double userLat = 0, userLng = 0;
    private String activeChip = "Nearby";

    // ─────────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_search.xml use karo (activity_search.xml NAHI)
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cinemasRef          = FirebaseDatabase.getInstance().getReference("cinemas");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Views
        rvCinemas = view.findViewById(R.id.rvCinemas);
        etSearch  = view.findViewById(R.id.etSearch);
        rvCinemas.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CinemaAdapter(shownCinemas);
        rvCinemas.setAdapter(adapter);

        // Filter button
        view.findViewById(R.id.btnFilter).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Filter coming soon", Toast.LENGTH_SHORT).show());

        setupChips(view);
        setupSearchBar();
        seedCinemasIfNeeded();
        requestLocationAndLoad();
    }

    // ─────────────────────────────────────────────────────────────
    //  CHIPS
    // ─────────────────────────────────────────────────────────────
    private void setupChips(View root) {
        int[] chipIds = {
                R.id.chipNearby, R.id.chipImax, R.id.chipGold,
                R.id.chipScreenX, R.id.chip4DMax
        };
        String[] labels = {"Nearby", "IMAX", "GOLD", "ScreenX", "4D MAX"};

        for (int i = 0; i < labels.length; i++) {
            final String label = labels[i];
            TextView chip = root.findViewById(chipIds[i]);
            if (chip == null) continue;
            chip.setOnClickListener(v -> {
                activeChip = label;
                highlightChip(label);
                applyFilters();
            });
        }
    }

    private void highlightChip(String selected) {
        if (getView() == null) return;
        int[] ids     = {R.id.chipNearby, R.id.chipImax, R.id.chipGold, R.id.chipScreenX, R.id.chip4DMax};
        String[] lbls = {"Nearby", "IMAX", "GOLD", "ScreenX", "4D MAX"};

        for (int i = 0; i < ids.length; i++) {
            TextView chip = getView().findViewById(ids[i]);
            if (chip == null) continue;
            boolean active = lbls[i].equals(selected);
            chip.setBackgroundResource(active ? R.drawable.badge_red_bg : R.drawable.chip_inactive_bg);
            chip.setTextColor(active ? 0xFFFFFFFF : 0xFFCCCCCC);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH BAR
    // ─────────────────────────────────────────────────────────────
    private void setupSearchBar() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  APPLY FILTER
    // ─────────────────────────────────────────────────────────────
    private void applyFilters() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        shownCinemas.clear();

        for (Cinema c : allCinemas) {
            boolean matchesSearch = query.isEmpty()
                    || c.name.toLowerCase(Locale.ROOT).contains(query)
                    || c.address.toLowerCase(Locale.ROOT).contains(query);
            if (!matchesSearch) continue;

            if (!activeChip.equals("Nearby")) {
                if (c.screen_types == null
                        || !c.screen_types.toUpperCase(Locale.ROOT)
                        .contains(activeChip.replace(" ", "").toUpperCase(Locale.ROOT))) {
                    continue;
                }
            }
            shownCinemas.add(c);
        }

        if (activeChip.equals("Nearby") && userLat != 0) {
            Collections.sort(shownCinemas, (a, b) ->
                    Double.compare(distanceTo(a), distanceTo(b)));
        }

        adapter.notifyDataSetChanged();
    }

    // ─────────────────────────────────────────────────────────────
    //  LOCATION
    // ─────────────────────────────────────────────────────────────
    private void requestLocationAndLoad() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            fetchLocationAndLoad();
        }
    }

    private void fetchLocationAndLoad() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadCinemas();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLng = location.getLongitude();
                    }
                    loadCinemas();
                })
                .addOnFailureListener(e -> loadCinemas());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndLoad();
        } else {
            loadCinemas();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  FIREBASE
    // ─────────────────────────────────────────────────────────────
    private void loadCinemas() {
        cinemasRef.orderByChild("is_active").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allCinemas.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Cinema c = data.getValue(Cinema.class);
                            if (c != null) allCinemas.add(c);
                        }
                        applyFilters();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void seedCinemasIfNeeded() {
        cinemasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) addSampleCinemas();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addSampleCinemas() {
        Cinema[] cinemas = {
                new Cinema("c1", "Stars (90°Mall)",
                        "23 Sunny Boulevard, Sunshine City",
                        24.8607, 67.0011, "IMAX,GOLD,ScreenX,4DMAX", true),
                new Cinema("c2", "FilmHouse (Galaxy Plaza)",
                        "456 Starry Lane, Galaxy Town",
                        24.8700, 67.0100, "IMAX,GOLD", true),
                new Cinema("c3", "ReelMagic (Dreamland Center)",
                        "101 Paradise Parkway, Paradise City",
                        24.8800, 67.0200, "ScreenX,4DMAX", true),
                new Cinema("c4", "StarVista (Cosmo City Mall)",
                        "303 Ocean Drive, OceanView",
                        24.8900, 67.0300, "IMAX", true),
                new Cinema("c5", "FlickPalace (Emerald Plaza)",
                        "505 Skyway Avenue, Skyline District",
                        24.9000, 67.0400, "GOLD,4DMAX", true),
                new Cinema("c6", "CineMax (Metro Hub)",
                        "12 Main Boulevard, Downtown",
                        24.9100, 67.0500, "IMAX,ScreenX", true),
                new Cinema("c7", "Reel Cinema (Clifton)",
                        "88 Sea View Road, Clifton",
                        24.8200, 67.0250, "GOLD,ScreenX", true)
        };
        for (Cinema c : cinemas) cinemasRef.child(c.id).setValue(c);
    }

    // ─────────────────────────────────────────────────────────────
    //  DISTANCE
    // ─────────────────────────────────────────────────────────────
    private double distanceTo(Cinema c) {
        if (userLat == 0 && userLng == 0) return 0;
        float[] result = new float[1];
        Location.distanceBetween(userLat, userLng, c.latitude, c.longitude, result);
        return result[0];
    }

    private String formatDistance(Cinema c) {
        double metres = distanceTo(c);
        if (metres == 0) return "";
        return String.format(Locale.ROOT, "%.1fkm", metres / 1000.0);
    }

    // ─────────────────────────────────────────────────────────────
    //  ADAPTER
    // ─────────────────────────────────────────────────────────────
    private class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.VH> {

        private final List<Cinema> list;
        CinemaAdapter(List<Cinema> list) { this.list = list; }

        @NonNull
        @Override
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

            String dist = formatDistance(c);
            holder.tvDistance.setText(dist);
            holder.tvDistance.setVisibility(dist.isEmpty() ? View.GONE : View.VISIBLE);

            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(requireContext(), c.name, Toast.LENGTH_SHORT).show());
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