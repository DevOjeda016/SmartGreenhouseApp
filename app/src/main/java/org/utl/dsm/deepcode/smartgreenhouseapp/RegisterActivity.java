package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Invernadero;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Persona;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SignupRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SignupResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;
    private ScrollView scrollView;

    // Campos de entrada y sus layouts
    private TextInputEditText etNombre, etApellidos, etAdmin, etContrasenia, etInvernadero, etNumSerie;
    private TextInputLayout txtNombre, txtApellidos, txtAdmin, txtContrasenia, txtInvernadero, txtNumSerie;

    // Servicio de API
    private ApiService apiService;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Ajusta el padding de la vista principal para considerar barras del sistema y teclado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom + imeInsets.bottom
            );
            return insets;
        });

        initializeViews();
        setupTextFields();
        setupRetrofit();
        setupButton();
        setupScrollView();
    }

    private void initializeViews() {
        btnRegister = findViewById(R.id.btnRegister);
        scrollView = findViewById(R.id.scrollView);

        // Inicializar TextInputLayouts
        txtNombre = findViewById(R.id.txtNombre);
        txtApellidos = findViewById(R.id.txtApellidos);
        txtAdmin = findViewById(R.id.txtAdmin);
        txtContrasenia = findViewById(R.id.txtContrasenia);
        txtInvernadero = findViewById(R.id.txtInvernadero);
        txtNumSerie = findViewById(R.id.txtNumSerie);

        // Inicializar EditTexts
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etAdmin = findViewById(R.id.etAdmin);
        etContrasenia = findViewById(R.id.etContrasenia);
        etInvernadero = findViewById(R.id.etInvernadero);
        etNumSerie = findViewById(R.id.etNumSerie);
    }

    private void setupTextFields() {
        // Configuración de capitalización y validación en tiempo real para nombre y apellidos
        setupCapitalization(etNombre, txtNombre);
        setupCapitalization(etApellidos, txtApellidos);
        // Configuración para limpiar errores al enfocar o desenfocar los campos
        setupErrorCleaner(etNombre, txtNombre);
        setupErrorCleaner(etApellidos, txtApellidos);
        setupErrorCleaner(etAdmin, txtAdmin);
        setupErrorCleaner(etContrasenia, txtContrasenia);
        setupErrorCleaner(etInvernadero, txtInvernadero);
        setupErrorCleaner(etNumSerie, txtNumSerie);
    }

    /**
     * Agrega un TextWatcher que asegura que la primera letra esté en mayúscula y valida el campo.
     */
    private void setupCapitalization(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

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
                if (isUpdating) return;
                if (s.length() > 0 && Character.isLowerCase(s.charAt(0))) {
                    isUpdating = true;
                    String capitalized = Character.toUpperCase(s.charAt(0)) + s.toString().substring(1);
                    editText.setText(capitalized);
                    editText.setSelection(capitalized.length());
                    isUpdating = false;
                }
                // Validación en tiempo real del campo
                validateSingleField(s.toString(), layout);
            }
        });
    }

    /**
     * Valida que el campo no esté vacío y asigna error si es necesario.
     */
    private void validateSingleField(String value, TextInputLayout layout) {
        if (value.isEmpty()) {
            layout.setError("Este campo es obligatorio");
        } else {
            layout.setError(null);
        }
    }

    /**
     * Configura un listener para limpiar el error cuando el campo gana foco y validar al perderlo.
     */
    private void setupErrorCleaner(TextInputEditText editText, TextInputLayout layout) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layout.setError(null);
            } else {
                validateField(editText, layout, "Este campo es obligatorio");
            }
        });
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void setupButton() {
        btnRegister.setOnClickListener(v -> {
            if (validateAllFields()) {
                performSignup(
                        etAdmin.getText().toString().trim(),
                        etContrasenia.getText().toString().trim(),
                        new Persona(
                                etNombre.getText().toString().trim(),
                                etApellidos.getText().toString().trim(),
                                ""
                        ),
                        "USUARIO",
                        new Invernadero(
                                etInvernadero.getText().toString().trim(),
                                etNumSerie.getText().toString().trim(),
                                "Modelo X"
                        )
                );
            }
        });
    }

    /**
     * Valida que todos los campos requeridos tengan contenido correcto.
     */
    private boolean validateAllFields() {
        return validateField(etNombre, txtNombre, "Por favor, ingrese su nombre") &&
                validateField(etApellidos, txtApellidos, "Por favor, ingrese sus apellidos") &&
                validateField(etAdmin, txtAdmin, "Por favor, ingrese su nombre de usuario") &&
                validatePassword() &&
                validateField(etInvernadero, txtInvernadero, "Por favor, ingrese el nombre de su invernadero") &&
                validateField(etNumSerie, txtNumSerie, "Por favor, ingrese el número de serie");
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
     * Valida la contraseña asegurando que cumpla con ciertos criterios.
     */
    private boolean validatePassword() {
        String password = etContrasenia.getText().toString().trim();
        List<String> missingCriteria = new ArrayList<>();

        if (password.length() < 12) missingCriteria.add("12 o más caracteres");
        if (!password.matches(".*[A-Z].*")) missingCriteria.add("1 mayúscula");
        if (!password.matches(".*[a-z].*")) missingCriteria.add("1 minúscula");
        if (!password.matches(".*\\d.*")) missingCriteria.add("1 número");
        if (!password.matches(".*[!@#$%^&*()].*")) missingCriteria.add("1 carácter especial");

        if (!missingCriteria.isEmpty()) {
            String errorMessage = "La contraseña debe tener:\n• " + String.join("\n• ", missingCriteria);
            txtContrasenia.setError(errorMessage);
            return false;
        }
        txtContrasenia.setError(null);
        return true;
    }

    /**
     * Realiza el proceso de registro (signup) haciendo la llamada a la API.
     */
    private void performSignup(String nombreUsuario, String contrasenia, Persona persona, String rol, Invernadero invernadero) {
        SignupRequest signupRequest = new SignupRequest(nombreUsuario, contrasenia, persona, rol, invernadero);

        apiService.signup(signupRequest).enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                handleSignupResponse(response);
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Maneja la respuesta de la API luego de intentar el registro.
     */
    private void handleSignupResponse(Response<SignupResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            SignupResponse signupResponse = response.body();
            switch (signupResponse.getStatus()) {
                case 201:
                    startActivity(new Intent(this, LoginActivity.class));
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case 409:
                    txtAdmin.setError("Este usuario ya existe");
                    break;
                default:
                    Toast.makeText(this, "Error: " + signupResponse.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error en el servidor", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ajusta el scroll para que el campo enfocado sea visible al aparecer el teclado.
     */
    private void setupScrollView() {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visibleArea = new Rect();
            scrollView.getWindowVisibleDisplayFrame(visibleArea);
            int screenHeight = scrollView.getRootView().getHeight();

            // Si la diferencia es mayor al 15% de la altura, se asume que el teclado está visible.
            if (screenHeight - visibleArea.bottom > screenHeight * 0.15) {
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    int[] location = new int[2];
                    focusedView.getLocationOnScreen(location);
                    int scrollY = (location[1] + focusedView.getHeight()) - visibleArea.height();
                    if (scrollY > 0) scrollView.smoothScrollTo(0, scrollY);
                }
            }
        });
    }
}
