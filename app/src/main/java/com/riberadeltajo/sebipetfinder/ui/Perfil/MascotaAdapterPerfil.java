package com.riberadeltajo.sebipetfinder.ui.Perfil;

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

public class MascotaAdapterPerfil extends RecyclerView.Adapter<MascotaAdapterPerfil.ViewHolder> {
    private List<MascotaPerfil> mascotas;
    private Context context;

    public MascotaAdapterPerfil(Context context, List<MascotaPerfil> mascotas) {
        this.context = context;
        this.mascotas = mascotas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota_perfil, parent, false);
        return new ViewHolder(view, context, mascotas);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MascotaPerfil mascota = mascotas.get(position);
        holder.textViewNombre.setText(mascota.getNombre());
        Picasso.get().load(mascota.getFotoUrl()).into(holder.imageViewFoto);
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    public void updateMascotas(List<MascotaPerfil> newMascotas) {
        this.mascotas.clear();
        this.mascotas.addAll(newMascotas);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFoto;
        TextView textViewNombre;

        public ViewHolder(View itemView, Context context, List<MascotaPerfil> mascotas) {
            super(itemView);
            imageViewFoto = itemView.findViewById(R.id.imageViewFoto);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MascotaPerfil selectedMascota = mascotas.get(position);
                    Intent intent = new Intent(context, AnuncioInfo.class);
                    intent.putExtra("mascotaId", selectedMascota.getId());
                    intent.putExtra("isMascotaPerdida", selectedMascota.isMascotaPerdida());
                    intent.putExtra("nombre", selectedMascota.getNombre());
                    intent.putExtra("descripcion", selectedMascota.getDescripcion());
                    intent.putExtra("fotoUrl", selectedMascota.getFotoUrl());
                    intent.putExtra("telefono", selectedMascota.getTelefono());
                    intent.putExtra("ciudad", selectedMascota.getCiudad());
                    context.startActivity(intent);
                }
            });
        }
    }
}