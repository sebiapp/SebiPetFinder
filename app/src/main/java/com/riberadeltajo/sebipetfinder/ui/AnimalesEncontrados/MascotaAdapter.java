package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.ViewHolder> {
    private List<Mascota> mascotas;
    private Context context;

    public MascotaAdapter(Context context, List<Mascota> mascotas) {
        this.context = context;
        this.mascotas = mascotas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota, parent, false);
        return new ViewHolder(view, context, mascotas);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        Mascota mascota = mascotas.get(position);
        holder.textViewNombre.setText(mascota.getNombre());

        //Obtener solo la primera foto de la cadena
        String fotoUrl = mascota.getFotoUrl();
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            String[] fotos = fotoUrl.split(",");
            if (fotos.length > 0) {
                String primeraFoto = fotos[0].trim();
                if (!primeraFoto.startsWith("http")) {
                    primeraFoto = "https://sienna-coyote-339198.hostingersite.com/" + primeraFoto;
                }
                Picasso.get()
                        .load(primeraFoto)
                        .fit()
                        .centerCrop()
                        .into(holder.imageViewFoto);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    public void updateMascotas(List<Mascota> newMascotas) {
        this.mascotas.clear();
        this.mascotas.addAll(newMascotas);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFoto;
        TextView textViewNombre;

        public ViewHolder(View itemView, Context context, List<Mascota> mascotas) {
            super(itemView);
            imageViewFoto = itemView.findViewById(R.id.imageViewFoto);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);

            imageViewFoto.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Mascota selectedMascota = mascotas.get(position);
                    Intent intent = new Intent(context, MascotaEncontradaInfo.class);
                    intent.putExtra("nombre", selectedMascota.getNombre());
                    intent.putExtra("descripcion", selectedMascota.getDescripcion());
                    intent.putExtra("fotoUrl", selectedMascota.getFotoUrl());
                    intent.putExtra("telefono", selectedMascota.getTelefono());
                    intent.putExtra("ciudad", selectedMascota.getCiudad());
                    intent.putExtra("anuncioId", selectedMascota.getUser_id());
                    context.startActivity(intent);
                }
            });
        }
    }

}
