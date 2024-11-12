package com.riberadeltajo.sebipetfinder.Principal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.riberadeltajo.sebipetfinder.R;
import com.riberadeltajo.sebipetfinder.databinding.ActivityMainPanelBinding;

public class MainPanelActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainPanelBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMainPanel.toolbar);

        binding.appBarMainPanel.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(MainPanelActivity.this, R.id.nav_host_fragment_content_main_panel);

                //Obtener el ID del fragmento actual
                int currentFragmentId = navController.getCurrentDestination().getId();

                //Verificar si estamos en el HomeFragment (mascotas perdidas)
                if (currentFragmentId == R.id.nav_home) {
                    Intent intent = new Intent(MainPanelActivity.this, NuevaMascotaPerdida.class);
                    startActivity(intent);
                }
                //Verificar si estamos en el GalleryFragment (mascotas encontradas)
                else if (currentFragmentId == R.id.nav_gallery) {
                    Intent intent = new Intent(MainPanelActivity.this, NuevaMascota.class);
                    startActivity(intent);
                }
                //Si no estamos en ninguno de los dos
                else {
                    Snackbar.make(view, "Selecciona una categoría para agregar una mascota", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_panel);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Ocultar el botón flotante si estamos en el SlideshowFragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_slideshow) {
                binding.appBarMainPanel.fab.hide();
            } else {
                binding.appBarMainPanel.fab.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_panel);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}