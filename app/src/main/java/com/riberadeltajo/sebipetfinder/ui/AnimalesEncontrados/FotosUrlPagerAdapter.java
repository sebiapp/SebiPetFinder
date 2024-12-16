package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;

public class FotosUrlPagerAdapter extends RecyclerView.Adapter<FotosUrlPagerAdapter.FotoViewHolder> {
    private String[] fotosUrls;
    private Context context;

    public FotosUrlPagerAdapter(Context context, String[] fotosUrls) {
        this.context = context;
        this.fotosUrls = fotosUrls;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String url = fotosUrls[position].trim();
        if (!url.isEmpty()) {
            if (!url.startsWith("http")) {
                url = "https://sienna-coyote-339198.hostingersite.com/" + url;
            }
            Picasso.get()
                    .load(url)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.ojoclose)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return fotosUrls != null ? fotosUrls.length : 0;
    }

    static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        FotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivFoto);
        }
    }
}