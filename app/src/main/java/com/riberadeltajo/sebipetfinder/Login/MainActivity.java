package com.riberadeltajo.sebipetfinder.Login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.Principal.MainPanelActivity;
import com.riberadeltajo.sebipetfinder.R;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    private EditText edUsername;
    private EditText edPassword;
    private Button loginButton;
    private TextView signupText,tvForgotPassword;
    private ImageView verOjoPassword;
    private boolean passwordVisible = false;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edUsername = findViewById(R.id.edUsuario);
        edPassword = findViewById(R.id.edPassword);
        loginButton = findViewById(R.id.registerButton);
        signupText = findViewById(R.id.loginText);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
        verOjoPassword = findViewById(R.id.verOjoPassword);
        verOjoPassword.setOnClickListener(v -> cambiarVisibilidadPassword());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(v -> signIn());
        //Traducir el texto
        for (int i = 0; i < googleSignInButton.getChildCount(); i++) {
            View view = googleSignInButton.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setText("Iniciar sesión con Google");
                ((TextView) view).setTextSize(16);
            }
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleGoogleSignInSuccess(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Error en inicio de sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignInSuccess(GoogleSignInAccount account) {
        // Guardar que ha iniciado con google
        getSharedPreferences("user_data", MODE_PRIVATE)
                .edit()
                .putBoolean("isGoogleLogin", true)
                .apply();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<String> call = apiService.loginWithGoogle(
                account.getId(),
                account.getEmail(),
                account.getDisplayName()
        );

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("API", "Response received: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body());

                        if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Login successful")) {
                            int userId = jsonResponse.getInt("userId");
                            getSharedPreferences("user_data", MODE_PRIVATE)
                                    .edit()
                                    .putInt("userId", userId)
                                    .apply();

                            Intent intent = new Intent(MainActivity.this, MainPanelActivity.class);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "Login Correcto", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error en el inicio de sesión con Google", Toast.LENGTH_LONG).show();
                           // Log.d("API Response", "Raw response: " + response.body());

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                        Log.d("API Response", "Raw response: " + response.body());

                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error en el inicio de sesión con Google", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cambiarVisibilidadPassword() {
        if (passwordVisible) {
            edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            verOjoPassword.setImageResource(R.drawable.ojoclose);
        } else {
            edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            verOjoPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
        }
        edPassword.setSelection(edPassword.getText().length());
        passwordVisible = !passwordVisible;
    }

    private void performLogin() {
        getSharedPreferences("user_data", MODE_PRIVATE)
                .edit()
                .putBoolean("isGoogleLogin", false) // No es login de Google
                .apply();

        Log.d("LoginActivity", "Button clicked");
        String username = edUsername.getText().toString().trim();
        String password = edPassword.getText().toString().trim();

        Log.d("LoginActivity", "Username: " + username + ", Password: " + password);

        //Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<String> call = apiService.loginUser(username, password);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("API", "Response received: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body());

                        if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Login successful")) {
                            int userId = jsonResponse.getInt("userId");

                            //Guardar el userId en SharedPreferences
                            getSharedPreferences("user_data", MODE_PRIVATE)
                                    .edit()
                                    .putInt("userId", userId)
                                    .apply();

                            Intent intent = new Intent(MainActivity.this, MainPanelActivity.class);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "Login Correcto", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Usuario o contraseña incorrecto", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrecto", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}