package com.example.routify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private final List<Vehicle> vehicleList;

    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.tvTitle.setText(vehicle.getTitle());
        holder.tvSubtitle.setText(vehicle.getConsumption());
        holder.tvLabelBottom.setText(vehicle.getActionText());
        holder.ivVehicle.setImageResource(vehicle.getImageResId());
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVehicle;
        TextView tvTitle, tvSubtitle, tvLabelBottom;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVehicle = itemView.findViewById(R.id.ivVehicle);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvLabelBottom = itemView.findViewById(R.id.tvLabelBottom);
        }
    }
}
