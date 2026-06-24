package com.example.locationtrackingappp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<LocationEntity> locationList;

    /**
     * Constructor with null safety
     */
    public LocationAdapter(List<LocationEntity> locationList) {
        this.locationList = locationList != null ? locationList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationEntity entity = locationList.get(position);
        holder.tvLatLng.setText("Lat: " + entity.latitude + ", Lng: " + entity.longitude);
        holder.tvTime.setText("Time: " + entity.timestamp);
    }

    @Override
    public int getItemCount() {
        return locationList != null ? locationList.size() : 0;
    }

    /**
     * Update the list safely and refresh the RecyclerView
     */
    public void updateList(List<LocationEntity> newList) {
        this.locationList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLatLng, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvLatLng = itemView.findViewById(android.R.id.text1);
            tvTime = itemView.findViewById(android.R.id.text2);
        }
    }
}