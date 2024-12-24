package com.riberadeltajo.sebipetfinder.Mensajes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.riberadeltajo.sebipetfinder.R;

import java.util.List;

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder> {
    private List<Mensaje> mensajes;
    private int usuarioActualId;
    private Context context;

    private static final int COLOR_MENSAJE_ENVIADO = Color.parseColor("#E3F2FD");
    private static final int COLOR_MENSAJE_RECIBIDO = Color.parseColor("#F5F5F5");

    public MensajeAdapter(Context context, List<Mensaje> mensajes, int usuarioActualId) {
        this.context = context;
        this.mensajes = mensajes;
        this.usuarioActualId = usuarioActualId;
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mensaje, parent, false);
        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        Log.d("MensajeAdapter", "Mensaje ID: " + mensaje.getEmisorId() + " | Usuario Actual ID: " + usuarioActualId);

        if (mensaje.getEmisorId() == usuarioActualId) {
            // Mensaje enviado por el usuario actual -> A la derecha
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.setMargins(50, 4, 8, 4);
            holder.mensajeCard.setCardBackgroundColor(COLOR_MENSAJE_ENVIADO);
            holder.mensajeText.setGravity(Gravity.END);
        } else {
            // Mensaje recibido -> A la izquierda
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.setMargins(8, 4, 50, 4);
            holder.mensajeCard.setCardBackgroundColor(COLOR_MENSAJE_RECIBIDO);
            holder.mensajeText.setGravity(Gravity.START);
        }


        holder.mensajeCard.setLayoutParams(params);
        holder.mensajeText.setText(mensaje.getMensaje());
        holder.fechaText.setText(mensaje.getFechaEnvio());
    }


    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        CardView mensajeCard;
        TextView mensajeText;
        TextView fechaText;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            mensajeCard = itemView.findViewById(R.id.mensajeCard);
            mensajeText = itemView.findViewById(R.id.mensajeText);
            fechaText = itemView.findViewById(R.id.fechaText);
        }
    }

}
