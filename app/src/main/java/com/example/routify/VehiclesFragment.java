package com.example.routify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.InputType;

public class VehiclesFragment extends Fragment {

    private VehicleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vehicles_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VehicleAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        View btnAdd = view.findViewById(R.id.fabAddVehicle);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showAddVehicleDialog());
        }

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDatabase.getInstance(requireContext()).vehicleDao().getAllVehicles()
                .observe(getViewLifecycleOwner(), vehicles -> {
                    if (vehicles != null) {
                        adapter.setVehicles(vehicles);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddVehicleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Dodaj nowy pojazd");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputTitle = new EditText(requireContext());
        inputTitle.setHint("Nazwa auta (np. Audi A4)");
        layout.addView(inputTitle);

        final EditText inputConsumption = new EditText(requireContext());
        inputConsumption.setHint("Spalanie l/100km (np. 7.5)");
        inputConsumption.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputConsumption);

        builder.setView(layout);

        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String title = inputTitle.getText().toString();
            String consumptionStr = inputConsumption.getText().toString();

            if (!title.isEmpty() && !consumptionStr.isEmpty()) {
                try {
                    double consumption = Double.parseDouble(consumptionStr.replace(",", "."));

                    new Thread(() -> {
                        Vehicle newVehicle = new Vehicle(title, consumption, "Szczegóły", R.drawable.ic_launcher_background);
                        AppDatabase.getInstance(requireContext()).vehicleDao().insert(newVehicle);
                    }).start();

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Błędny format spalania!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());
        builder.show();
    }

}
