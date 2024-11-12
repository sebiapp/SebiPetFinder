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
import com.riberadeltajo.sebipetfinder.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    private EditText edUser_name,edLast_name,edUsername,edEmail,edPassword;
    private Button registerButton;
    private TextView loginText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edUser_name = findViewById(R.id.edNombre_Usuario);
        edLast_name = findViewById(R.id.edApellidos_Usuario);
        edUsername = findViewById(R.id.edUsername);
        edEmail = findViewById(R.id.edCorreo);
        edPassword = findViewById(R.id.edPassword);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private void registerUser() {
        String user_name = edUser_name.getText().toString().trim();
        String user_lastname = edLast_name.getText().toString().trim();
        String username = edUsername.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String email = edEmail.getText().toString().trim();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<String> call = apiService.registerUser(user_name,user_lastname,username, email, password);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("API", "Response received: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, response.body(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registro fallido", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Network error", Toast.LENGTH_LONG).show();
            }
        });
    }
}