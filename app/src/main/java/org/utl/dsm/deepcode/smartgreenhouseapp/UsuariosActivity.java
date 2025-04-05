package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.UsuarioApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.UsuarioData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UsuariosActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_USER = 1;

    private RecyclerView recyclerView;
    private MaterialButton iconButton;

    private UsuarioAdapter adapter;
    private UsuarioApiService apiService;
    private List<UsuarioData> usuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuarios);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRetrofit();
        setupRecyclerView();
        loadUsuarios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar lista cada vez que se vuelve a esta actividad
        // Esto captura actualizaciones hechas en otros módulos
        loadUsuarios();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewUsers);
        iconButton = findViewById(R.id.iconButton);
        iconButton.setOnClickListener(v -> finish());
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(UsuarioApiService.class);
    }

    private void setupRecyclerView() {
        adapter = new UsuarioAdapter(usuarios, new UsuarioAdapter.OnUsuarioClickListener() {
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

    private void loadUsuarios() {
        showLoading(true);

        apiService.getUsuarios().enqueue(new Callback<ApiResponse<List<UsuarioData>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UsuarioData>>> call, Response<ApiResponse<List<UsuarioData>>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<UsuarioData> nuevosUsuarios = response.body().getData();
                    if (nuevosUsuarios != null && !nuevosUsuarios.isEmpty()) {
                        usuarios.clear();
                        usuarios.addAll(nuevosUsuarios);
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showError("Error al cargar usuarios: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UsuarioData>>> call, Throwable t) {
                showLoading(false);
                showError("Error de conexión: " + t.getMessage());
                Log.e("UsuariosActivity", "Error al cargar usuarios", t);
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

        apiService.deleteUsuario(usuario.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UsuariosActivity.this,
                            "Usuario eliminado correctamente",
                            Toast.LENGTH_SHORT).show();

                    // Eliminar localmente sin recargar toda la lista
                    int position = findUsuarioPosition(usuario.getId());
                    if (position != -1) {
                        usuarios.remove(position);
                        adapter.notifyItemRemoved(position);

                        // Verificar si la lista está vacía después de eliminar
                        if (usuarios.isEmpty()) {
                            showEmptyState();
                        }
                    }
                } else {
                    showError("Error al eliminar usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                showError("Error de conexión: " + t.getMessage());
                Log.e("UsuariosActivity", "Error al eliminar usuario", t);
            }
        });
    }

    private void handleEditUser(UsuarioData usuario) {
        Intent intent = new Intent(this, PerfilActivity.class);
        intent.putExtra("usuario", usuario);
        intent.putExtra("isEditing", true); // Indicador de edición
        startActivityForResult(intent, REQUEST_EDIT_USER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_USER) {
            // Recargar lista sin importar el resultado
            // Esto asegura que cualquier cambio se refleje
            loadUsuarios();
        }
    }

    private int findUsuarioPosition(int idUsuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId() == idUsuario) {
                return i;
            }
        }
        return -1;
    }

    private void showLoading(boolean show) {
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        recyclerView.setVisibility(View.GONE);
    }

    // Clase Adapter interna
    private static class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

        private List<UsuarioData> usuarios;
        private final OnUsuarioClickListener listener;

        public interface OnUsuarioClickListener {
            void onEditClick(UsuarioData usuario);
            void onDeleteClick(UsuarioData usuario);
        }

        public UsuarioAdapter(List<UsuarioData> usuarios, OnUsuarioClickListener listener) {
            this.usuarios = usuarios;
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

            holder.tvUsername.setText(usuario.getNombreUsuario());

            String nombreCompleto = usuario.getPersona().getNombre() + " " +
                    usuario.getPersona().getAPaterno() + " " +
                    usuario.getPersona().getAMaterno();
            holder.tvFullName.setText(nombreCompleto);

            // Mostrar el rol del usuario si existe
            if (usuario.getRol() != null && usuario.getRol() != null) {
                holder.tvRol.setText("Rol: " + usuario.getRol());
                holder.tvRol.setVisibility(View.VISIBLE);
            } else {
                holder.tvRol.setVisibility(View.GONE);
            }

            // Mostrar el nombre del invernadero si existe
            if (usuario.getInvernadero() != null && usuario.getInvernadero().getNombre() != null) {
                holder.tvInvernadero.setText("Invernadero: " + usuario.getInvernadero().getNombre());
                holder.tvInvernadero.setVisibility(View.VISIBLE);
            } else {
                holder.tvInvernadero.setVisibility(View.GONE);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(usuario));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(usuario));
        }

        @Override
        public int getItemCount() {
            return usuarios.size();
        }

        static class UsuarioViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername, tvFullName, tvRol, tvInvernadero;
            ImageButton btnEdit, btnDelete;

            public UsuarioViewHolder(@NonNull View itemView) {
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