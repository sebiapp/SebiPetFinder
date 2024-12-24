package com.riberadeltajo.sebipetfinder.Mensajes;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ListaMensajesActivity extends AppCompatActivity {
    private RecyclerView recyclerConversaciones;
    private ConversacionAdapter adapter;
    private LinearLayout vistaSinMensajes;
    private ApiService apiService;
    private List<Conversacion> conversaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_mensajes);

        //vistas
        recyclerConversaciones = findViewById(R.id.recyclerConversaciones);
        vistaSinMensajes = findViewById(R.id.vistaSinMensajes);

        //RecyclerView
        recyclerConversaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversacionAdapter(this, conversaciones);
        recyclerConversaciones.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        cargarConversaciones();
    }

    private void cargarConversaciones() {
        //ID del usuario actual
        int userId = obtenerIdUsuario();

        Call<JsonArray> call = apiService.obtenerConversaciones(userId);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //Convertir la respuesta a lista de conversaciones
                    List<Conversacion> nuevasConversaciones = convertirRespuestaAConversaciones(response.body());

                    //Actualizar la UI
                    if (nuevasConversaciones.isEmpty()) {
                        mostrarVistaSinMensajes();
                    } else {
                        mostrarConversaciones(nuevasConversaciones);
                    }
                } else {
                    mostrarVistaSinMensajes();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                mostrarVistaSinMensajes();
            }
        });
    }

    private int obtenerIdUsuario() {
        return getSharedPreferences("user_data", MODE_PRIVATE)
                .getInt("userId", -1);
    }

    private void mostrarVistaSinMensajes() {
        recyclerConversaciones.setVisibility(View.GONE);
        vistaSinMensajes.setVisibility(View.VISIBLE);
    }

    private void mostrarConversaciones(List<Conversacion> nuevasConversaciones) {
        vistaSinMensajes.setVisibility(View.GONE);
        recyclerConversaciones.setVisibility(View.VISIBLE);
        adapter.actualizarConversaciones(nuevasConversaciones);
    }

        //Método para convertir la respuesta JSON a objetos Conversacion
        private List<Conversacion> convertirRespuestaAConversaciones(JsonArray jsonArray) {
            List<Conversacion> conversaciones = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();
                conversaciones.add(new Conversacion(
                        obj.get("anuncio_id").getAsInt(),
                        obj.get("titulo_anuncio").getAsString(),
                        obj.get("imagen_url").getAsString(),
                        obj.get("ultimo_mensaje").getAsString(),
                        obj.get("fecha_ultimo_mensaje").getAsString(),
                        obj.get("mensajes_no_leidos").getAsInt(),
                        obj.get("nombre_contacto").getAsString(),
                        obj.get("tipo_anuncio").getAsString(),
                        obj.get("otro_usuario_id").getAsString()
                ));
            }
            return conversaciones;
        }


    @Override
    protected void onResume() {
        super.onResume();
        cargarConversaciones();
    }
}