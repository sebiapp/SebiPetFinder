package com.riberadeltajo.sebipetfinder.ui.Perfil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;
import com.hbb20.CountryCodePicker;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.Principal.NuevaMascotaPerdida;
import com.riberadeltajo.sebipetfinder.R;
import com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados.FotosUrlPagerAdapter;
import com.squareup.picasso.Picasso;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AnuncioInfo extends AppCompatActivity {
    private final String[] tiposMascota = {"Seleccione tipo", "Perro", "Gato", "Ave", "Conejo", "Otro"};
    private final String[] colores = {"Seleccione color", "Negro", "Blanco", "Marrón", "Gris", "Naranja",
            "Manchado", "Atigrado", "Otro"};
    private final String[] razas = {"Seleccione raza", "Pastor Alemán", "Labrador", "Golden Retriever",
            "Bulldog", "Chihuahua", "Yorkshire", "Husky", "Siamés", "Persa", "Angora", "Maine Coon",
            "Bengalí", "Otro"};
    private final String[] sexos = {"Seleccione sexo", "Macho", "Hembra", "Desconocido"};
    private final String[] tamanos = {"Seleccione tamaño", "Muy pequeño", "Pequeño", "Mediano", "Grande",
            "Muy grande"};
    private int mascotaId;
    private EditText tvNombre, tvDescripcion, tvTelefono;
    private String fotoUrl;
    private boolean isMascotaPerdida;
    private CountryCodePicker ccp;
    private String ciudad;
    private MapView mapView;
    private GeoPoint selectedLocation;
    private Marker currentMarker;
    private static final int LOCATION_PERMISSION_CODE = 102;
    private FusedLocationProviderClient fusedLocationClient;
    private ViewPager2 viewPagerFotos;
    private FotosUrlPagerAdapter fotosUrlPagerAdapter;
    private FloatingActionButton btnAgregarFoto, btnBorrarFoto;
    private List<String> fotosUrlsList = new ArrayList<>();
    private static final int MAX_FOTOS = 4;
    private Uri uri;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Spinner spinnerTipoMascota, spinnerColor, spinnerRaza, spinnerSexo, spinnerTamano;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configurar OpenStreetMap
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_anuncio_info);

        //Inicializar los elementos de la vista
        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvApellido);
        tvTelefono = findViewById(R.id.etTelefono);
        viewPagerFotos = findViewById(R.id.viewPagerFotos);
        btnAgregarFoto = findViewById(R.id.btnAgregarFoto);
        btnBorrarFoto = findViewById(R.id.btnBorrarFoto);
        TabLayout tabLayout = findViewById(R.id.tabDots);
        spinnerTipoMascota = findViewById(R.id.spinnerTipoMascota);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerRaza = findViewById(R.id.spinnerRaza);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        spinnerTamano = findViewById(R.id.spinnerTamano);
        configurarLaunchers();
        ccp = findViewById(R.id.ccp);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnBorrar = findViewById(R.id.btnBorrar);
        configurarSpinners();
        //Configurar el CountryCodePicker
        ccp.registerCarrierNumberEditText(tvTelefono);
        ccp.setDefaultCountryUsingNameCode("ES");
        ccp.setCountryForNameCode("ES");

        mascotaId = getIntent().getIntExtra("mascotaId", -1);
        isMascotaPerdida = getIntent().getBooleanExtra("isMascotaPerdida", true);

        Log.d("AnuncioInfo", "mascotaId recibido: " + mascotaId);
        Log.d("AnuncioInfo", "isMascotaPerdida: " + isMascotaPerdida);

        String nombre = getIntent().getStringExtra("nombre");
        String descripcion = getIntent().getStringExtra("descripcion");
        String tipoMascota = getIntent().getStringExtra("tipoMascota");
        String color = getIntent().getStringExtra("color");
        String raza = getIntent().getStringExtra("raza");
        String sexo = getIntent().getStringExtra("sexo");
        String tamano = getIntent().getStringExtra("tamano");

        fotoUrl = getIntent().getStringExtra("fotoUrl");
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            String[] fotosArray = fotoUrl.split(",");
            fotosUrlsList.clear();
            for (String foto : fotosArray) {
                fotosUrlsList.add(foto.trim());
            }
        }
        //adaptador fotos
        fotosUrlPagerAdapter = new FotosUrlPagerAdapter(this, fotosUrlsList.toArray(new String[0]));
        viewPagerFotos.setAdapter(fotosUrlPagerAdapter);

        //Configurar los indicadores
        if (fotosUrlsList.size() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
            new TabLayoutMediator(tabLayout, viewPagerFotos,
                    (tab, position) -> {
                        //No
                    }
            ).attach();
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        String telefono = getIntent().getStringExtra("telefono");
        ciudad = getIntent().getStringExtra("ciudad");

        tvNombre.setText(nombre);
        tvDescripcion.setText(descripcion);
        if (telefono != null && telefono.length() > 2) {
            telefono = telefono.substring(3);
        }
        tvTelefono.setText(telefono);
        setSpinnerToValue(spinnerTipoMascota, tipoMascota);
        //Esperar a que se actualice el adapter de razas y luego establecer la raza
        spinnerTipoMascota.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Establecemos la raza después de que el adapter se haya actualizado
                if (raza != null && !raza.isEmpty()) {
                    setSpinnerToValue(spinnerRaza, raza);
                }
            }
        }, 200);
        setSpinnerToValue(spinnerColor, color);
        //setSpinnerToValue(spinnerRaza, raza);
        setSpinnerToValue(spinnerSexo, sexo);
        setSpinnerToValue(spinnerTamano, tamano);




        configurarValidacionTelefono();

        mapView = findViewById(R.id.mapView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        verificarPermisosUbicacion();
        configurarMapa();

        btnAgregarFoto.setOnClickListener(v -> {
            if (fotosUrlsList.size() >= MAX_FOTOS) {
                Toast.makeText(this, "Máximo " + MAX_FOTOS + " fotos permitidas", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarOpcionesImagen();
        });

        btnBorrarFoto.setOnClickListener(v -> {
            int currentPos = viewPagerFotos.getCurrentItem();
            if (fotosUrlsList.size() > 1) {
                new AlertDialog.Builder(this)
                        .setTitle("Borrar foto")
                        .setMessage("¿Estás seguro de que quieres borrar esta foto?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            borrarFoto(currentPos);
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                Toast.makeText(this, "Debe mantener al menos una foto", Toast.LENGTH_SHORT).show();
            }
        });

        btnGuardar.setOnClickListener(v -> editarMascota());
        btnBorrar.setOnClickListener(v -> borrarMascota());
    }
    private void borrarFoto(int position) {
        if (position >= 0 && position < fotosUrlsList.size() && fotosUrlsList.size() > 1) {
            fotosUrlsList.remove(position);
            fotosUrlPagerAdapter = new FotosUrlPagerAdapter(this, fotosUrlsList.toArray(new String[0]));
            viewPagerFotos.setAdapter(fotosUrlPagerAdapter);

            //Actualizar dots
            TabLayout tabLayout = findViewById(R.id.tabDots);
            if (fotosUrlsList.size() > 1) {
                tabLayout.setVisibility(View.VISIBLE);
                new TabLayoutMediator(tabLayout, viewPagerFotos,
                        (tab, pos) -> {
                            //No
                        }
                ).attach();
            } else {
                tabLayout.setVisibility(View.GONE);
            }

            //Actualizar fotoUrl para cuando se guarde
            fotoUrl = String.join(",", fotosUrlsList);
        }
    }
    private void verificarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            obtenerUbicacionActual();
        }
    }
    private void obtenerUbicacionActual() {
        if (ciudad != null && !ciudad.isEmpty()) {
            try {
                String[] coordenadas = ciudad.split(",");
                if (coordenadas.length == 2) {
                    double latitud = Double.parseDouble(coordenadas[0]);
                    double longitud = Double.parseDouble(coordenadas[1]);

                    //Configurar el mapa
                    mapView.setTileSource(TileSourceFactory.MAPNIK);
                    mapView.getController().setZoom(15.0);

                    GeoPoint punto = new GeoPoint(latitud, longitud);
                    mapView.getController().setCenter(punto);
                    mapView.setMultiTouchControls(true);

                    // Establecer la ubicación seleccionada inicial
                    selectedLocation = punto;

                    //Añadir marcador
                    currentMarker = new Marker(mapView);
                    currentMarker.setPosition(punto);
                    currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    currentMarker.setTitle("Ubicación de la mascota");
                    mapView.getOverlays().add(currentMarker);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    private void configurarMapa() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(15.0);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        //clicks en el mapa
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedLocation = p;

                // Remover marcador anterior si existe
                if (currentMarker != null) {
                    mapView.getOverlays().remove(currentMarker);
                }

                // Crear nuevo marcador
                currentMarker = new Marker(mapView);
                currentMarker.setPosition(p);
                currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                currentMarker.setTitle("Nueva ubicación seleccionada");
                mapView.getOverlays().add(currentMarker);
                mapView.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mReceive);
        mapView.getOverlays().add(eventsOverlay);
    }
    private void configurarValidacionTelefono() {
        ccp.registerCarrierNumberEditText(tvTelefono);

        tvTelefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String number = s.toString();
                if (ccp.isValidFullNumber()) {
                    if (s.length() > number.length()) {
                        s.delete(number.length(), s.length());
                    }
                    tvTelefono.setError(null);
                } else if (s.length() > 0) {
                    tvTelefono.setError("Número inválido para " + ccp.getSelectedCountryName());
                }
            }
        });

        tvTelefono.setInputType(InputType.TYPE_CLASS_PHONE);
    }
    private void editarMascota() {
        String nombre = tvNombre.getText().toString();
        String descripcion = tvDescripcion.getText().toString();
        String tipoMascota = spinnerTipoMascota.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();
        String raza = spinnerRaza.getSelectedItem().toString();
        String sexo = spinnerSexo.getSelectedItem().toString();
        String tamano = spinnerTamano.getSelectedItem().toString();
        // Validar campos
        if (nombre.isEmpty()) {
            tvNombre.setError("El nombre es obligatorio");
            tvNombre.requestFocus();
            return;
        }

        if (descripcion.isEmpty()) {
            tvDescripcion.setError("La descripción es obligatoria");
            tvDescripcion.requestFocus();
            return;
        }
        if (tipoMascota.equals("Seleccione tipo")) {
            Toast.makeText(this, "Por favor seleccione un tipo de mascota", Toast.LENGTH_SHORT).show();
            spinnerTipoMascota.requestFocus();
            return;
        }

        if (color.equals("Seleccione color")) {
            Toast.makeText(this, "Por favor seleccione un color", Toast.LENGTH_SHORT).show();
            spinnerColor.requestFocus();
            return;
        }

        if (raza.equals("Seleccione raza")) {
            Toast.makeText(this, "Por favor seleccione una raza", Toast.LENGTH_SHORT).show();
            spinnerRaza.requestFocus();
            return;
        }

        if (sexo.equals("Seleccione sexo")) {
            Toast.makeText(this, "Por favor seleccione el sexo", Toast.LENGTH_SHORT).show();
            spinnerSexo.requestFocus();
            return;
        }

        if (tamano.equals("Seleccione tamaño")) {
            Toast.makeText(this, "Por favor seleccione el tamaño", Toast.LENGTH_SHORT).show();
            spinnerTamano.requestFocus();
            return;
        }
        if (!ccp.isValidFullNumber()) {
            tvTelefono.setError("Número de teléfono inválido");
            tvTelefono.requestFocus();
            return;
        }
        if (selectedLocation == null) {
            Toast.makeText(this, "Por favor selecciona la ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fotosUrlsList.isEmpty()) {
            Toast.makeText(this, "Debe tener al menos una foto", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerTipoMascota.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona el tipo de mascota", Toast.LENGTH_SHORT).show();
            return;
        }
        String ciudad = String.format(Locale.US, "%.6f,%.6f",
                selectedLocation.getLatitude(),
                selectedLocation.getLongitude());

        String numeroCompleto = ccp.getFullNumberWithPlus();
        fotoUrl = String.join(",", fotosUrlsList);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<String> call;
        if (isMascotaPerdida) {
            call = apiService.editarMascotaPerdida(mascotaId, nombre, descripcion, numeroCompleto, ciudad, fotoUrl,
                    tipoMascota, color, raza, sexo, tamano);
        } else {
            call = apiService.editarMascotaEncontrada(mascotaId, nombre, descripcion, numeroCompleto, ciudad, fotoUrl,
                    tipoMascota, color, raza, sexo, tamano);
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Actualizando anuncio...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseBody = response.body();
                    if (responseBody != null && responseBody.contains("Mascota actualizada con")) {
                        Toast.makeText(AnuncioInfo.this, "Anuncio actualizado con éxito", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AnuncioInfo.this, responseBody, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AnuncioInfo.this, "Error al actualizar el anuncio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(AnuncioInfo.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void borrarMascota() {
        new AlertDialog.Builder(this)
                .setTitle("Borrar Anuncio")
                .setMessage("¿Estás seguro de que deseas borrar este anuncio?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .build();

                    ApiService apiService = retrofit.create(ApiService.class);
                    Call<String> call;
                    if (isMascotaPerdida) {
                        call = apiService.borrarMascotaPerdida(mascotaId);
                    } else {
                        call = apiService.borrarMascotaEncontrada(mascotaId);
                    }

                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AnuncioInfo.this, "Anuncio borrado con éxito", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(AnuncioInfo.this, "Error al borrar el anuncio", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(AnuncioInfo.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
    /* PARA EDITAR LAS IMAGENES */
    private void configurarLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            // Múltiples fotos seleccionadas
                            ClipData clipData = result.getData().getClipData();
                            int countToAdd = Math.min(clipData.getItemCount(), MAX_FOTOS - fotosUrlsList.size());

                            for (int i = 0; i < countToAdd; i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                subirImagen(uri);
                            }

                            if (clipData.getItemCount() > countToAdd) {
                                Toast.makeText(this, "Solo se pueden seleccionar " + MAX_FOTOS + " fotos",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (result.getData().getData() != null) {
                            // Una sola foto seleccionada
                            subirImagen(result.getData().getData());
                        }
                    }
                }
        );
    }

    private void mostrarOpcionesImagen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen");
        String[] opciones = {"Tomar foto", "Elegir de la galería"};

        builder.setItems(opciones, (dialog, which) -> {
            switch (which) {
                case 0:
                    verificarPermisoCamara();
                    break;
                case 1:
                    verificarPermisoAlmacenamiento();
                    break;
            }
        });
        builder.show();
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            abrirCamara();
        }
    }

    private void verificarPermisoAlmacenamiento() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                seleccionarImagen();
            }
        } else {
            String[] permisos = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            boolean todosPermisosOtorgados = true;
            for (String permiso : permisos) {
                if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                    todosPermisosOtorgados = false;
                    break;
                }
            }

            if (!todosPermisosOtorgados) {
                ActivityCompat.requestPermissions(this, permisos, STORAGE_PERMISSION_CODE);
            } else {
                seleccionarImagen();
            }
        }
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private void abrirCamara() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Título");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripción");

        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        camaraARL.launch(intent);
    }

    private ActivityResultLauncher<Intent> camaraARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (fotosUrlsList.size() < MAX_FOTOS) {
                        subirImagen(uri);
                    } else {
                        Toast.makeText(this, "Máximo " + MAX_FOTOS + " fotos permitidas",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Cancelado por el usuario",
                            Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void subirImagen(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo imagen...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "image_" + timestamp + ".jpg";
            File file = new File(getCacheDir(), fileName);

            // Leer y comprimir la imagen
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            bitmap = redimensionarImagen(bitmap, 800);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            bitmap.recycle();
            baos.close();

            // Escribir bytes
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(imageBytes);
            fos.flush();
            fos.close();

            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", fileName, requestBody);

            // Configurar cliente HTTP con timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<JsonObject> call = apiService.uploadImage(body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        String url = response.body().get("url").getAsString();
                        fotosUrlsList.add(url);
                        actualizarVistaPrevia();
                    } else {
                        Toast.makeText(AnuncioInfo.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AnuncioInfo.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarVistaPrevia() {
        fotosUrlPagerAdapter = new FotosUrlPagerAdapter(this, fotosUrlsList.toArray(new String[0]));
        viewPagerFotos.setAdapter(fotosUrlPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabDots);
        if (fotosUrlsList.size() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
            new TabLayoutMediator(tabLayout, viewPagerFotos,
                    (tab, position) -> {
                    }
            ).attach();
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        viewPagerFotos.setCurrentItem(fotosUrlsList.size() - 1);
    }

    private Bitmap redimensionarImagen(Bitmap imagen, int maxSize) {
        int width = imagen.getWidth();
        int height = imagen.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(imagen, width, height, true);
    }
    //FILTROS
    private void configurarSpinners() {
        //Tipos de mascota
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tiposMascota);
        spinnerTipoMascota.setAdapter(tipoAdapter);

        //Colores
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, colores);
        spinnerColor.setAdapter(colorAdapter);
        //Raza
        ArrayAdapter<String> razaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{"Seleccione raza"});
        spinnerRaza.setAdapter(razaAdapter);
        String[] razasPerro = {"Seleccione raza", "Pastor Alemán", "Labrador", "Golden Retriever", "Bulldog", "Chihuahua", "Yorkshire", "Husky", "Otro"};
        String[] razasGato = {"Seleccione raza", "Siamés", "Persa", "Angora", "Maine Coon", "Bengalí", "Otro"};
        String[] razasAve = {"Seleccione raza", "Canario", "Periquito", "Cotorra", "Agaporni", "Loro", "Otro"};
        String[] razasConejo = {"Seleccione raza", "Cabeza de León", "Mini Lop", "Angora", "Rex", "Holland Lop", "Otro"};
        String[] razasOtro = {"Seleccione raza", "Otro"};
        spinnerTipoMascota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipoSeleccionado = tiposMascota[position];
                String razaActual = spinnerRaza.getSelectedItem() != null ?
                        spinnerRaza.getSelectedItem().toString() : "";
                ArrayAdapter<String> nuevoAdapter;

                switch (tipoSeleccionado) {
                    case "Perro":
                        nuevoAdapter = new ArrayAdapter<>(AnuncioInfo.this,
                                android.R.layout.simple_spinner_dropdown_item, razasPerro);
                        break;
                    case "Gato":
                        nuevoAdapter = new ArrayAdapter<>(AnuncioInfo.this,
                                android.R.layout.simple_spinner_dropdown_item, razasGato);
                        break;
                    case "Ave":
                        nuevoAdapter = new ArrayAdapter<>(AnuncioInfo.this,
                                android.R.layout.simple_spinner_dropdown_item, razasAve);
                        break;
                    case "Conejo":
                        nuevoAdapter = new ArrayAdapter<>(AnuncioInfo.this,
                                android.R.layout.simple_spinner_dropdown_item, razasConejo);
                        break;
                    default:
                        nuevoAdapter = new ArrayAdapter<>(AnuncioInfo.this,
                                android.R.layout.simple_spinner_dropdown_item, razasOtro);
                        break;
                }
                spinnerRaza.setAdapter(nuevoAdapter);

                // Intentar mantener la selección previa si existe en el nuevo adapter
                if (!razaActual.isEmpty()) {
                    setSpinnerToValue(spinnerRaza, razaActual);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //Sexo
        ArrayAdapter<String> sexoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, sexos);
        spinnerSexo.setAdapter(sexoAdapter);

        //Tamaños
        ArrayAdapter<String> tamanoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tamanos);
        spinnerTamano.setAdapter(tamanoAdapter);
    }
    private void setSpinnerToValue(Spinner spinner, String value) {
        Log.d("Spinner", "Intentando establecer valor: " + value);
        if (value != null && !value.isEmpty() && spinner.getAdapter() != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            for (int position = 0; position < adapter.getCount(); position++) {
                String item = adapter.getItem(position).toString();
                Log.d("Spinner", "Comparando con: " + item);
                if (item.equals(value)) {
                    spinner.setSelection(position);
                    Log.d("Spinner", "Valor establecido en posición: " + position);
                    return;
                }
            }
        }
        Log.d("Spinner", "No se pudo establecer el valor: " + value);
    }
}