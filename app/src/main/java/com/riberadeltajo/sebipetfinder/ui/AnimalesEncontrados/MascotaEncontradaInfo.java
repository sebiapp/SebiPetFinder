package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.riberadeltajo.sebipetfinder.Principal.FotosPagerAdapter;
import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;
import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MascotaEncontradaInfo extends AppCompatActivity {
    private MapView mapView;
    private ApiService apiService;
    private String anuncioId;
    private ViewPager2 viewPagerFotos;
    private FotosUrlPagerAdapter fotosUrlPagerAdapter;
    private List<Uri> fotosList = new ArrayList<>();
    private String nombre,descripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configurar OpenStreetMap
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_mascota_encontrada_info);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);


        nombre = getIntent().getStringExtra("nombre");
        descripcion = getIntent().getStringExtra("descripcion");
        String fotoUrl = getIntent().getStringExtra("fotoUrl");
        String telefono = getIntent().getStringExtra("telefono");
        String ciudad = getIntent().getStringExtra("ciudad");
        String tipoMascota = getIntent().getStringExtra("tipoMascota");
        String color = getIntent().getStringExtra("color");
        String raza = getIntent().getStringExtra("raza");
        String sexo = getIntent().getStringExtra("sexo");
        String tamano = getIntent().getStringExtra("tamano");
        anuncioId = getIntent().getStringExtra("anuncioId");

        Log.d("ID ANUNCIO ",anuncioId);
        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvDescripcion = findViewById(R.id.tvApellido);
        TextView tvTelefono = findViewById(R.id.tvUsuario);
        TextView tvTipoMascota = findViewById(R.id.tvTipoMascota);
        TextView tvRaza = findViewById(R.id.tvRaza);
        TextView tvColor = findViewById(R.id.tvColor);
        TextView tvSexo = findViewById(R.id.tvSexo);
        TextView tvTamano = findViewById(R.id.tvTamano);
        mapView = findViewById(R.id.mapView);

        //Configurar ViewPager para las fotos
        viewPagerFotos = findViewById(R.id.viewPagerFotos);
        TabLayout tabLayout = findViewById(R.id.tabDots);

        //Configurar fotos
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            String[] fotosUrls = fotoUrl.split(",");

            //Eliminar espacios en blanco al inicio y final de cada URL
            for (int i = 0; i < fotosUrls.length; i++) {
                fotosUrls[i] = fotosUrls[i].trim();
            }
            //Crear y configurar el adaptador
            fotosUrlPagerAdapter = new FotosUrlPagerAdapter(this, fotosUrls);
            viewPagerFotos.setAdapter(fotosUrlPagerAdapter);

            //Configurar los dots indicadores solo si hay más de una foto
            if (fotosUrls.length > 1) {
                tabLayout.setVisibility(View.VISIBLE);
                new TabLayoutMediator(tabLayout, viewPagerFotos,
                        (tab, position) -> {
                    //NADA
                        }
                ).attach();
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        } else {
            viewPagerFotos.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        }
        View overlayView = findViewById(R.id.clickOverlay);
        if (overlayView != null) {
            overlayView.setOnClickListener(v -> {
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", fotoUrl);
                intent.putExtra("position", viewPagerFotos.getCurrentItem()); //Pasar la posición actual
                startActivity(intent);
            });
        }
        Button btnLlamar = findViewById(R.id.btnGuardar);
        Button btnCorreo = findViewById(R.id.emailButton);
        Button btnCompartir = findViewById(R.id.shareButton);
        btnCompartir.setOnClickListener(v -> captureAndShareScreen());
        tvNombre.setText(String.format("Nombre: %s", nombre));
        tvDescripcion.setText(String.format("Descripción: %s", descripcion));
        tvTelefono.setText(String.format("Teléfono: %s", telefono));
        tvTipoMascota.setText(String.format("Tipo: %s", tipoMascota));
        tvRaza.setText(String.format("Raza: %s", raza));
        tvColor.setText(String.format("Color: %s", color));
        tvSexo.setText(String.format("Sexo: %s", sexo));
        tvTamano.setText(String.format("Tamaño: %s", tamano));
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

    //COMPARTIR EN REDES
    private void captureAndShareScreen() {

        ScrollView scrollView = findViewById(R.id.rootScrollView);

        //Crear un bitmap del tamaño total del contenido
        Bitmap bitmap = getBitmapFromScrollView(scrollView);

        if (bitmap != null) {
            shareScreenshot(bitmap);
        } else {
            Toast.makeText(this, "Error al crear la captura", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromScrollView(ScrollView scrollView) {
        try {
            //Obtener las dimensiones totales del contenido
            int totalHeight = scrollView.getChildAt(0).getHeight();
            int totalWidth = scrollView.getWidth();

            //Crear un bitmap del tamaño total
            Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            //Guardar el estado actual del ScrollView
            int originalScrollY = scrollView.getScrollY();

            //Forzar al ScrollView a scrollear al inicio
            scrollView.scrollTo(0, 0);

            //Dibujar el fondo
            canvas.drawColor(Color.WHITE);

            //Dibujar la vista completa
            scrollView.draw(canvas);

            //Restaurar la posición original del scroll
            scrollView.scrollTo(0, originalScrollY);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void shareScreenshot(Bitmap bitmap) {
        try {
            //Comprimir con mejor calidad
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "shared_image.jpg"); //Cambiado a JPG
            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream); //Usar JPEG con alta calidad
            stream.flush();
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this,
                    "com.riberadeltajo.sebipetfinder.fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            String shareText = String.format("¡Mira esta mascota encontrada!\n\nNombre: %s\nDescripción: %s",
                    nombre, descripcion);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Compartir anuncio"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al compartir la imagen", Toast.LENGTH_SHORT).show();
        }
    }

}