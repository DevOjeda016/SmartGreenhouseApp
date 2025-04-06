package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
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
    private TextInputLayout userLayout, passwordLayout;
    private Button btnLogin;
    private TextView txtRegisterNewUser;
    private ScrollView scrollView;

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
        userLayout = findViewById(R.id.txtNombreUsu);
        passwordLayout = findViewById(R.id.txtContrasenia);

        // Luego obtener los EditText dentro de los layouts
        etUsername = (TextInputEditText) userLayout.getEditText();
        etPassword = (TextInputEditText) passwordLayout.getEditText();

        btnLogin = findViewById(R.id.btnLogin);
        scrollView = findViewById(R.id.scrollview);
        txtRegisterNewUser = findViewById(R.id.txtRegisterNewUser);


        // Configurar el botón de login
        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (username.isEmpty()) {
                userLayout.setError("Por favor, ingresa un nombre de usuario");
                return;
            } else {
                userLayout.setError(null);
            }
            if (password.isEmpty()) {
                passwordLayout.setError("Por favor, ingresa una contraseña");
                return;
            } else {
                passwordLayout.setError(null);
            }
            performLogin(username, password);
        });

        // Configurar el texto de registro
        txtRegisterNewUser.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        setupScrollView();
        setupErrorCleaner(etUsername, userLayout);
        setupErrorCleaner(etPassword, passwordLayout);
        inputChanged(etUsername, userLayout);
        inputChanged(etPassword, passwordLayout);
    }

    private void inputChanged(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Validación en tiempo real del campo
                validateSingleField(s.toString(), layout);
            }
        });
    }

    private void validateSingleField(String value, TextInputLayout layout) {
        if (value.isEmpty()) {
            layout.setError("Este campo es obligatorio");
        } else {
            layout.setError(null);
        }
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
                        Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
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

    private void setupErrorCleaner(TextInputEditText editText, TextInputLayout layout) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layout.setError(null);
            } else {
                validateField(editText, layout, "Este campo es obligatorio");
            }
        });
    }

    private boolean validateField(TextInputEditText editText, TextInputLayout layout, String errorMessage) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            layout.setError(errorMessage);
            return false;
        }
        layout.setError(null);
        return true;
    }

    /**
     * Ajusta el scroll para que el campo enfocado sea visible al aparecer el teclado.
     */
        // En lugar de tu método setupScrollView actual
        private void setupScrollView() {
            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
                // Ajusta el padding inferior del ScrollView para dar espacio al teclado
                int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                scrollView.setPadding(0, 0, 0, imeHeight);
                return insets;
            });
        }
}