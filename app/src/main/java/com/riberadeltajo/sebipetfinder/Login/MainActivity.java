package com.riberadeltajo.sebipetfinder.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView signupText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edUsername = findViewById(R.id.edUsuario);
        edPassword = findViewById(R.id.edPassword);
        loginButton = findViewById(R.id.registerButton);
        signupText = findViewById(R.id.signupText);

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

    }
    private void performLogin() {
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
    }}