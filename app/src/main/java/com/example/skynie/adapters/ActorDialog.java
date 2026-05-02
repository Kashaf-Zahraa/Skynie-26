// ActorDialog.java
package com.example.skynie.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skynie.R;
import com.example.skynie.adapters.ActorAdapter;
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
        RecyclerView rvActors = findViewById(R.id.rv_dialog_actors);
        AppCompatButton btnClose = findViewById(R.id.btn_close);

        tvTitle.setText(title);

        // Show ALL actors in dialog
        ActorAdapter adapter = new ActorAdapter(actors, null);
        rvActors.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActors.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());
    }
}