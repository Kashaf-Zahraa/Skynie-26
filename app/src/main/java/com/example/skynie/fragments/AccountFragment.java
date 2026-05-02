package com.example.skynie.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.skynie.R;
import com.example.skynie.views.AuthActivity;
import com.example.skynie.views.LoginActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    ImageView ivProfile;
    TextView tvName, tvEmail;
    LinearLayout rowPersonalInfo, rowLanguage, rowPrivacyPolicy, rowSetting, rowHelpCenter, rowLogOut;
    SwitchMaterial switchPush, switchPromo;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_account, container, false);
        init(view);
        loadUserData();
        setClickListeners();
        return view;
    }
    private void init(View view){
        ivProfile = view.findViewById(R.id.ivProfile);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        rowPersonalInfo = view.findViewById(R.id.rowPersonalInfo);
        rowLanguage = view.findViewById(R.id.rowLanguage);
        rowPrivacyPolicy = view.findViewById(R.id.rowPrivacyPolicy);
        rowSetting = view.findViewById(R.id.rowSetting);
        rowHelpCenter = view.findViewById(R.id.rowHelpCenter);
        rowLogOut = view.findViewById(R.id.rowLogOut);
        switchPush = view.findViewById(R.id.switchPush);
        switchPromo = view.findViewById(R.id.switchPromo);
    }
    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            if (name != null && !name.isEmpty()) {
                tvName.setText(name);
            } else {
                tvName.setText("User");
            }

            if (email != null && !email.isEmpty()) {
                tvEmail.setText(email);
            }

            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(ivProfile);
            }
        }
    }

    private void setClickListeners() {
        rowLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            requireContext().startActivity(intent);
            requireActivity().finish();
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }
}