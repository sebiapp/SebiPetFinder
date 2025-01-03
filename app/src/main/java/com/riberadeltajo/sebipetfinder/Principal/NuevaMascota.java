package com.riberadeltajo.sebipetfinder.Principal;

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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;
import com.hbb20.CountryCodePicker;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;

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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

public class NuevaMascota extends AppCompatActivity {
    private EditText etNombre, etDescripcion, etTelefono, etCiudad;
    private Button btnSeleccionarFoto, btnGuardarMascota;
    private String fotoUrl = "";
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    //private ImageView ivVistaPrevia;
    private Uri uri;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private CountryCodePicker ccp;

    private MapView mapView;
    private GeoPoint selectedLocation;
    private Marker currentMarker;
    private static final int LOCATION_PERMISSION_CODE = 102;
    private FusedLocationProviderClient fusedLocationClient;
    //FOTOS 4
    private List<Uri> selectedImageUris = new ArrayList<>();
    private static final int MAX_FOTOS = 4;
    private ViewPager2 viewPagerFotos;
    private FotosPagerAdapter fotosPagerAdapter;
    private Spinner spinnerTipoMascota, spinnerColor, spinnerSexo, spinnerTamano,spinnerRaza;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configurar OpenStreetMap
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_nueva_mascota_enontrada);

        inicializarVistas();
        configurarMapa();
        configurarLaunchers();
        configurarBotones();
        configurarValidacionTelefono();
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etTelefono = findViewById(R.id.etTelefono);
        //etCiudad = findViewById(R.id.etCiudad);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnGuardarMascota = findViewById(R.id.btnGuardarMascota);
        //ivVistaPrevia = findViewById(R.id.ivVistaPrevia);
        spinnerTipoMascota = findViewById(R.id.spinnerTipoMascota);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        spinnerTamano = findViewById(R.id.spinnerTamano);
        spinnerRaza = findViewById(R.id.spinnerRaza);
        configurarSpinners();

        // 4 FOTOS
        viewPagerFotos = findViewById(R.id.viewPagerFotos);
        fotosPagerAdapter = new FotosPagerAdapter(this, selectedImageUris);
        viewPagerFotos.setAdapter(fotosPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabDots);
        new TabLayoutMediator(tabLayout, viewPagerFotos,
                (tab, position) -> {

                }
        ).attach();

        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(etTelefono);
        ccp.setDefaultCountryUsingNameCode("ES");
        ccp.setCountryForNameCode("ES");

        mapView = findViewById(R.id.mapView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        verificarPermisosUbicacion();
    }
    private void configurarSpinners() {
        // Tipos de mascota
        String[] tiposMascota = {"Seleccione tipo", "Perro", "Gato", "Ave", "Conejo", "Otro"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tiposMascota);
        spinnerTipoMascota.setAdapter(tipoAdapter);

        // Colores comunes
        String[] colores = {"Seleccione color", "Negro", "Blanco", "Marrón", "Gris", "Naranja",
                "Manchado", "Atigrado", "Otro"};
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, colores);
        spinnerColor.setAdapter(colorAdapter);
        // Raza
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
                ArrayAdapter<String> nuevoAdapter;

                switch (tipoSeleccionado) {
                    case "Perro":
                        nuevoAdapter = new ArrayAdapter<>(NuevaMascota.this,
                                android.R.layout.simple_spinner_dropdown_item, razasPerro);
                        break;
                    case "Gato":
                        nuevoAdapter = new ArrayAdapter<>(NuevaMascota.this,
                                android.R.layout.simple_spinner_dropdown_item, razasGato);
                        break;
                    case "Ave":
                        nuevoAdapter = new ArrayAdapter<>(NuevaMascota.this,
                                android.R.layout.simple_spinner_dropdown_item, razasAve);
                        break;
                    case "Conejo":
                        nuevoAdapter = new ArrayAdapter<>(NuevaMascota.this,
                                android.R.layout.simple_spinner_dropdown_item, razasConejo);
                        break;
                    default:
                        nuevoAdapter = new ArrayAdapter<>(NuevaMascota.this,
                                android.R.layout.simple_spinner_dropdown_item, razasOtro);
                        break;
                }
                spinnerRaza.setAdapter(nuevoAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // Sexo
        String[] sexos = {"Seleccione sexo", "Macho", "Hembra", "Desconocido"};
        ArrayAdapter<String> sexoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, sexos);
        spinnerSexo.setAdapter(sexoAdapter);

        // Tamaños
        String[] tamanos = {"Seleccione tamaño", "Muy pequeño", "Pequeño", "Mediano", "Grande",
                "Muy grande"};
        ArrayAdapter<String> tamanoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tamanos);
        spinnerTamano.setAdapter(tamanoAdapter);
    }
    private void verificarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            obtenerUbicacionActual();
        }
    }
    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    GeoPoint punto = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mapView.getController().setCenter(punto);
                    mapView.getController().setZoom(17.0);

                    // Crear marcador inicial
                    selectedLocation = punto;
                    if (currentMarker != null) {
                        mapView.getOverlays().remove(currentMarker);
                    }

                    Marker marker = new Marker(mapView);
                    marker.setPosition(punto);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle("Tu ubicación actual");

                    mapView.getOverlays().add(marker);
                    currentMarker = marker;
                    mapView.invalidate();
                }
            });
        }
    }

    private void configurarMapa() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(15.0);

        //Centrar en España por defecto
        GeoPoint startPoint = new GeoPoint(40.4168, -3.7038);
        mapView.getController().setCenter(startPoint);

        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        //clicks en el mapa
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedLocation = p;
                if (currentMarker != null) {
                    mapView.getOverlays().remove(currentMarker);
                }

                Marker marker = new Marker(mapView);
                marker.setPosition(p);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle("Ubicación seleccionada");

                mapView.getOverlays().add(marker);
                currentMarker = marker;
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
        ccp.registerCarrierNumberEditText(etTelefono);

        etTelefono.addTextChangedListener(new TextWatcher() {
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
                    etTelefono.setError(null);
                } else if (s.length() > 0) {
                    etTelefono.setError("Número inválido para " + ccp.getSelectedCountryName());
                }
            }
        });

        etTelefono.setInputType(InputType.TYPE_CLASS_PHONE);
    }
    private void configurarLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            ClipData clipData = result.getData().getClipData();
                            int countToAdd = Math.min(clipData.getItemCount(), MAX_FOTOS - selectedImageUris.size());

                            for (int i = 0; i < countToAdd; i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                selectedImageUris.add(uri);
                            }

                            if (clipData.getItemCount() > countToAdd) {
                                Toast.makeText(this, "Solo se pueden seleccionar " + MAX_FOTOS + " fotos",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (result.getData().getData() != null && selectedImageUris.size() < MAX_FOTOS) {
                            selectedImageUris.add(result.getData().getData());
                        }

                        actualizarVistaPrevia();
                        actualizarBotonFotos();
                    }
                }
        );
    }
    private void actualizarVistaPrevia() {
        fotosPagerAdapter.updateFotos(selectedImageUris);
        if (!selectedImageUris.isEmpty()) {
            viewPagerFotos.setCurrentItem(selectedImageUris.size() - 1);
        }
    }

    private void actualizarBotonFotos() {
        btnSeleccionarFoto.setText("Seleccionar Fotos (" + selectedImageUris.size() + "/" + MAX_FOTOS + ")");
    }
    private void configurarBotones() {
        btnSeleccionarFoto.setOnClickListener(v -> {
            if (!validarCamposBasicos()) {
                return;
            }
            if (selectedImageUris.size() >= MAX_FOTOS) {
                Toast.makeText(this, "Máximo " + MAX_FOTOS + " fotos permitidas", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarOpcionesImagen();
        });

        btnGuardarMascota.setOnClickListener(v -> {
            if (!validarTodosCampos()) {
                return;
            }
            if (selectedImageUris.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una imagen antes de guardar",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            subirImagenes();
        });
    }
    private boolean validarCamposBasicos() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return false;
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setError("La descripción es obligatoria");
            etDescripcion.requestFocus();
            return false;
        }

        if (!ccp.isValidFullNumber()) {
            etTelefono.setError("Número de teléfono inválido");
            etTelefono.requestFocus();
            return false;
        }

        return true;
    }
    private boolean validarTodosCampos() {
        if (!validarCamposBasicos()) {
            return false;
        }

        if (selectedLocation == null) {
            Toast.makeText(this, "Por favor selecciona la ubicación en el mapa",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerTipoMascota.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona el tipo de mascota", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerColor.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona el color", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerRaza.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona la raza de mascota", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerSexo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona el sexo", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerTamano.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona el tamaño", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void subirImagenes() {
        List<String> fotosUrls = new ArrayList<>();
        AtomicInteger contador = new AtomicInteger(0);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo imágenes...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            try {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                //Usar índice i para el nombre del archivo
                String fileName = "image_" + timestamp + "_" + i + ".jpg";
                File file = new File(getCacheDir(), fileName);

                //Leer y comprimir la imagen
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                bitmap = redimensionarImagen(bitmap, 800);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] imageBytes = baos.toByteArray();
                bitmap.recycle();
                baos.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(imageBytes);
                fos.flush();
                fos.close();

                RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", fileName, requestBody);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);
                final int currentIndex = i; //Guardar el índice actual para uso en el callback

                Call<JsonObject> call = apiService.uploadImage(body);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String url = response.body().get("url").getAsString();
                                if (url.startsWith("https://sienna-coyote-339198.hostingersite.com/")) {
                                    url = url.replace("https://sienna-coyote-339198.hostingersite.com/", "");
                                }
                                //Agregar la URL en el índice correcto
                                while (fotosUrls.size() <= currentIndex) {
                                    fotosUrls.add(null);
                                }
                                fotosUrls.set(currentIndex, url);

                                if (contador.incrementAndGet() == selectedImageUris.size()) {
                                    progressDialog.dismiss();
                                    //Filtrar cualquier null y unir las URLs
                                    fotoUrl = fotosUrls.stream()
                                            .filter(u -> u != null)
                                            .collect(Collectors.joining(","));
                                    guardarMascota();
                                }
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(NuevaMascota.this,
                                        "Error subiendo imagen " + (currentIndex + 1),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            progressDialog.dismiss();
                            Log.e("SubirImagen", "Error procesando respuesta", e);
                            Toast.makeText(NuevaMascota.this,
                                    "Error al procesar la respuesta",
                                    Toast.LENGTH_SHORT).show();
                        } finally {
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("SubirImagen", "Error de red", t);
                        Toast.makeText(NuevaMascota.this,
                                "Error de conexión al subir imagen " + (currentIndex + 1),
                                Toast.LENGTH_SHORT).show();
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                });

            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e("SubirImagen", "Error general", e);
                Toast.makeText(this, "Error procesando imagen " + (i + 1),
                        Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            switch (requestCode) {
                case CAMERA_PERMISSION_CODE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        abrirCamara();
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case STORAGE_PERMISSION_CODE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        seleccionarImagen();
                    } else {
                        Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            }
        }
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
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
                    if (selectedImageUris.size() < MAX_FOTOS) {
                        selectedImageUris.add(uri);
                        actualizarVistaPrevia();
                        actualizarBotonFotos();
                    } else {
                        Toast.makeText(this, "Máximo " + MAX_FOTOS + " fotos permitidas",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NuevaMascota.this, "Cancelado por el usuario",
                            Toast.LENGTH_SHORT).show();
                }
            }
    );



    private void guardarMascota() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String tipoMascota = spinnerTipoMascota.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();
        String raza = spinnerRaza.getSelectedItem().toString();
        String sexo = spinnerSexo.getSelectedItem().toString();
        String tamano = spinnerTamano.getSelectedItem().toString();
        //String ciudad = etCiudad.getText().toString().trim();
        // Validar campos
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setError("La descripción es obligatoria");
            etDescripcion.requestFocus();
            return;
        }

        if (!ccp.isValidFullNumber()) {
            etTelefono.setError("Número de teléfono inválido");
            etTelefono.requestFocus();
            return;
        }
        if (selectedLocation == null) {
            Toast.makeText(this, "Por favor selecciona la ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        String numeroCompleto = ccp.getFullNumberWithPlus();
        int userId = obtenerUserId();

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        String ciudad = String.format(Locale.US, "%.6f,%.6f",
                selectedLocation.getLatitude(),
                selectedLocation.getLongitude());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<String> call = apiService.addMascota(nombre, descripcion, numeroCompleto, ciudad, fotoUrl, userId,
                tipoMascota, color, raza, sexo, tamano);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseBody = response.body();
                    if (responseBody.contains("Mascota registrada con")) {
                        Toast.makeText(NuevaMascota.this, "Mascota guardada con éxito", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(NuevaMascota.this, responseBody, Toast.LENGTH_SHORT).show();
                        Log.d("error", "el error es este" + responseBody);
                    }
                } else {
                    Toast.makeText(NuevaMascota.this, "Error al guardar la mascota", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(NuevaMascota.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int obtenerUserId() {
        return getSharedPreferences("user_data", MODE_PRIVATE).getInt("userId", -1);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}