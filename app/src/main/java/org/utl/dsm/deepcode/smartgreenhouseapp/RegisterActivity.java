package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.ApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Invernadero;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Persona;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SignupRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SignupResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    Button btnRegister;
    ScrollView scrollView;

    // Campos de entrada
    private TextInputEditText etNombre, etApellidos, etAdmin, etContrasenia, etInvernadero, etNumSerie;

    // Servicio de API
    private ApiService apiService;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        btnRegister = findViewById(R.id.btnRegister);
        scrollView = findViewById(R.id.scrollView);

        // Inicializar los campos de texto correctamente
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etAdmin = findViewById(R.id.etAdmin);
        etContrasenia = findViewById(R.id.etContrasenia);
        etInvernadero = findViewById(R.id.etInvernadero);
        etNumSerie = findViewById(R.id.etNumSerie);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.16.30.174:8080/SmartGreenhouse/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Configurar el botón de registro
        btnRegister.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();
            String apellidos = etApellidos.getText().toString();
            String admin = etAdmin.getText().toString();
            String contrasenia = etContrasenia.getText().toString();
            String invernadero = etInvernadero.getText().toString();
            String numSerie = etNumSerie.getText().toString();

            if (nombre.isEmpty() || apellidos.isEmpty() || admin.isEmpty() || contrasenia.isEmpty() || invernadero.isEmpty() || numSerie.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // Crear objetos Persona e Invernadero
                Persona persona = new Persona(nombre, apellidos, ""); // Apellido materno vacío
                Invernadero invernaderoObj = new Invernadero(invernadero, numSerie, "Modelo X"); // Modelo por defecto

                // Realizar el registro
                performSignup(admin, contrasenia, persona, "USUARIO", invernaderoObj);
            }
        });

        // Configurar el listener para ocultar el teclado al tocar fuera de los campos de texto
        View mainLayout = findViewById(R.id.main);
        mainLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });

        // Configurar el ScrollView para que se ajuste al teclado
        setupScrollView();
    }

    // Método para realizar el registro
    private void performSignup(String nombreUsuario, String contrasenia, Persona persona, String rol, Invernadero invernadero) {
        SignupRequest signupRequest = new SignupRequest(nombreUsuario, contrasenia, persona, rol, invernadero);

        Call<SignupResponse> call = apiService.signup(signupRequest);
        call.enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SignupResponse signupResponse = response.body();

                    if (signupResponse.getStatus() == 201) {
                        // Registro exitoso
                        Toast.makeText(RegisterActivity.this, signupResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Redirigir a la actividad de login
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Manejo de otros códigos de estado
                        switch (signupResponse.getStatus()) {
                            case 409:
                                Toast.makeText(RegisterActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                                break;
                            case 400:
                                Toast.makeText(RegisterActivity.this, "Solicitud incorrecta", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(RegisterActivity.this, "Error: " + signupResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Error en la respuesta
                    Toast.makeText(RegisterActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                // Error de conexión
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para ocultar el teclado
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Método para configurar el ScrollView
    private void setupScrollView() {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Verificar si el teclado está visible
            int scrollViewHeight = scrollView.getRootView().getHeight();
            int scrollViewBottom = scrollView.getBottom();
            int keyboardHeight = scrollViewHeight - scrollViewBottom;

            if (keyboardHeight > scrollViewHeight * 0.15) { // Teclado visible
                // Desplazar el ScrollView para enfocar el campo de texto activo
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    scrollView.smoothScrollTo(0, focusedView.getBottom());
                }
            }
        });
    }
}