package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;
import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MascotaEncontradaInfo extends AppCompatActivity {
    private MapView mapView;
    private ApiService apiService;
    private String anuncioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configurar OpenStreetMap
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_mascota_encontrada_info);

        // Inicializar Retrofit y apiService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);


        String nombre = getIntent().getStringExtra("nombre");
        String descripcion = getIntent().getStringExtra("descripcion");
        String fotoUrl = getIntent().getStringExtra("fotoUrl");
        String telefono = getIntent().getStringExtra("telefono");
        String ciudad = getIntent().getStringExtra("ciudad");
        anuncioId = getIntent().getStringExtra("anuncioId");
        Log.d("ID ANUNCIO ",anuncioId);
        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvDescripcion = findViewById(R.id.tvApellido);
        TextView tvTelefono = findViewById(R.id.tvUsuario);
        mapView = findViewById(R.id.mapView);
        ImageView ivFoto = findViewById(R.id.ivFoto);
        ivFoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullScreenImageActivity.class);
            intent.putExtra("imageUrl", fotoUrl);
            startActivity(intent);
        });
        Button btnLlamar = findViewById(R.id.btnGuardar);
        Button btnCorreo = findViewById(R.id.emailButton);

        tvNombre.setText(String.format("Nombre: %s", nombre));
        tvDescripcion.setText(String.format("Descripción: %s", descripcion));
        tvTelefono.setText(String.format("Teléfono: %s", telefono));
        //tvCiudad.setText(String.format("Ciudad: %s", ciudad));
        if (ciudad != null && !ciudad.isEmpty()) {
            try {
                String[] coordenadas = ciudad.split(",");
                if (coordenadas.length == 2) {
                    double latitud = Double.parseDouble(coordenadas[0]);
                    double longitud = Double.parseDouble(coordenadas[1]);

                    //Configurar el mapa
                    mapView.setTileSource(TileSourceFactory.MAPNIK);
                    mapView.getController().setZoom(15.0);

                    GeoPoint punto = new GeoPoint(latitud, longitud);
                    mapView.getController().setCenter(punto);
                    mapView.setMultiTouchControls(true);

                    //Añadir marcador
                    Marker marker = new Marker(mapView);
                    marker.setPosition(punto);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle("Ubicación de la mascota");
                    mapView.getOverlays().add(marker);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Picasso.get().load(fotoUrl).into(ivFoto);
        }

        btnLlamar.setOnClickListener(v -> {
            if (telefono != null && !telefono.isEmpty()) {
                // Enviar notificación
                enviarNotificacion("llamada");

                Intent intentLlamar = new Intent(Intent.ACTION_DIAL);
                intentLlamar.setData(Uri.parse("tel:" + telefono));
                startActivity(intentLlamar);
            } else {
                Toast.makeText(this, "Número de teléfono no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnCorreo.setOnClickListener(v -> {
            if (ciudad != null && !ciudad.isEmpty()) {
                // Enviar notificación
                enviarNotificacion("correo");

                Intent intentCorreo = new Intent(Intent.ACTION_SEND);
                intentCorreo.setType("message/rfc822");
                intentCorreo.putExtra(Intent.EXTRA_EMAIL, new String[]{"sebiandrei.crucianu@riberadeltajo.es"});
                intentCorreo.putExtra(Intent.EXTRA_SUBJECT, "Información sobre mascota de " + ciudad);
                intentCorreo.putExtra(Intent.EXTRA_TEXT, "Hola,\n\nNecesitamos hablar sobre " + nombre + ".\n" +
                        "Descripción: " + descripcion + "\n" +
                        "Teléfono de contacto: " + telefono + "\n\nGracias");

                try {
                    startActivity(Intent.createChooser(intentCorreo, "Elige una aplicación de correo"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "No hay aplicaciones de correo instaladas", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Ciudad no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarNotificacion(String tipo) {
        if (anuncioId != null) {
            Log.d("Notificación", "Enviando notificación con anuncioId: " + anuncioId + " y tipo: " + tipo);

            Call<JsonObject> call = apiService.enviarNotificacion(anuncioId, tipo);

            Log.d("Notificación", "Llamando a la API para enviar la notificación...");

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("Notificación", "Notificación enviada correctamente");
                    } else {
                        Log.e("Notificación", "Error en la respuesta del servidor: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("Notificación", "Error al enviar la solicitud: ", t);
                }
            });
        } else {
            Log.e("Notificación", "El anuncioId es nulo, no se puede enviar la notificación");
        }
    }



}