package com.riberadeltajo.sebipetfinder.Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    private EditText edUser_name, edLast_name, edUsername, edEmail, edPassword, edCodigo;
    private Button btnEnviarCodigo, btnVerificar, btnReenviar, registerButton;
    private TextView loginText, tvTemporizador;
    private String codigoVerificacion;
    private boolean isVerified = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        inicializarVistas();
        configurarListeners();
    }

    private void inicializarVistas() {
        edUser_name = findViewById(R.id.edNombre_Usuario);
        edLast_name = findViewById(R.id.edApellidos_Usuario);
        edUsername = findViewById(R.id.edUsername);
        edEmail = findViewById(R.id.edCorreo);
        edPassword = findViewById(R.id.edPassword);
        edCodigo = findViewById(R.id.edCodigo);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        btnVerificar = findViewById(R.id.btnVerificar);
        btnReenviar = findViewById(R.id.btnReenviar);
        registerButton = findViewById(R.id.registerButton);
        tvTemporizador = findViewById(R.id.tvTemporizador);
        loginText = findViewById(R.id.loginText);

        registerButton.setEnabled(false);
        edCodigo.setEnabled(false);
        btnVerificar.setVisibility(View.GONE);
        btnReenviar.setVisibility(View.GONE);
    }

    private void configurarListeners() {
        btnEnviarCodigo.setOnClickListener(v -> {
            enviarCodigoVerificacion();
        });

        btnVerificar.setOnClickListener(v -> {
            verificarCodigo();
        });

        btnReenviar.setOnClickListener(v -> {
            btnReenviar.setVisibility(View.GONE);
            enviarCodigoVerificacion();
        });

        registerButton.setOnClickListener(v -> registerUser());
        loginText.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, MainActivity.class)));
    }

    private void enviarCodigoVerificacion() {
        String email = edEmail.getText().toString().trim();
        Log.d("DEBUG", "Enviando código a: " + email);

        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese un correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<JsonObject> call = apiService.enviarCodigo(email);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("DEBUG", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DEBUG", "Response body: " + response.body());
                    JsonObject jsonResponse = response.body();
                    if (jsonResponse.has("code")) {
                        codigoVerificacion = jsonResponse.get("code").getAsString();
                        edCodigo.setEnabled(true);
                        btnEnviarCodigo.setVisibility(View.GONE);
                        btnVerificar.setVisibility(View.VISIBLE);
                        iniciarTemporizador();
                        Toast.makeText(RegisterActivity.this, "Código enviado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e("ERROR", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(RegisterActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("ERROR", "Network error: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(RegisterActivity.this, "Error al enviar código: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarTemporizador() {
        btnReenviar.setVisibility(View.GONE);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTemporizador.setText("Espera " + millisUntilFinished / 1000 + "s para reenviar");
            }

            @Override
            public void onFinish() {
                tvTemporizador.setText("");
                if (!isVerified) {
                    btnReenviar.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    private void verificarCodigo() {
        String codigoIngresado = edCodigo.getText().toString().trim();
        if (codigoIngresado.equals(codigoVerificacion)) {
            isVerified = true;
            registerButton.setEnabled(true);
            btnVerificar.setEnabled(false);
            btnReenviar.setVisibility(View.GONE);
            edCodigo.setEnabled(false);
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            tvTemporizador.setText("¡Código verificado!");
            Toast.makeText(this, "Código verificado correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        if (!isVerified) {
            Toast.makeText(this, "Debe verificar su correo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String user_name = edUser_name.getText().toString().trim();
        String user_lastname = edLast_name.getText().toString().trim();
        String username = edUsername.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String email = edEmail.getText().toString().trim();

        //Validar campos vacíos
        if (user_name.isEmpty() || user_lastname.isEmpty() || username.isEmpty() ||
                password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<String> call = apiService.registerUser(user_name, user_lastname, username, email, password);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body();
                    Toast.makeText(RegisterActivity.this, responseBody, Toast.LENGTH_LONG).show();

                    if (responseBody.contains("exitoso")) {
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JsonObject jsonError = new Gson().fromJson(errorBody, JsonObject.class);
                        String errorMessage = jsonError.has("error") ?
                                jsonError.get("error").getAsString() :
                                "Error en el registro";
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error en el registro", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error de red", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}