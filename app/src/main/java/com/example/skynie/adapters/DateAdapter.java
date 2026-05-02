package com.example.skynie.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skynie.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private Context context;
    private List<Date> dates = new ArrayList<>();
    private int selectedPosition = 0; //initailly aaj ki date
    private OnDateChangeListener dateChangeListener;

    public interface OnDateChangeListener {
        void onDateChanged();
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        this.dateChangeListener = listener;
    }

    public DateAdapter(Context context) {
        this.context = context;
        generateDates();
    }

    private void generateDates() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        for (int i = 0; i < 7; i++) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_selector, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        Date date = dates.get(position);
        int pos=position;

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);

        holder.tvDayName.setText(dayFormat.format(date).toUpperCase());
        holder.tvDayNumber.setText(dateFormat.format(date));

        if (position == selectedPosition) {
            holder.dayCard.setBackgroundResource(R.drawable.circle_red_bg);
        } else {
            holder.dayCard.setBackgroundResource(R.drawable.circle_transparent_bg);
        }

        holder.dayCard.setOnClickListener((v)->{
            int oldPosition = selectedPosition;
            selectedPosition = pos;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);

            if (dateChangeListener != null) {
                dateChangeListener.onDateChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return dates.size();
    }
    public int getSelectedPosition() {
        return selectedPosition; //index
    }

    public Date getSelectedDate() {
        return dates.get(selectedPosition); //actual date
    }

    public String getSelectedDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return sdf.format(dates.get(selectedPosition)); //date format
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;
        LinearLayout dayCard;
        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            dayCard = itemView.findViewById(R.id.day_card);
        }
    }
}