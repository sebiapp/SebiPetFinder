package com.riberadeltajo.sebipetfinder.Principal;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.riberadeltajo.sebipetfinder.R;

import java.util.List;

public class FotosPagerAdapter extends RecyclerView.Adapter<FotosPagerAdapter.FotoViewHolder> {
    private List<Uri> fotos;
    private Context context;

    public FotosPagerAdapter(Context context, List<Uri> fotos) {
        this.context = context;
        this.fotos = fotos;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        holder.imageView.setImageURI(fotos.get(position));
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    public void updateFotos(List<Uri> newFotos) {
        this.fotos = newFotos;
        notifyDataSetChanged();
    }

    static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        FotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivFoto);
        }
    }
}
