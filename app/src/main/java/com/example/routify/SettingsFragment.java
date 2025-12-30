package com.example.routify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        setupPopupMenu(view.findViewById(R.id.tvDistanceUnit), new String[]{"Kilometry", "Mile"});
        setupPopupMenu(view.findViewById(R.id.tvFuelConsumptionUnit), new String[]{"l/100km", "mpg (US)", "mpg (UK)"});
        setupPopupMenu(view.findViewById(R.id.tvCurrency), new String[]{"PLN", "EUR", "USD", "GBP"});
        setupPopupMenu(view.findViewById(R.id.tvDateFormat), new String[]{"DD.MM.RRRR", "MM/DD/RRRR", "RRRR-MM-DD"});
        setupPopupMenu(view.findViewById(R.id.tvNotificationLeadTime), new String[]{"Przypomnij 1 tydzień przed terminem", "Przypomnij 2 tygodnie przed terminem", "Przypomnij 1 miesiąc przed terminem"});
        setupPopupMenu(view.findViewById(R.id.tvAppTheme), new String[]{"Jasny", "Ciemny", "Systemowy"});
        setupPopupMenu(view.findViewById(R.id.tvStartScreen), new String[]{"pokaż ostatnie trasy", "pokaż mapę", "pokaż listę aut"});
        setupPopupMenu(view.findViewById(R.id.tvCarOrder), new String[]{"po ostatniej aktywności", "alfabetycznie", "po dacie dodania"});

        return view;
    }

    private void setupPopupMenu(View view, String[] options) {
        if (!(view instanceof TextView)) return;
        TextView textView = (TextView) view;

        view.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            for (String option : options) {
                popup.getMenu().add(option);
            }

            popup.setOnMenuItemClickListener(item -> {
                textView.setText(item.getTitle());
                return true;
            });

            popup.show();
        });
    }
}
