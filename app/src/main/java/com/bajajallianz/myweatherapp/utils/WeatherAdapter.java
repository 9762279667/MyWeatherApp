package com.bajajallianz.myweatherapp.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bajajallianz.myweatherapp.R;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyViewHolder> {

    private List<Weather> list;

    public WeatherAdapter(List<Weather> list) {
        this.list = list;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTV, timeTV, descTV, tempTV, tempMinTV, tempMaxTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            dateTV = (TextView) itemView.findViewById(R.id.id_weather_date);
            timeTV = (TextView) itemView.findViewById(R.id.id_weather_time);
            descTV = (TextView) itemView.findViewById(R.id.id_weather_desc);
            tempTV = (TextView) itemView.findViewById(R.id.id_weather_temp);
            tempMinTV = (TextView) itemView.findViewById(R.id.id_weather_temp_min);
            tempMaxTV = (TextView) itemView.findViewById(R.id.id_weather_temp_max);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Weather wh = this.list.get(position);
        holder.dateTV.setText(wh.getDateTime());
        holder.timeTV.setText(wh.getTime());
        holder.descTV.setText(wh.getDescription());
        holder.tempTV.setText(wh.getTemp());
        holder.tempMinTV.setText(wh.getTempMin());
        holder.tempMaxTV.setText(wh.getTempMax());

    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }
}
