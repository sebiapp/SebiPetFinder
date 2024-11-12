package com.riberadeltajo.sebipetfinder.ui.Perfil;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AnuncioInfo extends AppCompatActivity {
    private int mascotaId;
    private EditText tvNombre, tvDescripcion, tvTelefono, tvCiudad;
    private ImageView ivFoto;
    private String fotoUrl;
    private boolean isMascotaPerdida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncio_info);

        mascotaId = getIntent().getIntExtra("mascotaId", -1);
        isMascotaPerdida = getIntent().getBooleanExtra("isMascotaPerdida", true);

        Log.d("AnuncioInfo", "mascotaId recibido: " + mascotaId);
        Log.d("AnuncioInfo", "isMascotaPerdida: " + isMascotaPerdida);

        String nombre = getIntent().getStringExtra("nombre");
        String descripcion = getIntent().getStringExtra("descripcion");
        fotoUrl = getIntent().getStringExtra("fotoUrl");
        String telefono = getIntent().getStringExtra("telefono");
        String ciudad = getIntent().getStringExtra("ciudad");

        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvApellido);
        tvTelefono = findViewById(R.id.tvUsuario);
        tvCiudad = findViewById(R.id.tvCorreo);
        ivFoto = findViewById(R.id.ivFoto);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnBorrar = findViewById(R.id.btnBorrar);

        tvNombre.setText(nombre);
        tvDescripcion.setText(descripcion);
        tvTelefono.setText(telefono);
        tvCiudad.setText(ciudad);

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Picasso.get().load(fotoUrl).into(ivFoto);
        }

        btnGuardar.setOnClickListener(v -> editarMascota());
        btnBorrar.setOnClickListener(v -> borrarMascota());
    }

    private void editarMascota() {
        String nombre = tvNombre.getText().toString();
        String descripcion = tvDescripcion.getText().toString();
        String telefono = tvTelefono.getText().toString();
        String ciudad = tvCiudad.getText().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<String> call;
        if (isMascotaPerdida) {
            call = apiService.editarMascotaPerdida(mascotaId, nombre, descripcion, telefono, ciudad);
        } else {
            call = apiService.editarMascotaEncontrada(mascotaId, nombre, descripcion, telefono, ciudad);
        }

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
}