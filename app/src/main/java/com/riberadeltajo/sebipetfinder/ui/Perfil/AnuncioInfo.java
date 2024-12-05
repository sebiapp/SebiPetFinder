package com.riberadeltajo.sebipetfinder.ui.Perfil;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;
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
    private CountryCodePicker ccp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncio_info);

        //Inicializar los elementos de la vista
        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvApellido);
        tvTelefono = findViewById(R.id.etTelefono);
        ivFoto = findViewById(R.id.ivFoto);
        ccp = findViewById(R.id.ccp);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnBorrar = findViewById(R.id.btnBorrar);

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
        fotoUrl = getIntent().getStringExtra("fotoUrl");
        String telefono = getIntent().getStringExtra("telefono");

        tvNombre.setText(nombre);
        tvDescripcion.setText(descripcion);
        if (telefono != null && telefono.length() > 2) {
            telefono = telefono.substring(3);
        }
        tvTelefono.setText(telefono);

        configurarValidacionTelefono();

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Picasso.get().load(fotoUrl).into(ivFoto);
        }

        btnGuardar.setOnClickListener(v -> editarMascota());
        btnBorrar.setOnClickListener(v -> borrarMascota());
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

        if (!ccp.isValidFullNumber()) {
            tvTelefono.setError("Número de teléfono inválido");
            tvTelefono.requestFocus();
            return;
        }
        String numeroCompleto = ccp.getFullNumberWithPlus();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<String> call;
        if (isMascotaPerdida) {
            call = apiService.editarMascotaPerdida(mascotaId, nombre, descripcion, numeroCompleto);
        } else {
            call = apiService.editarMascotaEncontrada(mascotaId, nombre, descripcion, numeroCompleto);
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