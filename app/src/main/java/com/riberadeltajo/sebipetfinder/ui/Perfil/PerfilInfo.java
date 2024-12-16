package com.riberadeltajo.sebipetfinder.ui.Perfil;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.Login.MainActivity;
import com.riberadeltajo.sebipetfinder.R;

import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PerfilInfo extends AppCompatActivity {
    private EditText tvNombre, tvApellido, tvUsuario, tvEmail, tvContra, edCodigo;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Button btnEnviarCodigo, btnVerificar, btnReenviar, btnGuardar;
    private TextView tvTemporizador;
    private String codigoVerificacion;
    private boolean isVerified = false;
    private CountDownTimer countDownTimer;
    int id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_info);

        inicializarVistas();
        cargarDatosIntent();
        configurarListeners();
    }
    private void inicializarVistas() {
        tvNombre = findViewById(R.id.tvNombre);
        tvApellido = findViewById(R.id.tvApellido);
        tvUsuario = findViewById(R.id.tvUsuario);
        tvEmail = findViewById(R.id.tvCorreo);
        tvContra = findViewById(R.id.tvContra);
        edCodigo = findViewById(R.id.edCodigo);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        btnVerificar = findViewById(R.id.btnVerificar);
        btnReenviar = findViewById(R.id.btnReenviar);
        btnGuardar = findViewById(R.id.btnGuardar);
        tvTemporizador = findViewById(R.id.tvTemporizador);
        Button btnBorrar = findViewById(R.id.btnBorrar);

        // Configuración inicial
        btnGuardar.setEnabled(false);
        edCodigo.setEnabled(false);
        btnVerificar.setVisibility(View.GONE);
        btnReenviar.setVisibility(View.GONE);

        btnGuardar.setOnClickListener(v -> editarUsuario());
        btnBorrar.setOnClickListener(v -> borrarUsuario());
    }
    private void cargarDatosIntent() {
        id = getIntent().getIntExtra("id", 0);
        String nom = getIntent().getStringExtra("nom");
        String ape = getIntent().getStringExtra("ape");
        String user = getIntent().getStringExtra("user");
        String email = getIntent().getStringExtra("email");
        String contra = getIntent().getStringExtra("contra");

        tvNombre.setText(nom);
        tvApellido.setText(ape);
        tvUsuario.setText(user);
        tvEmail.setText(email);
        tvContra.setText(contra);
    }
    private void configurarListeners() {
        btnEnviarCodigo.setOnClickListener(v -> enviarCodigoVerificacion());
        btnVerificar.setOnClickListener(v -> verificarCodigo());
        btnReenviar.setOnClickListener(v -> {
            btnReenviar.setVisibility(View.GONE);
            enviarCodigoVerificacion();
        });
        btnGuardar.setOnClickListener(v -> editarUsuario());
    }
    private void enviarCodigoVerificacion() {
        String email = tvEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese un correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validarCorreo(email)) {
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
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = response.body();
                    if (jsonResponse.has("code")) {
                        codigoVerificacion = jsonResponse.get("code").getAsString();
                        edCodigo.setEnabled(true);
                        btnEnviarCodigo.setVisibility(View.GONE);
                        btnVerificar.setVisibility(View.VISIBLE);
                        iniciarTemporizador();
                        Toast.makeText(PerfilInfo.this, "Código enviado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PerfilInfo.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PerfilInfo.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarCodigo() {
        String codigoIngresado = edCodigo.getText().toString().trim();
        if (codigoIngresado.equals(codigoVerificacion)) {
            isVerified = true;
            btnGuardar.setEnabled(true);
            btnVerificar.setEnabled(false);
            btnReenviar.setVisibility(View.GONE);
            edCodigo.setEnabled(false);

            tvEmail.setEnabled(false);
            tvEmail.setFocusable(false);
            tvEmail.setFocusableInTouchMode(false);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            tvTemporizador.setText("¡Código verificado!");
            Toast.makeText(this, "Código verificado correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show();
        }
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
    /* COMPROBAR SI TIENE DISPONIBILIDAD DE HUELLA EL DISPOSITIVO */
    private void comprobarDisponibilidadBiometrica() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "La app puede autenticar usando biometría.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Este dispositivo no tiene sensor biométrico.", Toast.LENGTH_LONG).show();
                mandarconfirmacion();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "El sensor biométrico no está disponible en este momento.", Toast.LENGTH_LONG).show();
                mandarconfirmacion();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No hay huellas o caras registradas en este dispositivo.", Toast.LENGTH_LONG).show();
                mandarconfirmacion();
                break;
        }
    }
    /* CONFIGURAR LA AUTENTIFCACIÓN CARA */
    private void configurarAutenticacionBiometrica() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "¡Autenticación exitosa!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Autenticación fallida", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación biométrica")
                .setSubtitle("Usa tu huella dactilar o reconocimiento facial para continuar")
                .setNegativeButtonText("Cancelar")
                .build();
    }
    /* CONFIGURAR LA AUTENTIFCACIÓN HUELLA */
    private void solicitarAutenticacion(Runnable onSuccess) {
        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        onSuccess.run();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(PerfilInfo.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(PerfilInfo.this, "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación requerida")
                .setSubtitle("Usa tu huella dactilar o reconocimiento facial")
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
    private void borrarUsuario() {
        configurarAutenticacionBiometrica();
        comprobarDisponibilidadBiometrica();

        solicitarAutenticacion(() -> {
            mandarconfirmacion();
        });
    }

    private void mandarconfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Borrar Usuario")
                .setMessage("¿Estás seguro de que quieres borrar tu cuenta? Todos tus anuncios se eliminarán de forma permanente.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .build();

                    ApiService apiService = retrofit.create(ApiService.class);
                    Call<String> call = apiService.borrarUsuario(id);

                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.d("Response", response.body().toString());
                                if (response.body() != null) {
                                    Log.d("RESPUESTA_BODY", response.body().toString());
                                }
                                Toast.makeText(PerfilInfo.this, "Usuario borrado con éxito", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PerfilInfo.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(PerfilInfo.this, "Error al borrar el usuario", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(PerfilInfo.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();

    }


    private void editarUsuario() {

        String nombre = tvNombre.getText().toString();
        String apellido= tvApellido.getText().toString();
        String usuario= tvUsuario.getText().toString();
        String email= tvEmail.getText().toString();
        String contra= tvContra.getText().toString();

        // Validar campos
        if (nombre.isEmpty()) {
            tvNombre.setError("El nombre es obligatorio");
            tvNombre.requestFocus();
            return;
        }

        if (apellido.isEmpty()) {
            tvApellido.setError("Los apellidos son obligatorios");
            tvApellido.requestFocus();
            return;
        }

        if (usuario.isEmpty()) {
            tvUsuario.setError("El usuario es obligatorio");
            tvUsuario.requestFocus();
            return;
        }
        if (!isVerified) {
            Toast.makeText(this, "Debe verificar su correo primero", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validarCorreo(email)) {
            return;
        }
        if (!validarContrasena(contra)) {
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<String> call = apiService.editarUsuario(id, nombre, apellido, usuario, email, contra);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseBody = response.body();
                    Log.d("Response", response.body().toString());

                    if (response.body() != null) {
                        Log.d("RESPUESTA_BODY", response.body().toString());
                    }
                    if (responseBody != null && responseBody.contains("Usuario actualizado con")) {
                        Toast.makeText(PerfilInfo.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(PerfilInfo.this, responseBody, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PerfilInfo.this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("Response Error", errorBody);
                        Toast.makeText(PerfilInfo.this, "Error al actualizar el perfil: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(PerfilInfo.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    private boolean validarCorreo(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!email.matches(emailPattern)) {
            tvEmail.setError("Correo inválido. Debe tener el formato correcto, por ejemplo: usuario@dominio.com");
            tvEmail.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validarContrasena(String password) {
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

        if (!isValid) {
            tvContra.setError(feedback.toString().trim());
            tvContra.requestFocus();
        }

        return isValid;
    }

}