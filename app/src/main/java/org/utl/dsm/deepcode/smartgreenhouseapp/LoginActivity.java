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
import org.utl.dsm.deepcode.smartgreenhouseapp.model.LoginResponse;

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
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

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
                .baseUrl(Globals.BASE_URL) // Asegúrate de que termine con /
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Crear el cuerpo de la solicitud
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Realizar la solicitud
        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Intent intent = new Intent(
                            LoginActivity.this,
                            loginResponse.getdata().getIdRol() == 1
                                    ? menu_admin.class
                                    : menu_usuario.class
                    );
                    intent.putExtra("id", loginResponse.getdata().getId());
                    intent.putExtra("nombre", loginResponse.getdata().getNombre());
                    intent.putExtra("aPaterno", loginResponse.getdata().getaPaterno());
                    intent.putExtra("idRol", loginResponse.getdata().getIdRol());
                    startActivity(intent);
                    finish();
                } else {
                    // Error en la respuesta
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("LoginActivity", "Error en la respuesta del servidor: " + errorBody);
                        Toast.makeText(LoginActivity.this, "Error en la respuesta del servidor: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Error de conexión
                Log.e("LoginActivity", "Error de conexión: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}