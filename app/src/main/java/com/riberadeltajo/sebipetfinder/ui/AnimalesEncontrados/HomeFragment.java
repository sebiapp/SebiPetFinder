package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;
import com.riberadeltajo.sebipetfinder.databinding.FragmentHomeBinding;

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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Spinner spinnerCities;
    private RecyclerView recyclerView;
    private MascotaAdapter mascotaAdapter;
    private Map<String, String> cityToCoordinates = new HashMap<>();
    private Spinner spinnerTipoMascota, spinnerColor, spinnerSexo, spinnerTamano,spinnerRaza;
    private String currentCiudad = "", currentTipo = "", currentColor = "", currentSexo = "", currentTamano = "", currentRaza = "";
    private View root;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        boolean isGoogleLogin = getContext()
                .getSharedPreferences("user_data", Context.MODE_PRIVATE)
                .getBoolean("isGoogleLogin", false);
        if (isGoogleLogin) {
            String username = getContext()
                    .getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    .getString("username", null);

            //Si no tiene nombre de usuario, mostrar el diálogo
            if (username == null || username.isEmpty()) {
                showUsernameDialog();
            }
        }
        checkNotificationPermission();
        spinnerCities = root.findViewById(R.id.spinnerCities);
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); //2 columnas

        mascotaAdapter = new MascotaAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(mascotaAdapter);

        inicializarSpinners();
        loadCities();
        //loadMascotas("");



        return root;
    }
    private void showUsernameDialog() {
        EditText input = new EditText(getContext());
        input.setHint("Nombre de usuario");
        input.setSingleLine(true);
        input.setMaxLines(1);

        new AlertDialog.Builder(getContext())
                .setTitle("Asignar Nombre de Usuario")
                .setMessage("Ya que has iniciado sesión con Google, necesitas asignar un nombre de usuario para tu cuenta:")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String username = input.getText().toString().trim();
                    if (username.isEmpty()) {
                        showUsernameDialog();
                        Toast.makeText(getContext(), "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUsername(username);
                    }
                })
                .show();
    }
    private void updateUsername(String username) {
        int userId = getContext()
                .getSharedPreferences("user_data", Context.MODE_PRIVATE)
                .getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<JsonObject> call = apiService.updateUsername(userId, username);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = response.body();
                    String status = jsonResponse.get("status").getAsString();
                    String message = jsonResponse.get("message").getAsString();

                    if (status.equals("success")) {
                        // Guardar en SharedPreferences
                        getContext()
                                .getSharedPreferences("user_data", Context.MODE_PRIVATE)
                                .edit()
                                .putString("username", username)
                                .apply();
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        showUsernameDialog();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JsonObject errorJson = new Gson().fromJson(errorBody, JsonObject.class);
                        String errorMessage = errorJson.get("message").getAsString();
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al actualizar el nombre de usuario", Toast.LENGTH_SHORT).show();
                    }
                    showUsernameDialog();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
                showUsernameDialog();
            }
        });
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                new AlertDialog.Builder(requireContext())
                        .setTitle("Permisos de Notificaciones")
                        .setMessage("Para recibir notificaciones cuando alguien quiera contactar contigo sobre una mascota, necesitamos tu permiso para enviar notificaciones.")
                        .setPositiveButton("Permitir", (dialog, which) -> {
                            requestPermissionLauncher.launch(
                                    android.Manifest.permission.POST_NOTIFICATIONS
                            );
                        })
                        .setNegativeButton("Ahora no", null)
                        .show();
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(),
                            "¡Gracias! Recibirás notificaciones cuando alguien quiera contactar contigo.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(),
                            "No recibirás notificaciones sobre contactos de tus mascotas.",
                            Toast.LENGTH_LONG).show();
                }
            });
    private void loadCities() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<String>> call = apiService.getCities();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> coordinates = response.body();
                    List<String> cities = new ArrayList<>();
                    cities.add("Todas las ciudades");

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

    private void loadMascotas() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<Mascota>> call = apiService.getMascotas();

        call.enqueue(new Callback<List<Mascota>>() {
            @Override
            public void onResponse(Call<List<Mascota>> call, Response<List<Mascota>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Mascota> todasLasMascotas = response.body();
                    List<Mascota> mascotasFiltradas = new ArrayList<>();

                    for (Mascota mascota : todasLasMascotas) {
                        if (cumpleFiltros(mascota)) {
                            mascotasFiltradas.add(mascota);
                        }
                    }

                    mascotaAdapter.updateMascotas(mascotasFiltradas);
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
    // FILTROS
    private void inicializarSpinners() {
        spinnerCities = root.findViewById(R.id.spinnerCities);
        spinnerTipoMascota = root.findViewById(R.id.spinnerTipoMascota);
        spinnerColor = root.findViewById(R.id.spinnerColor);
        spinnerRaza = root.findViewById(R.id.spinnerRaza);
        spinnerSexo = root.findViewById(R.id.spinnerSexo);
        spinnerTamano = root.findViewById(R.id.spinnerTamano);

        // Configurar adaptadores con valores predefinidos
        String[] tiposMascota = {"Todos los tipos", "Perro", "Gato", "Ave", "Conejo", "Otro"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, tiposMascota);
        spinnerTipoMascota.setAdapter(tipoAdapter);

        String[] colores = {"Todos los colores", "Negro", "Blanco", "Marrón", "Gris", "Naranja",
                "Manchado", "Atigrado", "Otro"};
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, colores);
        spinnerColor.setAdapter(colorAdapter);

        String[] razas = {"Todas las razas"};
        ArrayAdapter<String> razaAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, razas);
        spinnerRaza.setAdapter(razaAdapter);

        String[] sexos = {"Todos los sexos", "Macho", "Hembra", "Desconocido"};
        ArrayAdapter<String> sexoAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, sexos);
        spinnerSexo.setAdapter(sexoAdapter);

        String[] tamanos = {"Todos los tamaños", "Muy pequeño", "Pequeño", "Mediano", "Grande",
                "Muy grande"};
        ArrayAdapter<String> tamanoAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, tamanos);
        spinnerTamano.setAdapter(tamanoAdapter);

        // Configurar listeners
        configurarListenersSpinners();
    }

    private void configurarListenersSpinners() {
        spinnerTipoMascota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTipo = position == 0 ? "" : parent.getItemAtPosition(position).toString();
                actualizarRazasPorTipo(currentTipo);
                aplicarFiltros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentColor = position == 0 ? "" : parent.getItemAtPosition(position).toString();
                aplicarFiltros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerRaza.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRaza = position == 0 ? "" : parent.getItemAtPosition(position).toString();
                aplicarFiltros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerSexo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSexo = position == 0 ? "" : parent.getItemAtPosition(position).toString();
                aplicarFiltros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTamano.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTamano = position == 0 ? "" : parent.getItemAtPosition(position).toString();
                aplicarFiltros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCiudad = parent.getItemAtPosition(position).toString();
                if (currentCiudad.equals("Todas las ciudades")) {
                    currentCiudad = "";
                }
                aplicarFiltros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void actualizarRazasPorTipo(String tipo) {
        ArrayAdapter<String> razaAdapter;
        switch (tipo) {
            case "Perro":
                String[] razasPerro = {"Todas las razas", "Pastor Alemán", "Labrador", "Golden Retriever", "Bulldog", "Chihuahua", "Yorkshire", "Husky", "Otro"};
                razaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, razasPerro);
                break;
            case "Gato":
                String[] razasGato = {"Todas las razas", "Siamés", "Persa", "Angora", "Maine Coon", "Bengalí", "Otro"};
                razaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, razasGato);
                break;
            case "Ave":
                String[] razasAve = {"Todas las razas", "Canario", "Periquito", "Cacatúa", "Otro"};
                razaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, razasAve);
                break;
            case "Conejo":
                String[] razasConejo = {"Todas las razas", "Enano", "Mini Rex", "Angora", "Otro"};
                razaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, razasConejo);
                break;
            default:
                String[] razasOtro = {"Todas las razas", "Otro", "Otro"};
                razaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, razasOtro);
                break;
        }
        spinnerRaza.setAdapter(razaAdapter);
    }

    private void aplicarFiltros() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<Mascota>> call = apiService.getMascotas();

        call.enqueue(new Callback<List<Mascota>>() {
            @Override
            public void onResponse(Call<List<Mascota>> call, Response<List<Mascota>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Mascota> todasLasMascotas = response.body();
                    List<Mascota> mascotasFiltradas = new ArrayList<>();

                    for (Mascota mascota : todasLasMascotas) {
                        if (cumpleFiltros(mascota)) {
                            mascotasFiltradas.add(mascota);
                        }
                    }

                    mascotaAdapter.updateMascotas(mascotasFiltradas);
                }
            }

            @Override
            public void onFailure(Call<List<Mascota>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean cumpleFiltros(Mascota mascota) {
        // Verificar ciudad si está seleccionada
        if (!currentCiudad.isEmpty()) {
            try {
                String[] coordenadas = mascota.getCiudad().split(",");
                double latitude = Double.parseDouble(coordenadas[0]);
                double longitude = Double.parseDouble(coordenadas[1]);

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses.isEmpty() || addresses.get(0).getLocality() == null ||
                        !addresses.get(0).getLocality().equals(currentCiudad)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        // Verificar tipo de mascota
        if (!currentTipo.isEmpty() && (mascota.getTipo_mascota() == null || !mascota.getTipo_mascota().equals(currentTipo))) {
            return false;
        }

        // Verificar color
        if (!currentColor.isEmpty() && (mascota.getColor() == null || !mascota.getColor().equals(currentColor))) {
            return false;
        }
        if (!currentRaza.isEmpty() && (mascota.getRaza() == null || !mascota.getRaza().equals(currentRaza))) {
            return false;
        }
        // Verificar sexo
        if (!currentSexo.isEmpty() && (mascota.getSexo() == null || !mascota.getSexo().equals(currentSexo))) {
            return false;
        }

        // Verificar tamaño
        if (!currentTamano.isEmpty() && (mascota.getTamano() == null || !mascota.getTamano().equals(currentTamano))) {
            return false;
        }

        return true;
    }
    @Override
    public void onResume() {
        super.onResume();
        String selectedCity = (String) spinnerCities.getSelectedItem();
        loadMascotas();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        recyclerView = null;
        spinnerCities = null;
    }
}
