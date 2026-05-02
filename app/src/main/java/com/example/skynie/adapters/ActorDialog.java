package com.example.skynie.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.example.skynie.R;

import java.util.List;

public class ActorDialog extends Dialog {
    private List<String> actors;
    private String title;

    public ActorDialog(Context context, List<String> actors, String title) {
        super(context);
        this.actors = actors;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_actors);

        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        TextView tvActorsList = findViewById(R.id.tv_actors_list);
        AppCompatButton btnClose = findViewById(R.id.btn_close);

        tvTitle.setText(title);

        StringBuilder actorsText = new StringBuilder();
        for (int i = 0; i < actors.size(); i++) {
            actorsText.append(actors.get(i));
            if (i < actors.size() - 1) {
                actorsText.append("\n\n"); // Double line break between names
                // Or use "\n" for single line break
            }
        }
        tvActorsList.setText(actorsText.toString());

        btnClose.setOnClickListener(v -> dismiss());
    }
}