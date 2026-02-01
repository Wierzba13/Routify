package com.example.routify;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VehiclesFragment extends Fragment {

    private VehicleAdapter adapter;
    private Uri selectedImageUri;
    private ImageView vehicleImageView;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri uri = result.getData().getData();
                        requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        selectedImageUri = uri;
                        if (vehicleImageView != null) {
                            vehicleImageView.setImageURI(selectedImageUri);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vehicles_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VehicleAdapter(new ArrayList<>(), this::showEditVehicleDialog);
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
        selectedImageUri = null;
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

        Button pickImageButton = new Button(requireContext());
        pickImageButton.setText("Wybierz zdjęcie");
        layout.addView(pickImageButton);

        vehicleImageView = new ImageView(requireContext());
        layout.addView(vehicleImageView);

        pickImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        builder.setView(layout);

        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String title = inputTitle.getText().toString();
            String consumptionStr = inputConsumption.getText().toString();

            if (!title.isEmpty() && !consumptionStr.isEmpty()) {
                try {
                    double consumption = Double.parseDouble(consumptionStr.replace(",", "."));
                    String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : null;

                    new Thread(() -> {
                        Vehicle newVehicle = new Vehicle(title, consumption, imageUriString);
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

    private void showEditVehicleDialog(Vehicle vehicle) {
        selectedImageUri = (vehicle.getImageUri() != null) ? Uri.parse(vehicle.getImageUri()) : null;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edytuj pojazd");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputTitle = new EditText(requireContext());
        inputTitle.setText(vehicle.getTitle());
        layout.addView(inputTitle);

        final EditText inputConsumption = new EditText(requireContext());
        inputConsumption.setText(String.valueOf(vehicle.getConsumption()));
        inputConsumption.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputConsumption);

        Button pickImageButton = new Button(requireContext());
        pickImageButton.setText("Zmień zdjęcie");
        layout.addView(pickImageButton);

        vehicleImageView = new ImageView(requireContext());
        if (selectedImageUri != null) {
            vehicleImageView.setImageURI(selectedImageUri);
        }
        layout.addView(vehicleImageView);

        pickImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        builder.setView(layout);

        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String title = inputTitle.getText().toString();
            String consumptionStr = inputConsumption.getText().toString();

            if (!title.isEmpty() && !consumptionStr.isEmpty()) {
                try {
                    double consumption = Double.parseDouble(consumptionStr.replace(",", "."));
                    String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : null;

                    vehicle.setTitle(title);
                    vehicle.setConsumption(consumption);
                    vehicle.setImageUri(imageUriString);

                    new Thread(() -> {
                        AppDatabase.getInstance(requireContext()).vehicleDao().update(vehicle);
                    }).start();

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Błędny format spalania!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Usuń", (dialog, which) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Potwierdzenie")
                    .setMessage("Czy na pewno chcesz usunąć ten pojazd?")
                    .setPositiveButton("Tak", (d, w) -> {
                        new Thread(() -> {
                            AppDatabase.getInstance(requireContext()).vehicleDao().delete(vehicle);
                        }).start();
                    })
                    .setNegativeButton("Nie", null)
                    .show();
        });

        builder.show();
    }
}
