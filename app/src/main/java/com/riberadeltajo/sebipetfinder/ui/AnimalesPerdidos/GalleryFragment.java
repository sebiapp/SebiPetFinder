package com.riberadeltajo.sebipetfinder.ui.AnimalesPerdidos;

import android.os.Bundle;
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
import java.util.List;

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
                    List<String> cities = response.body();
                    cities.add(0, "Todas las ciudades");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, cities);
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

    private void loadMascotas(String city) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<Mascota>> call;
        if (city.isEmpty()) {
            call = apiService.getMascotasEncontradas(); //Obtener todas las mascotas
        } else {
            call = apiService.getMascotasEncontradasByCity(city); //Filtrar por ciudad
        }

        call.enqueue(new Callback<List<Mascota>>() {
            @Override
            public void onResponse(Call<List<Mascota>> call, Response<List<Mascota>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Mascota> mascotas = response.body();
                    mascotaAdapter.updateMascotas(mascotas);
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
