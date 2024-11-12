package com.riberadeltajo.sebipetfinder.Principal;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class NuevaMascota extends AppCompatActivity {
    private EditText etNombre, etDescripcion, etTelefono, etCiudad;
    private Button btnSeleccionarFoto, btnGuardarMascota;
    private String fotoUrl = "";
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView ivVistaPrevia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_mascota_enontrada);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etTelefono = findViewById(R.id.etTelefono);
        etCiudad = findViewById(R.id.etCiudad);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnGuardarMascota = findViewById(R.id.btnGuardarMascota);

        ivVistaPrevia = findViewById(R.id.ivVistaPrevia);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivVistaPrevia.setImageURI(selectedImageUri);
                    }
                }
        );
        btnSeleccionarFoto.setOnClickListener(v -> seleccionarImagen());
        btnGuardarMascota.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                subirImagen(selectedImageUri);
            } else {
                Toast.makeText(this, "Selecciona una imagen antes de guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

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

            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
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
                        Toast.makeText(NuevaMascota.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(NuevaMascota.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

        //Verificar userId
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<String> call = apiService.addMascota(nombre, descripcion, telefono, ciudad, fotoUrl, userId);
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
                        Log.d("error","el error es este"+responseBody);
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

}