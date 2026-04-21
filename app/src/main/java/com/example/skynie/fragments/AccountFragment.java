package com.example.skynie.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.skynie.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AccountFragment extends Fragment {

    ImageView ivProfile;
    TextView tvName;
    TextView tvEmail;
    LinearLayout rowPersonalInfo;
    LinearLayout rowLanguage;
    LinearLayout rowPrivacyPolicy;
    LinearLayout rowSetting;
    LinearLayout rowHelpCenter;
    LinearLayout rowLogOut;
    SwitchMaterial switchPush;
    SwitchMaterial switchPromo;

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

}