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
import java.util.List;

public class VehiclesFragment extends Fragment {

    private List<Vehicle> vehicleList;
    private VehicleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vehicles_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        vehicleList = new ArrayList<>();
        vehicleList.add(new Vehicle(
                "Firmówka",
                "7.4l/100km",
                "Sprawdź przejechane trasy",
                R.drawable.car1
        ));
        vehicleList.add(new Vehicle(
                "Auto żony",
                "6.7l/100km",
                "Sprawdź przejechane trasy",
                R.drawable.car1
        ));

        adapter = new VehicleAdapter(vehicleList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
