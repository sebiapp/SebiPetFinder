package com.riberadeltajo.sebipetfinder.Principal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class NuevaMascotaPerdida extends AppCompatActivity {
    private EditText etNombre, etDescripcion, etTelefono, etCiudad;
    private Button btnSeleccionarFoto, btnGuardarMascota;
    private String fotoUrl = "";
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView ivVistaPrevia;
    private Uri uri;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_mascota_perdida);

        inicializarVistas();
        configurarLaunchers();
        configurarBotones();
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etTelefono = findViewById(R.id.etTelefono);
        etCiudad = findViewById(R.id.etCiudad);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnGuardarMascota = findViewById(R.id.btnGuardarMascota);
        ivVistaPrevia = findViewById(R.id.ivVistaPrevia);
    }

    private void configurarLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivVistaPrevia.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    private void configurarBotones() {
        btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesImagen());
        btnGuardarMascota.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                subirImagen(selectedImageUri);
            } else {
                Toast.makeText(this, "Selecciona una imagen antes de guardar", Toast.LENGTH_SHORT).show();
            }
        });
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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
                    selectedImageUri = uri;
                    ivVistaPrevia.setImageURI(uri);
                } else {
                    Toast.makeText(NuevaMascotaPerdida.this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void subirImagen(Uri imageUri) {
        try {
            File file = new File(getCacheDir(), "image.jpg");
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<JsonObject> call = apiService.uploadImage(body);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        fotoUrl = response.body().get("url").getAsString();
                        guardarMascota();
                    } else {
                        Toast.makeText(NuevaMascotaPerdida.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(NuevaMascotaPerdida.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al seleccionar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarMascota() {
        String nombre = etNombre.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String telefono = etTelefono.getText().toString();
        String ciudad = etCiudad.getText().toString();
        int userId = obtenerUserId();

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<String> call = apiService.addMascotaPerdida(nombre, descripcion, telefono, ciudad, fotoUrl, userId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseBody = response.body();
                    if (responseBody.contains("Mascota registrada con")) {
                        Toast.makeText(NuevaMascotaPerdida.this, "Mascota guardada con éxito", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(NuevaMascotaPerdida.this, responseBody, Toast.LENGTH_SHORT).show();
                        Log.d("error", "el error es este" + responseBody);
                    }
                } else {
                    Toast.makeText(NuevaMascotaPerdida.this, "Error al guardar la mascota", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(NuevaMascotaPerdida.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int obtenerUserId() {
        return getSharedPreferences("user_data", MODE_PRIVATE).getInt("userId", -1);
    }
}