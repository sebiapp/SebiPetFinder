package com.riberadeltajo.sebipetfinder.ui.AnimalesPerdidos;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;
import com.riberadeltajo.sebipetfinder.databinding.FragmentHomeBinding;
import com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados.Mascota;
import com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados.MascotaAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GalleryFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Spinner spinnerCities;
    private RecyclerView recyclerView;
    private MascotaAdapter mascotaAdapter;
    private Map<String, String> cityToCoordinates = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinnerCities = root.findViewById(R.id.spinnerCities);
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columnas

        mascotaAdapter = new MascotaAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(mascotaAdapter);

        loadCities();
        loadMascotas("");

        spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) parent.getItemAtPosition(position);
                if (selectedCity.equals("Todas las ciudades")) {
                    loadMascotas(""); //Mostrar todas las mascotas
                } else {
                    loadMascotas(selectedCity); //Filtrar por ciudad
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    private void loadCities() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<String>> call = apiService.getCitiesEncontradas();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> coordinates = response.body();
                    List<String> cities = new ArrayList<>();
                    cities.add( "Todas las ciudades");

                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                    for (String coordenada : coordinates) {
                        try {
                            String[] latLng = coordenada.split(",");
                            double latitude = Double.parseDouble(latLng[0]);
                            double longitude = Double.parseDouble(latLng[1]);

                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (!addresses.isEmpty() && addresses.get(0).getLocality() != null) {
                                String cityName = addresses.get(0).getLocality();
                                cityToCoordinates.put(cityName, coordenada); //Guarda la relación
                                if (!cities.contains(cityName)) {
                                    cities.add(cityName);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Geocoding", "Error con coordenadas " + coordenada + ": " + e.getMessage());
                        }
                    }


                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            cities
                    );
                    spinnerCities.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No se pudieron recuperar las ciudades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMascotas(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<Mascota>> call = apiService.getMascotasEncontradas();

        call.enqueue(new Callback<List<Mascota>>() {
            @Override
            public void onResponse(Call<List<Mascota>> call, Response<List<Mascota>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Mascota> todasLasMascotas = response.body();

                    if (cityName.isEmpty() || cityName.equals("Todas las ciudades")) {
                        mascotaAdapter.updateMascotas(todasLasMascotas);
                    } else {
                        List<Mascota> mascotasFiltradas = new ArrayList<>();
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                        for (Mascota mascota : todasLasMascotas) {
                            try {
                                String[] coordenadas = mascota.getCiudad().split(",");
                                double latitude = Double.parseDouble(coordenadas[0]);
                                double longitude = Double.parseDouble(coordenadas[1]);

                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (!addresses.isEmpty() && addresses.get(0).getLocality() != null) {
                                    String mascotaCityName = addresses.get(0).getLocality();
                                    if (mascotaCityName.equals(cityName)) {
                                        mascotasFiltradas.add(mascota);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("Geocoding", "Error al convertir coordenadas: " + e.getMessage());
                            }
                        }

                        mascotaAdapter.updateMascotas(mascotasFiltradas);
                    }
                } else {
                    Toast.makeText(getContext(), "No se pudieron recuperar las mascotas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Mascota>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String selectedCity = (String) spinnerCities.getSelectedItem();
        if (selectedCity == null || selectedCity.equals("Todas las ciudades")) {
            loadMascotas(""); //Cargar todas las mascotas si no hay ciudad seleccionada
        } else {
            loadMascotas(selectedCity); //Cargar mascotas filtradas
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        recyclerView = null;
        spinnerCities = null;
    }
}
