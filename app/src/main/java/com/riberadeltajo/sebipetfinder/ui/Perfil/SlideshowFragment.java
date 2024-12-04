package com.riberadeltajo.sebipetfinder.ui.Perfil;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.riberadeltajo.sebipetfinder.Interfaces.ApiService;
import com.riberadeltajo.sebipetfinder.databinding.FragmentSlideshowBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SlideshowFragment extends Fragment {
    private Button btnGestionar;
    private FragmentSlideshowBinding binding;
    private RecyclerView recyclerView;
    private MascotaAdapterPerfil mascotaAdapter;
    private ApiService apiService;
    String nombre ="";
    String apellidos = "";
    String usuario = "";
    String correo = "";
    String contraseña = "";
    int userId=0;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mascotaAdapter = new MascotaAdapterPerfil(getContext(), new ArrayList<>());
        recyclerView.setAdapter(mascotaAdapter);
        btnGestionar=binding.btnGestionar;
        apiService = new Retrofit.Builder()
                .baseUrl("https://sienna-coyote-339198.hostingersite.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        loadUserDetails();
        loadAnuncios();

        btnGestionar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean isGoogleLogin = getContext()
                        .getSharedPreferences("user_data", MODE_PRIVATE)
                        .getBoolean("isGoogleLogin", false);
                if(isGoogleLogin){
                    new AlertDialog.Builder(getContext())
                            .setTitle("Gestión de datos con Google")
                            .setMessage("Has iniciado sesión con Google. Para gestionar tus datos, accede a tu cuenta de Google desde" +
                                    " https://myaccount.google.com/")
                            .setPositiveButton("Entendido", null)
                            .show();
                }else{
                    Intent intent = new Intent(getContext(), PerfilInfo.class);
                    intent.putExtra("id",userId);
                    intent.putExtra("nom",nombre);
                    intent.putExtra("ape",apellidos);
                    intent.putExtra("user",usuario);
                    intent.putExtra("email",correo);
                    intent.putExtra("contra",contraseña);
                    startActivity(intent);
                }



            }
        });


        return root;
    }

    private void loadUserDetails() {
        //Obtener el userId desde SharedPreferences
        userId = getActivity().getSharedPreferences("user_data", getContext().MODE_PRIVATE)
                .getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        //Realizar la llamada a la API para obtener el usuario por ID
        Call<JsonObject> call = apiService.getUsuarioPorId(userId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonObject = response.body();
                    nombre = jsonObject.get("nombre_usuario").getAsString();
                    apellidos = jsonObject.get("apellidos_usuario").getAsString();
                    usuario = jsonObject.get("usuario").getAsString();
                    correo = jsonObject.get("correo").getAsString();
                    contraseña = jsonObject.get("contraseña").getAsString();
                    String saludo = "¡Hola, " + nombre + " " + apellidos + "!";
                    TextView txtBienvenido = binding.txtBienvenido;
                    txtBienvenido.setText(saludo);
                } else {
                    Toast.makeText(getContext(), "No se pudieron recuperar los detalles del usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("error",t.getMessage());
            }
        });
    }

    private void loadAnuncios() {
        int userId = getActivity().getSharedPreferences("user_data", getContext().MODE_PRIVATE)
                .getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<JsonObject>> call = apiService.getAnuncios(userId);
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<JsonObject> anunciosJson = response.body();
                    List<MascotaPerfil> anuncios = new ArrayList<>();

                    for (JsonObject anuncioJson : anunciosJson) {
                        MascotaPerfil mascota = new MascotaPerfil(
                                anuncioJson.get("id").getAsInt(),
                                anuncioJson.get("nombre").getAsString(),
                                anuncioJson.get("descripcion").getAsString(),
                                anuncioJson.get("fotoUrl").getAsString(),
                                anuncioJson.get("telefono").getAsString(),
                                anuncioJson.get("ciudad").getAsString(),
                                anuncioJson.get("isMascotaPerdida").getAsInt()
                        );
                        anuncios.add(mascota);
                    }

                    Log.d("SlideshowFragment", "Anuncios recibidos: " + anuncios.size());
                    mascotaAdapter.updateMascotas(anuncios);
                } else {
                    Toast.makeText(getContext(), "No se pudieron recuperar los anuncios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SlideshowFragment", "Error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}