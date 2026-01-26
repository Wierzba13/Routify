package com.example.routify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvMonthDistance, tvWeekFuel, tvMonthlyCosts;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupStatistics();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        tvMonthDistance = view.findViewById(R.id.tvMonthDistance);
        tvWeekFuel = view.findViewById(R.id.tvWeekFuel);
        tvMonthlyCosts = view.findViewById(R.id.tvMonthlyCosts);

        return view;
    }

    private void setupStatistics() {
        if (getContext() == null) return;

        RouteDao dao = AppDatabase.getInstance(requireContext()).routeDao();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long monthStart = cal.getTimeInMillis();

        long weekStart = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        dao.getSumDistanceSince(monthStart).observe(getViewLifecycleOwner(), distance -> {
            if (tvMonthDistance != null) {
                double d = (distance != null) ? distance : 0.0;
                tvMonthDistance.setText(String.format(Locale.getDefault(), "%.1f km", d));
            }
        });

        dao.getSumFuelSince(weekStart).observe(getViewLifecycleOwner(), fuel -> {
            if (tvWeekFuel != null) {
                double f = (fuel != null) ? fuel : 0.0;
                tvWeekFuel.setText(String.format(Locale.getDefault(), "%.1f l", f));

                if (tvMonthlyCosts != null) {
                    double fuelPrice = 6.50;
                    tvMonthlyCosts.setText(String.format(Locale.getDefault(), "%.2f PLN", f * fuelPrice));
                }
            }
        });
    }
}