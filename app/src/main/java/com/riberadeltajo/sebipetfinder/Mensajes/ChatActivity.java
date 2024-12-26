package com.riberadeltajo.sebipetfinder.Mensajes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ApiService apiService;
    private int emisorId;
    private String receptorId;
    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> mensajes;
    private String tipoAnuncio;

    private TextView userNameTitle;
    private ImageButton backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //vistas
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        userNameTitle = findViewById(R.id.userNameTitle);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        emisorId = prefs.getInt("userId", -1);
        int anuncioId = getIntent().getIntExtra("anuncioId",0);
        tipoAnuncio = getIntent().getStringExtra("tipoAnuncio");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        //RecyclerView
        mensajes = new ArrayList<>();
        mensajeAdapter = new MensajeAdapter(this, mensajes, emisorId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(mensajeAdapter);

        sendButton.setOnClickListener(v -> enviarMensaje());

        //Determinar receptor y cargar mensajes
        String otroUsuarioId = getIntent().getStringExtra("otroUsuarioId");
        Log.d("ChatActivity", "otroUsuarioId recibido: " + otroUsuarioId);
        Log.d("ChatActivity", "Tipo Anuncio: " + tipoAnuncio);
        Log.d("ChatActivity", "ID Anuncio: " + anuncioId);
        if (otroUsuarioId != null) {
            Log.d("ChatActivity", "Usando otroUsuarioId como receptor");
            receptorId = otroUsuarioId;
            obtenerNombreUsuario(otroUsuarioId);
            sendButton.setEnabled(true);
            cargarMensajes();
        } else {
            Log.d("ChatActivity", "Obteniendo dueño del anuncio");
            obtenerDuenoAnuncio(String.valueOf(anuncioId));
            sendButton.setEnabled(false);
        }
    }
    private void obtenerNombreUsuario(String userId) {
        Call<JsonObject> call = apiService.obtenerUsuario(userId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().has("nombre_usuario")) {
                        String nombreUsuario = response.body().get("nombre_usuario").getAsString();
                        userNameTitle.setText(nombreUsuario);
                    }
                } else {
                    Log.e("ChatActivity", "Error al obtener nombre de usuario");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("ChatActivity", "Error al obtener nombre de usuario", t);
            }
        });
    }
    private void obtenerDuenoAnuncio(String anuncioId) {
        if (anuncioId == null) {
            Log.e("ChatActivity", "Error: anuncioId es null");
            Toast.makeText(this, "Error: ID de anuncio no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipo = getIntent().getStringExtra("tipoAnuncio");
        Log.d("ChatActivity", "ID del anuncio: " + anuncioId);
        Log.d("ChatActivity", "Tipo de anuncio: " + tipo);

        Call<JsonObject> call = tipo.equals("perdida") ?
                apiService.obtenerDuenoAnuncioPerdido(anuncioId) :
                apiService.obtenerDuenoAnuncio(anuncioId);

        Log.d("ChatActivity", "URL de la llamada: " + call.request().url());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("ChatActivity", "Código de respuesta: " + response.code());
                if (response.body() != null) {
                    Log.d("ChatActivity", "Respuesta: " + response.body().toString());
                }
                if (response.errorBody() != null) {
                    try {
                        Log.e("ChatActivity", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().has("user_id")) {
                        receptorId = response.body().get("user_id").getAsString();
                        // Establecer el nombre de usuario en el título
                        if (response.body().has("nombre_usuario")) {
                            String nombreUsuario = response.body().get("nombre_usuario").getAsString();
                            userNameTitle.setText(nombreUsuario);
                        }
                        sendButton.setEnabled(true);
                        cargarMensajes();
                    } else {
                        Log.e("ChatActivity", "Respuesta no contiene user_id: " + response.body());
                        Toast.makeText(ChatActivity.this,
                                "Error al obtener información del dueño", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("ChatActivity", "Error al obtener dueño", t);
                Log.e("ChatActivity", "URL que falló: " + call.request().url());
                Toast.makeText(ChatActivity.this,
                        "Error al obtener información del dueño", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void enviarMensaje() {
        String mensaje = messageInput.getText().toString().trim();
        if (!mensaje.isEmpty()) {
            Log.d("ChatActivity", "Enviando mensaje");
            Log.d("ChatActivity", "emisorId: " + emisorId);
            Log.d("ChatActivity", "receptorId actual: " + receptorId);
            Log.d("ChatActivity", "anuncioId: " + getIntent().getIntExtra("anuncioId",0));
            Log.d("ChatActivity", "tipoAnuncio: " + tipoAnuncio);
            String otroUsuarioId = getIntent().getStringExtra("otroUsuarioId");
            String receptorFinal = otroUsuarioId != null ? otroUsuarioId : receptorId;

            Call<JsonObject> call = apiService.enviarMensaje(
                    String.valueOf(emisorId),
                    receptorFinal,
                    String.valueOf(getIntent().getIntExtra("anuncioId",0)),
                    tipoAnuncio,
                    mensaje
            );
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("ChatActivity", "Respuesta del servidor: " + response.body().toString());

                        if (response.body().has("error")) {
                            String error = response.body().get("error").getAsString();
                            Toast.makeText(ChatActivity.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show();
                        } else if (response.body().has("success")) {
                            messageInput.setText("");
                            Toast.makeText(ChatActivity.this,
                                    "Mensaje enviado", Toast.LENGTH_SHORT).show();
                            cargarMensajes();
                        }
                    } else {
                        //error
                        Log.e("ChatActivity", "Error en la respuesta: " + response.code());
                        try {
                            Log.e("ChatActivity", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(ChatActivity.this,
                                "Error al enviar el mensaje: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("ChatActivity", "Error al enviar mensaje", t);
                    Toast.makeText(ChatActivity.this,
                            "Error al enviar el mensaje: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void cargarMensajes() {
        Log.d("ChatActivity", "Cargando mensajes");
        Log.d("ChatActivity", "emisorId: " + emisorId);
        Log.d("ChatActivity", "receptorId: " + receptorId);

        Call<JsonArray> call = apiService.obtenerMensajes(emisorId, receptorId,getIntent().getIntExtra("anuncioId", 0));
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ChatActivity", "Mensajes recibidos: " + response.body().toString());

                    mensajes.clear();
                    for (JsonElement element : response.body()) {
                        JsonObject json = element.getAsJsonObject();

                        Mensaje mensaje = new Mensaje(
                                json.get("id").getAsInt(),
                                json.get("emisor_id").getAsInt(),
                                json.get("receptor_id").getAsInt(),
                                json.get("mensaje").getAsString(),
                                json.get("fecha_envio").getAsString(),
                                json.get("leido").getAsBoolean()
                        );

                        mensajes.add(mensaje);
                    }

                    mensajeAdapter.notifyDataSetChanged();
                    messagesRecyclerView.scrollToPosition(mensajes.size() - 1);
                } else {
                    Log.e("ChatActivity", "Error al cargar mensajes: " + response.code());
                    try {
                        Log.e("ChatActivity", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ChatActivity.this,
                            "Error al cargar los mensajes: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.e("ChatActivity", "Error al cargar mensajes", t);
                Toast.makeText(ChatActivity.this,
                        "Error al cargar los mensajes: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}