package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;

public class MascotaEncontradaInfo extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mascota_encontrada_info);

        String nombre = getIntent().getStringExtra("nombre");
        String descripcion = getIntent().getStringExtra("descripcion");
        String fotoUrl = getIntent().getStringExtra("fotoUrl");
        String telefono = getIntent().getStringExtra("telefono");
        String ciudad = getIntent().getStringExtra("ciudad");

        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvDescripcion = findViewById(R.id.tvApellido);
        TextView tvTelefono = findViewById(R.id.tvUsuario);
        TextView tvCiudad = findViewById(R.id.tvCorreo);
        ImageView ivFoto = findViewById(R.id.ivFoto);
        Button btnLlamar = findViewById(R.id.btnGuardar);
        Button btnCorreo = findViewById(R.id.emailButton);

        tvNombre.setText(String.format("Nombre: %s", nombre));
        tvDescripcion.setText(String.format("Descripción: %s", descripcion));
        tvTelefono.setText(String.format("Teléfono: %s", telefono));
        tvCiudad.setText(String.format("Ciudad: %s", ciudad));

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Picasso.get().load(fotoUrl).into(ivFoto);
        }

        btnLlamar.setOnClickListener(v -> {
            if (telefono != null && !telefono.isEmpty()) {
                Intent intentLlamar = new Intent(Intent.ACTION_DIAL);
                intentLlamar.setData(Uri.parse("tel:" + telefono));
                startActivity(intentLlamar);
            } else {
                Toast.makeText(this, "Número de teléfono no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnCorreo.setOnClickListener(v -> {
            if (ciudad != null && !ciudad.isEmpty()) {
                Intent intentCorreo = new Intent(Intent.ACTION_SEND);
                intentCorreo.setType("message/rfc822");
                intentCorreo.putExtra(Intent.EXTRA_EMAIL, new String[]{"sebiandrei.crucianu@riberadeltajo.es"});
                intentCorreo.putExtra(Intent.EXTRA_SUBJECT, "Información sobre mascota de " + ciudad);
                intentCorreo.putExtra(Intent.EXTRA_TEXT, "Hola,\n\nNecesitamos hablar sobre " + nombre + ".\n" +
                        "Descripción: " + descripcion + "\n" +
                        "Teléfono de contacto: " + telefono + "\n" +
                        "Ciudad: " + ciudad + "\n\nGracias.");

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
}
