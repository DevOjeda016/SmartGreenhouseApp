package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.ApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.LoginRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.UsuarioDTO;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView txtRegisterNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Configura el padding para la vista principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener los layouts primero
        TextInputLayout userLayout = findViewById(R.id.txtNombreUsu);
        TextInputLayout passwordLayout = findViewById(R.id.txtContrasenia);

        // Luego obtener los EditText dentro de los layouts
        etUsername = (TextInputEditText) userLayout.getEditText();
        etPassword = (TextInputEditText) passwordLayout.getEditText();

        btnLogin = findViewById(R.id.btnLogin);
        txtRegisterNewUser = findViewById(R.id.txtRegisterNewUser);

        // Configurar el botón de login
        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(username, password);
            }
        });

        // Configurar el texto de registro
        txtRegisterNewUser.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin(String username, String password) {
        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Crear el cuerpo de la solicitud
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Realizar la solicitud
        Call<ApiResponse<UsuarioDTO>> call = apiService.login(loginRequest);
        call.enqueue(new Callback<ApiResponse<UsuarioDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UsuarioDTO>> call, Response<ApiResponse<UsuarioDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UsuarioDTO> apiResponse = response.body();

                    // Verificar que la respuesta contiene datos
                    if (apiResponse.getData() != null) {
                        UsuarioDTO usuario = apiResponse.getData();

                        // Verificar que el rol no es 0
                        if (usuario.getIdRol() != 0) {
                            // Redirigir según el rol
                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    usuario.getIdRol() == 1
                                            ? menu_admin.class
                                            : menu_usuario.class
                                    );

                            // Pasar datos del usuario
                            intent.putExtra("id", usuario.getId());
                            intent.putExtra("nombre", usuario.getNombre());
                            intent.putExtra("aPaterno", usuario.getaPaterno());
                            intent.putExtra("idRol", usuario.getIdRol());

                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Rol de usuario no válido", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Mostrar mensaje de error de la API o uno por defecto
                        String errorMsg = apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Credenciales incorrectas";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Manejar errores de la API
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        Log.e("LoginActivity", "Error del servidor: " + errorBody);
                        Toast.makeText(LoginActivity.this, "Error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("LoginActivity", "Error al leer respuesta", e);
                        Toast.makeText(LoginActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UsuarioDTO>> call, Throwable t) {
                // Error de conexión
                Log.e("LoginActivity", "Error de conexión: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}