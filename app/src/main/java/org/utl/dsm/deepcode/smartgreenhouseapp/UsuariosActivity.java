package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.api.UsuarioApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.UsuarioData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UsuariosActivity extends AppCompatActivity {

    // Componentes de UI
    private RecyclerView recyclerView;
    private View progressBar;
    private TextView tvError;
    private MaterialButton backButton;
    private FloatingActionButton fabAddUser;

    // Servicios y adaptador
    private UsuarioAdapter adapter;
    private UsuarioApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        setupViews();
        setupRetrofit();
        setupRecyclerView();
        setupListeners();
        loadUsuarios();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        backButton = findViewById(R.id.iconButton);
        fabAddUser = findViewById(R.id.fabAddUser);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(UsuarioApiService.class);
    }

    private void setupRecyclerView() {
        adapter = new UsuarioAdapter(new UsuarioAdapter.OnUsuarioClickListener() {
            @Override
            public void onEditClick(UsuarioData usuario) {
                handleEditUser(usuario);
            }

            @Override
            public void onDeleteClick(UsuarioData usuario) {
                showDeleteConfirmation(usuario);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        fabAddUser.setOnClickListener(v -> handleAddUser());
    }

    private void loadUsuarios() {
        showLoading(true);

        apiService.getUsuarios().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<UsuarioData> usuarios = response.body().getData();
                    if (usuarios != null && !usuarios.isEmpty()) {
                        adapter.setUsuarios(usuarios); // Cambiado de updateUsuarios a setUsuarios
                    } else {
                        showEmptyState();
                    }
                } else {
                    showError("Error al cargar usuarios: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showLoading(false);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmation(UsuarioData usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar al usuario " + usuario.getNombreUsuario() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteUsuario(usuario))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteUsuario(UsuarioData usuario) {
        showLoading(true);

        apiService.deleteUsuario(usuario.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UsuariosActivity.this,
                            "Usuario eliminado correctamente",
                            Toast.LENGTH_SHORT).show();
                    // Recargar la lista de usuarios
                    loadUsuarios();
                } else {
                    showLoading(false);
                    showError("Error al eliminar usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showLoading(false);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        tvError.setText("No hay usuarios registrados");
        tvError.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void handleEditUser(UsuarioData usuario) {
        // Crear Intent para redirigir a ActualizarUsuarioActivity
        Intent intent = new Intent(UsuariosActivity.this, actualizar_usuario.class);

        // Pasar el objeto usuario como extra (asegúrate que UsuarioData implementa Serializable)
        intent.putExtra("usuario", usuario);

        // Iniciar la actividad
        startActivity(intent);
    }

    private void handleAddUser() {
        Toast.makeText(this, "Agregar nuevo usuario", Toast.LENGTH_SHORT).show();
        // Lógica para añadir usuario
    }

    /**
     * Clase Adapter como clase interna
     */
    private static class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

        private List<UsuarioData> usuarios;
        private final OnUsuarioClickListener listener;

        public interface OnUsuarioClickListener {
            void onEditClick(UsuarioData usuario);
            void onDeleteClick(UsuarioData usuario);
        }

        UsuarioAdapter(OnUsuarioClickListener listener) {
            this.usuarios = new ArrayList<>();
            this.listener = listener;
        }

        @NonNull
        @Override
        public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UsuarioViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
            UsuarioData usuario = usuarios.get(position);
            if (usuario == null) return;

            // Configurar vistas con datos del usuario
            holder.tvUsername.setText(usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "N/A");

            String fullName = formatFullName(usuario);
            holder.tvFullName.setText(fullName.isEmpty() ? "Nombre no disponible" : fullName);

            holder.tvRol.setText("Rol: " + (usuario.getRol() != null ? usuario.getRol() : "N/A"));

            String invernadero = "Invernadero: " +
                    (usuario.getInvernadero() != null && usuario.getInvernadero().getNombre() != null ?
                            usuario.getInvernadero().getNombre() : "No asignado");
            holder.tvInvernadero.setText(invernadero);

            // Configurar listeners de botones
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(usuario));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(usuario));
        }

        @Override
        public int getItemCount() {
            return usuarios.size();
        }

        public void setUsuarios(List<UsuarioData> usuarios) {
            this.usuarios = usuarios != null ? usuarios : new ArrayList<>();
            notifyDataSetChanged();
        }

        private String formatFullName(UsuarioData usuario) {
            if (usuario.getPersona() == null) return "";

            return String.format("%s %s %s",
                    usuario.getPersona().getNombre() != null ? usuario.getPersona().getNombre() : "",
                    usuario.getPersona().getAPaterno() != null ? usuario.getPersona().getAPaterno() : "",
                    usuario.getPersona().getAMaterno() != null ? usuario.getPersona().getAMaterno() : ""
            ).trim();
        }

        static class UsuarioViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername, tvFullName, tvRol, tvInvernadero;
            ImageButton btnEdit, btnDelete;

            UsuarioViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvFullName = itemView.findViewById(R.id.tvFullName);
                tvRol = itemView.findViewById(R.id.tvRol);
                tvInvernadero = itemView.findViewById(R.id.tvInvernadero);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

}