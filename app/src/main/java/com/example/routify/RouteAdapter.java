package com.example.routify;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;
public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {
    private List<Route> routeList;
    private List<Vehicle> availableVehicles;
    public RouteAdapter(List<Route> routeList) {
        this.routeList = routeList;
    }
    public void setVehicles(List<Vehicle> vehicles) {
        this.availableVehicles = vehicles;
        notifyDataSetChanged();
    }
    public void setRoutes(List<Route> routes) {
        this.routeList = routes;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routeList.get(position);

        holder.tvDistance.setText(String.format(Locale.US, "%.2f km", route.getDistance()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(route.getTimestamp()));
        holder.tvDate.setText(dateString);

        if (route.getVehicleName() != null && !route.getVehicleName().isEmpty()) {
            holder.tvVehicleName.setText(route.getVehicleName());
            holder.tvConsumption.setText(String.format(Locale.US, "(%.1f l)", route.fuelUsed));
        } else {
            holder.tvVehicleName.setText("Wybierz auto...");
            holder.tvConsumption.setText("(--- l)");
        }
        holder.tvVehicleName.setOnClickListener(v -> {
            showVehicleSelectionDialog(holder.itemView.getContext(), route);
        });

        holder.tvRouteTitle.setText(route.getTitle() != null ? route.getTitle() : "Automatyczna rejestracja #" + route.getId());

        holder.tvRouteTitle.setOnClickListener(v -> {
            showEditDialog(holder.itemView.getContext(), route);
        });
    }

    @Override
    public int getItemCount() {
        return routeList != null ? routeList.size() : 0;
    }


    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteTitle, tvDistance, tvConsumption, tvVehicleName, tvDate;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteTitle = itemView.findViewById(R.id.tvRouteTitle);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvConsumption = itemView.findViewById(R.id.tvConsumption);
            tvVehicleName = itemView.findViewById(R.id.tvVehicleName);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
    private void showEditDialog(Context context, Route route) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Zmień nazwę trasy");

        final EditText input = new EditText(context);
        input.setText(route.getTitle());
        builder.setView(input);

        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String newName = input.getText().toString();

            route.setTitle(newName);
            new Thread(() -> {
                AppDatabase.getInstance(context).routeDao().updateTitle(route.getId(), newName);
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        notifyDataSetChanged();
                    });
                }
            }).start();
        });


        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    private void showVehicleSelectionDialog(Context context, Route route) {
        if (availableVehicles == null || availableVehicles.isEmpty()) {
            Toast.makeText(context, "Najpierw dodaj auta w ustawieniach!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] carNames = new String[availableVehicles.size()];
        for (int i = 0; i < availableVehicles.size(); i++) {
            carNames[i] = availableVehicles.get(i).getTitle() + " (" + availableVehicles.get(i).getConsumption() + " l/100km)";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Którym autem jechałeś?");
        builder.setItems(carNames, (dialog, which) -> {
            Vehicle selected = availableVehicles.get(which);

            double distance = route.getDistance();
            double consumptionPer100 = selected.getConsumption();
            double fuelResult = (distance * consumptionPer100) / 100.0;

            route.setVehicleId(selected.id);
            route.setVehicleName(selected.getTitle());
            route.fuelUsed = fuelResult;

            new Thread(() -> {
                AppDatabase.getInstance(context).routeDao().updateRouteVehicle(
                        route.getId(),
                        route.getVehicleId(),
                        route.getVehicleName(),
                        route.fuelUsed
                );

                ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
            }).start();
        });
        builder.show();
    }
}