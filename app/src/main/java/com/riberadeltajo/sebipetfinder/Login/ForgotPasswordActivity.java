package com.riberadeltajo.sebipetfinder.Login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText edCorreo, edCodigo, edNuevaPassword, edConfirmarPassword;
    private Button btnEnviarCodigo, btnVerificar, btnReenviar, btnCambiarPassword;
    private LinearLayout layoutVerificacion, layoutNuevaPassword;
    private String codigoVerificacion;
    private boolean isVerified = false;
    private Retrofit retrofit;
    private ApiService apiService;
    private CountDownTimer countDownTimer;
    private TextView tvTemporizador;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        inicializarVistas();
        configurarListeners();
        retrofitSetup();
    }

    private void inicializarVistas() {
        edCorreo = findViewById(R.id.edCorreo);
        edCodigo = findViewById(R.id.edCodigo);
        edNuevaPassword = findViewById(R.id.edNuevaPassword);
        edConfirmarPassword = findViewById(R.id.edConfirmarPassword);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        btnVerificar = findViewById(R.id.btnVerificar);
        btnReenviar = findViewById(R.id.btnReenviar);
        btnCambiarPassword = findViewById(R.id.btnCambiarPassword);
        layoutVerificacion = findViewById(R.id.layoutVerificacion);
        layoutNuevaPassword = findViewById(R.id.layoutNuevaPassword);
        tvTemporizador = findViewById(R.id.tvTemporizador);

        edNuevaPassword.addTextChangedListener(new PasswordTextWatcher());
        edConfirmarPassword.addTextChangedListener(new PasswordTextWatcher());

        ImageView verOjoNuevaPassword = findViewById(R.id.verOjoNuevaPassword);
        ImageView verOjoConfirmarPassword = findViewById(R.id.verOjoConfirmarPassword);

        verOjoNuevaPassword.setOnClickListener(v -> cambiarVisibilidadPassword(edNuevaPassword, verOjoNuevaPassword));
        verOjoConfirmarPassword.setOnClickListener(v -> cambiarVisibilidadPassword(edConfirmarPassword, verOjoConfirmarPassword));

    }
    private void cambiarVisibilidadPassword(EditText editText, ImageView imageView) {
        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.ojoclose);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.baseline_remove_red_eye_24);
        }
        editText.setSelection(editText.getText().length());
    }
    private void configurarListeners() {
        btnEnviarCodigo.setOnClickListener(v -> enviarCodigoVerificacion());
        btnVerificar.setOnClickListener(v -> verificarCodigo());
        btnReenviar.setOnClickListener(v -> enviarCodigoVerificacion());
        btnCambiarPassword.setOnClickListener(v -> cambiarContrasena());
    }

    private void enviarCodigoVerificacion() {
        String email = edCorreo.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<JsonObject> call = apiService.enviarCodigoRestablecerPassword(email);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = response.body();
                    if (jsonResponse.has("status")) {
                        String status = jsonResponse.get("status").getAsString();
                        if (status.equals("error")) {
                            Toast.makeText(ForgotPasswordActivity.this, jsonResponse.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if (jsonResponse.has("code")) {
                        codigoVerificacion = jsonResponse.get("code").getAsString();
                        Toast.makeText(ForgotPasswordActivity.this, "Código enviado al correo", Toast.LENGTH_SHORT).show();
                        layoutVerificacion.setVisibility(View.VISIBLE);
                        layoutNuevaPassword.setVisibility(View.GONE);
                        btnEnviarCodigo.setEnabled(false);
                        btnReenviar.setVisibility(View.GONE);
                        iniciarTemporizador();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error al enviar el código", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                btnReenviar.setVisibility(View.VISIBLE);
                btnEnviarCodigo.setEnabled(true);
            }
        }.start();
    }
    private void retrofitSetup() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }
    private void verificarCodigo() {
        String codigoIngresado = edCodigo.getText().toString().trim();
        if (codigoIngresado.equals(codigoVerificacion)) {
            isVerified = true;
            btnVerificar.setEnabled(false);
            layoutVerificacion.setVisibility(View.GONE);
            layoutNuevaPassword.setVisibility(View.VISIBLE);
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            Toast.makeText(this, "Código verificado correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    private void cambiarContrasena() {
        if (!isVerified) {
            Toast.makeText(this, "Debe verificar el código de verificación primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevaPassword = edNuevaPassword.getText().toString().trim();
        String confirmarPassword = edConfirmarPassword.getText().toString().trim();

        if (nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
            Toast.makeText(this, "Debe ingresar y confirmar la nueva contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        actualizarContrasena(edCorreo.getText().toString().trim(), nuevaPassword);
    }

    private void actualizarContrasena(String email, String nuevaPassword) {
        Call<JsonObject> call = apiService.actualizarContrasena(email, nuevaPassword);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = response.body();
                    if (jsonResponse.has("status") && jsonResponse.get("status").getAsString().equals("success")) {
                        Toast.makeText(ForgotPasswordActivity.this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error de servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            if (password.equals(edConfirmarPassword.getText().toString().trim())) {
                edNuevaPassword.setError(feedback.toString().trim());
                edConfirmarPassword.setError(null);
            } else {
                edNuevaPassword.setError(feedback.toString().trim());
                edConfirmarPassword.setError(feedback.toString().trim());
            }
        } else {
            edNuevaPassword.setError(null);
            edConfirmarPassword.setError(null);
        }

        btnCambiarPassword.setEnabled(isValid && password.equals(edConfirmarPassword.getText().toString().trim()));
    }
}