package com.riberadeltajo.sebipetfinder.ui.AnimalesEncontrados;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.riberadeltajo.sebipetfinder.R;
import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {
    private ViewPager2 viewPagerFotos;
    private FotosUrlPagerAdapter fotosUrlPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        viewPagerFotos = findViewById(R.id.fullScreenViewPager);
        TabLayout tabLayout = findViewById(R.id.tabDotsFullScreen);

        String imageUrls = getIntent().getStringExtra("imageUrl");
        int currentPosition = getIntent().getIntExtra("position", 0);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            String[] fotosUrls = imageUrls.split(",");

            //adaptador
            fotosUrlPagerAdapter = new FotosUrlPagerAdapter(this, fotosUrls);
            viewPagerFotos.setAdapter(fotosUrlPagerAdapter);
            viewPagerFotos.setCurrentItem(currentPosition, false);

            //si hay más de una foto
            if (fotosUrls.length > 1) {
                tabLayout.setVisibility(View.VISIBLE);
                new TabLayoutMediator(tabLayout, viewPagerFotos,
                        (tab, position) -> {
                        }
                ).attach();
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        }

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }
}