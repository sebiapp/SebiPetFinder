package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView imageView = findViewById(R.id.fullScreenImageView);
        String imageUrl = getIntent().getStringExtra("imageUrl");

        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(imageView);
        }

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }
}
