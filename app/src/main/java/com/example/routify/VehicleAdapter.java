package com.example.routify;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;
    private final OnVehicleClickListener onVehicleClickListener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    public VehicleAdapter(List<Vehicle> vehicleList, OnVehicleClickListener onVehicleClickListener) {
        this.vehicleList = vehicleList;
        this.onVehicleClickListener = onVehicleClickListener;
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
        holder.bind(vehicle, onVehicleClickListener);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVehicle;
        TextView tvTitle, tvSubtitle;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVehicle = itemView.findViewById(R.id.ivVehicle);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
        }

        public void bind(final Vehicle vehicle, final OnVehicleClickListener onVehicleClickListener) {
            tvTitle.setText(vehicle.getTitle());
            tvSubtitle.setText(String.format(Locale.US, "%.1f l/100km", vehicle.getConsumption()));
            if (vehicle.getImageUri() != null) {
                ivVehicle.setImageURI(Uri.parse(vehicle.getImageUri()));
            }
            itemView.setOnClickListener(v -> onVehicleClickListener.onVehicleClick(vehicle));
        }
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicleList = vehicles;
        notifyDataSetChanged();
    }
}
