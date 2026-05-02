package com.example.skynie.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;

import java.util.ArrayList;
import java.util.List;

public class FormatAdapter extends RecyclerView.Adapter<FormatAdapter.FormatViewHolder> {

    private Context context;
    private List<String> formats = new ArrayList<>();
    private int selectedPosition = 0;
    private OnFormatChangeListener formatChangeListener;

    public interface OnFormatChangeListener {
        void onFormatChanged(String format, int position);
    }

    public FormatAdapter(Context context, OnFormatChangeListener listener) {
        this.context = context;
        this.formatChangeListener = listener;
        generateFormats();
    }

    private void generateFormats() {
        formats.add("ALL");
        formats.add("IMAX");
        formats.add("ScreenX");
        formats.add("4DMAX");
        formats.add("GOLD");
    }

    @NonNull
    @Override
    public FormatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_screen_type, parent, false);
        return new FormatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FormatViewHolder holder, int position) {
        String format = formats.get(position);
        holder.btnFormat.setText(format);
        int pos=position;

        // Set background based on selection
        if (selectedPosition == pos) {
            holder.btnFormat.setBackgroundResource(R.drawable.button_red_bg);
            holder.btnFormat.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.btnFormat.setBackgroundResource(R.drawable.button_grey_bg);
            holder.btnFormat.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }

        holder.btnFormat.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = pos;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);

            if (formatChangeListener != null) {
                formatChangeListener.onFormatChanged(format, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return formats.size();
    }

    public String getSelectedFormat() {
        return formats.get(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public static class FormatViewHolder extends RecyclerView.ViewHolder {
        AppCompatButton btnFormat;

        public FormatViewHolder(@NonNull View itemView) {
            super(itemView);
            btnFormat = itemView.findViewById(R.id.btn_format);
        }
    }
}