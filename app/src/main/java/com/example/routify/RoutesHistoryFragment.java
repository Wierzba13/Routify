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

public class RoutesHistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private RouteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.routes_history_fragment, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RouteAdapter(new ArrayList<>());
        rvHistory.setAdapter(adapter);

        AppDatabase.getInstance(requireContext()).routeDao().getAllRoutes()
                .observe(getViewLifecycleOwner(), routes -> {
                    if (routes != null) {
                        adapter.setRoutes(routes);
                        adapter.notifyDataSetChanged();
                    }
                });
        AppDatabase.getInstance(requireContext()).vehicleDao().getAllVehicles()
                .observe(getViewLifecycleOwner(), vehicles -> {
                    adapter.setVehicles(vehicles);
                });
    }
}