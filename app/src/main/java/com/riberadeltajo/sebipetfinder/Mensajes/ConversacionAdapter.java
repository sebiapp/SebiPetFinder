package com.riberadeltajo.sebipetfinder.Mensajes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ConversacionAdapter extends RecyclerView.Adapter<ConversacionAdapter.ConversacionViewHolder> {
    private List<Conversacion> conversaciones;
    private Context context;

    public ConversacionAdapter(Context context, List<Conversacion> conversaciones) {
        this.context = context;
        this.conversaciones = conversaciones;
    }

    @NonNull
    @Override
    public ConversacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversacion, parent, false);
        return new ConversacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversacionViewHolder holder, int position) {
        Conversacion conversacion = conversaciones.get(position);
        holder.tituloAnuncio.setText(conversacion.getTituloAnuncio());
        holder.ultimoMensaje.setText(conversacion.getUltimoMensaje());
        holder.fechaMensaje.setText(conversacion.getFechaUltimoMensaje());

        //solo cargar la primera foto
        if (conversacion.getImagenUrl() != null && !conversacion.getImagenUrl().isEmpty()) {
            String[] fotos = conversacion.getImagenUrl().split(",");
            if (fotos.length > 0) {
                String primeraFoto = fotos[0].trim();
                if (!primeraFoto.startsWith("http")) {
                    primeraFoto = "https://sienna-coyote-339198.hostingersite.com/" + primeraFoto;
                }
                Picasso.get()
                        .load(primeraFoto)
                        .placeholder(R.drawable.baseline_pets_24)
                        .error(R.drawable.baseline_pets_24)
                        .fit()
                        .centerCrop()
                        .into(holder.imagenAnuncio);
            }
        }

        if (conversacion.getMensajesNoLeidos() > 0) {
            holder.contadorNoLeidos.setVisibility(View.VISIBLE);
            holder.contadorNoLeidos.setText(String.valueOf(conversacion.getMensajesNoLeidos()));
        } else {
            holder.contadorNoLeidos.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d("ConversacionAdapter", "Abriendo chat:");
            Log.d("ConversacionAdapter", "anuncioId: " + conversacion.getAnuncioId());
            Log.d("ConversacionAdapter", "tipoAnuncio: " + conversacion.getTipoAnuncio());
            Log.d("ConversacionAdapter", "otroUsuarioId: " + conversacion.getOtroUsuarioId());
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("anuncioId", conversacion.getAnuncioId());
            intent.putExtra("tipoAnuncio", conversacion.getTipoAnuncio());
            intent.putExtra("otroUsuarioId", conversacion.getOtroUsuarioId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversaciones.size();
    }

    public void actualizarConversaciones(List<Conversacion> nuevasConversaciones) {
        this.conversaciones = nuevasConversaciones;
        notifyDataSetChanged();
    }

    static class ConversacionViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenAnuncio;
        TextView tituloAnuncio;
        TextView ultimoMensaje;
        TextView fechaMensaje;
        TextView contadorNoLeidos;

        public ConversacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenAnuncio = itemView.findViewById(R.id.imagenAnuncio);
            tituloAnuncio = itemView.findViewById(R.id.tituloAnuncio);
            ultimoMensaje = itemView.findViewById(R.id.ultimoMensaje);
            fechaMensaje = itemView.findViewById(R.id.fechaMensaje);
            contadorNoLeidos = itemView.findViewById(R.id.contadorNoLeidos);
        }
    }
}