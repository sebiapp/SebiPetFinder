package com.riberadeltajo.sebipetfinder.Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;

import org.json.JSONException;
import org.json.JSONObject;

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
    private ImageView ivShowPassword;
    private boolean isPasswordVisible = false;
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

        edPassword.addTextChangedListener(new PasswordTextWatcher());

        ivShowPassword = findViewById(R.id.ivShowPassword);

        ivShowPassword.setOnClickListener(v -> togglePasswordVisibility());
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivShowPassword.setImageResource(R.drawable.ojoclose);
        } else {
            edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
        }
        edPassword.setSelection(edPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
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
    }

    private void enviarCodigoVerificacion() {
        String email = edEmail.getText().toString().trim();
        Log.d("DEBUG", "Enviando código a: " + email);

        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese un correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!esCorreoValido(email)) {
            Toast.makeText(this, "El correo no tiene un formato válido", Toast.LENGTH_SHORT).show();
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

    private boolean esCorreoValido(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(regex);
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
            btnVerificar.setEnabled(true);
            btnVerificar.setEnabled(false);
            btnReenviar.setVisibility(View.GONE);
            edCodigo.setEnabled(false);

            edEmail.setEnabled(false);
            edEmail.setFocusable(false);
            edEmail.setFocusableInTouchMode(false);
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
        // Encriptar la contraseña
        String hashedPassword = SecurityUtils.hashPassword(password);
        if (hashedPassword == null) {
            Toast.makeText(this, "Error al procesar la contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = edEmail.getText().toString().trim();

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
        Call<String> call = apiService.registerUser(user_name, user_lastname, username, email, hashedPassword);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body());
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if (status.equals("error")) {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        } else if (status.equals("success")) {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(RegisterActivity.this, "Error al procesar la respuesta", Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject errorJson = new JSONObject(errorBody);
                        String errorMessage = errorJson.getString("message");
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

    private class PasswordTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validarContrasena(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void validarContrasena(String password) {
        StringBuilder feedback = new StringBuilder();
        boolean isValid = true;

        if (password.length() < 8) {
            feedback.append("Debe tener al menos 8 caracteres.\n");
            isValid = false;
        }
        if (!password.matches(".*[A-Z].*")) {
            feedback.append("Debe incluir al menos una letra mayúscula.\n");
            isValid = false;
        }
        if (!password.matches(".*[a-z].*")) {
            feedback.append("Debe incluir al menos una letra minúscula.\n");
            isValid = false;
        }
        if (!password.matches(".*\\d.*")) {
            feedback.append("Debe incluir al menos un número.\n");
            isValid = false;
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            feedback.append("Debe incluir al menos un carácter especial (@#$%^&+=!).\n");
            isValid = false;
        }

        if (feedback.length() > 0) {
            edPassword.setError(feedback.toString().trim());
        } else {
            edPassword.setError(null);
        }

        registerButton.setEnabled(isValid);
    }
}
