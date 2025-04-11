package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.UsuarioApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Invernadero;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Persona;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.UsuarioData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerfilActivity extends AppCompatActivity {

    private static final String TAG = "PerfilActivity";

    // Views
    private ImageView profileImage;
    private TextView txtTitle;
    private TextView tvUsername, tvRole;
    private TextInputEditText etFirstName, etLastName, etUsername,
            etGreenhouseName, etSerialNumber, etModel;
    private TextInputLayout tilFirstName, tilLastName, tilUsername, tilGreenhouseName, tilSerialNumber, tilModel;

    private MaterialButton btnUpdate, btnLogout, iconButton;
    private ProgressBar progressBar;

    // Datos
    private UsuarioData usuario;
    private ScrollView scrollView;
    private UsuarioApiService apiService;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private boolean isEditing = false;
    UsuarioData usuarioLogged = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtTitle = findViewById(R.id.txtTitle);

        // 1. Configurar Retrofit
        setupRetrofit();

        // 2. Inicializar vistas
        initViews();

        // 3. Verificar si hay datos del usuario en el Intent
        if (getIntent().hasExtra("usuario")) {
            usuario = (UsuarioData) getIntent().getSerializableExtra("usuario");
            isEditing = getIntent().getBooleanExtra("isEditing", false);
            usuarioLogged = (UsuarioData) getIntent().getSerializableExtra("usuarioLogged");

            // Cargar los datos del usuario recibido
            if (usuario != null) {
                Log.d(TAG, "Usuario recibido via Intent - ID: " + usuario.getId());
                loadUserDataToUI();
            } else {
                // Si por alguna razón el usuario es null, cargar desde API
                loadUserDataFromApi();
            }
        } else {
            // Si no hay usuario en el intent, cargar desde API (comportamiento anterior)
            loadUserDataFromApi();
        }

        // 4. Configurar listeners
        setupListeners();
        if (usuarioLogged != null) {
            if (usuarioLogged.getId() != usuario.getId()) {
                btnLogout.setVisibility(View.GONE);
                txtTitle.setText(getString(R.string.UserUser));
            }

            if (!usuario.getRol().equals("ADMINISTRADOR")) {
                etGreenhouseName.setEnabled(false);
                etSerialNumber.setEnabled(false);
                etModel.setEnabled(false);
            }
        }
        setupScrollView();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(UsuarioApiService.class);
    }

    private void initViews() {
        scrollView = findViewById(R.id.scrollView);
        profileImage = findViewById(R.id.profile_image);
        tvUsername = findViewById(R.id.tv_username);
        tvRole = findViewById(R.id.tv_role);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etUsername = findViewById(R.id.et_username);
        etGreenhouseName = findViewById(R.id.et_greenhouse_name);
        etSerialNumber = findViewById(R.id.et_serial_number);
        etModel = findViewById(R.id.et_model);
        tilFirstName = findViewById(R.id.til_first_name);
        tilLastName = findViewById(R.id.til_last_name);
        tilUsername = findViewById(R.id.til_username);
        tilGreenhouseName = findViewById(R.id.til_greenhouse_name);
        tilSerialNumber = findViewById(R.id.til_serial_number);
        tilModel = findViewById(R.id.til_model);
        inputChanged(etUsername, tilUsername);
        inputChanged(etFirstName, tilFirstName);
        inputChanged(etLastName, tilLastName);
        inputChanged(etGreenhouseName, tilGreenhouseName);
        inputChanged(etSerialNumber, tilSerialNumber);
        inputChanged(etModel, tilModel);
        setupErrorCleaner(etUsername, tilUsername);
        setupErrorCleaner(etFirstName, tilFirstName);
        setupErrorCleaner(etLastName, tilLastName);
        setupErrorCleaner(etGreenhouseName, tilGreenhouseName);
        setupErrorCleaner(etSerialNumber, tilSerialNumber);
        setupErrorCleaner(etModel, tilModel);
        tilModel = findViewById(R.id.til_model);
        btnUpdate = findViewById(R.id.btn_update);
        btnLogout = findViewById(R.id.btn_logout);
        progressBar = findViewById(R.id.progressBar);
        iconButton = findViewById(R.id.iconButton);
        iconButton.setOnClickListener(v -> finish());
    }

    private void loadUserDataFromApi() {
        //showProgress(true);
        Call<ApiResponse<List<UsuarioData>>> call = apiService.getUsuarios();
        call.enqueue(new Callback<ApiResponse<List<UsuarioData>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UsuarioData>>> call, Response<ApiResponse<List<UsuarioData>>> response) {
                //showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UsuarioData>> apiResponse = response.body();
                    if (apiResponse.getStatus() == 200 && !apiResponse.getData().isEmpty()) {
                        // Tomar el primer usuario si no estamos en modo edición
                        usuario = apiResponse.getData().get(0);

                        // Log para verificar IDs
                        Log.d(TAG, "Usuario ID cargado desde API: " + usuario.getId());
                        if (usuario.getPersona() != null) {
                            Log.d(TAG, "Persona ID cargada: " + usuario.getPersona().getId());
                        }
                        if (usuario.getInvernadero() != null) {
                            Log.d(TAG, "Invernadero ID cargado: " + usuario.getInvernadero().getId());
                        }

                        loadUserDataToUI();
                    } else {
                        Toast.makeText(PerfilActivity.this, "No se encontraron usuarios", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(PerfilActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UsuarioData>>> call, Throwable t) {
                //showProgress(false);
                Toast.makeText(PerfilActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadUserDataToUI() {
        // Mostrar datos básicos
        tvUsername.setText(usuario.getNombreUsuario());
        tvRole.setText(usuario.getRol() != null ? usuario.getRol() : "Usuario");

        // Datos personales
        etUsername.setText(usuario.getNombreUsuario());

        if (usuario.getPersona() != null) {
            etFirstName.setText(usuario.getPersona().getNombre());
            // Combinar apellidos para mostrarlos en un solo campo
            String apellidos = usuario.getPersona().getAPaterno() +
                    (usuario.getPersona().getAMaterno() != null && !usuario.getPersona().getAMaterno().isEmpty() ?
                            " " + usuario.getPersona().getAMaterno() : "");
            etLastName.setText(apellidos.trim());
        } else {
            Log.w(TAG, "Objeto Persona es null para usuario ID: " + usuario.getId());
        }

        // Datos del invernadero
        if (usuario.getInvernadero() != null) {
            etGreenhouseName.setText(usuario.getInvernadero().getNombre());
            etSerialNumber.setText(usuario.getInvernadero().getNumSerie());
            etModel.setText(usuario.getInvernadero().getModelo());
        } else {
            Log.w(TAG, "Objeto Invernadero es null para usuario ID: " + usuario.getId());
        }
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(v -> {
            if (!validateFields()) {
                return;
            }
            updateUser();
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            finishAffinity();
        });
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (etUsername.getText().toString().trim().isEmpty()) {
            tilUsername.setError(getString(R.string.username_required));
            isValid = false;
        }

        if (etFirstName.getText().toString().trim().isEmpty()) {
            tilFirstName.setError(getString(R.string.first_name_required));
            isValid = false;
        }

        if (etLastName.getText().toString().trim().isEmpty()) {
            tilLastName.setError(getString(R.string.last_name_required));
            isValid = false;
        }

        if (etGreenhouseName.getText().toString().trim().isEmpty()) {
            tilGreenhouseName.setError(getString(R.string.greenhouse_name_required));
            isValid = false;
        }

        if (etSerialNumber.getText().toString().trim().isEmpty()) {
            tilSerialNumber.setError(getString(R.string.serial_number_required));
            isValid = false;
        }

        if (etModel.getText().toString().trim().isEmpty()) {
            tilModel.setError(getString(R.string.model_required));
            isValid = false;
        }

        return isValid;
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
            layout.setError(getString(R.string.required_field));
        } else {
            layout.setError(null);
        }
    }

    private void setupErrorCleaner(TextInputEditText editText, TextInputLayout layout) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layout.setError(null);
            } else {
                validateField(editText, layout, getString(R.string.required_field));
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

    private void updateUser() {
        // Verificar que tenemos los datos del usuario
        if (usuario == null) {
            Toast.makeText(this, "Datos de usuario no disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar datos del usuario con los valores de los campos
        usuario.setNombreUsuario(etUsername.getText().toString().trim());

        // Asegurar que el objeto persona existe (ya debería existir con los IDs correctos)
        if (usuario.getPersona() == null) {
            usuario.setPersona(new Persona());
            // Asegúrate de que tenga ID si es un nuevo objeto
            usuario.getPersona().setId(1); // Usa el ID apropiado según tu lógica de negocio
            Toast.makeText(this, "Creando nuevos datos de persona", Toast.LENGTH_SHORT).show();
        }

        // Procesar apellidos (separar paterno y materno)
        String[] apellidos = etLastName.getText().toString().trim().split(" ", 2);
        usuario.getPersona().setNombre(etFirstName.getText().toString().trim());
        usuario.getPersona().setAPaterno(apellidos.length > 0 ? apellidos[0] : "");
        usuario.getPersona().setAMaterno(apellidos.length > 1 ? apellidos[1] : "");

        // Asegurar que el objeto invernadero existe (ya debería existir con los IDs correctos)
        if (usuario.getInvernadero() == null) {
            usuario.setInvernadero(new Invernadero());
            // Asegúrate de que tenga ID si es un nuevo objeto
            usuario.getInvernadero().setId(2); // Usa el ID apropiado según tu lógica de negocio
            Toast.makeText(this, "Creando nuevos datos de invernadero", Toast.LENGTH_SHORT).show();
        }

        // Actualizar datos del invernadero
        usuario.getInvernadero().setNombre(etGreenhouseName.getText().toString().trim());
        usuario.getInvernadero().setNumSerie(etSerialNumber.getText().toString().trim());
        usuario.getInvernadero().setModelo(etModel.getText().toString().trim());

        // Log de verificación antes de enviar
        Log.d(TAG, "Usuario ID a enviar: " + usuario.getId());
        Log.d(TAG, "Persona ID a enviar: " + usuario.getPersona().getId());
        Log.d(TAG, "Invernadero ID a enviar: " + usuario.getInvernadero().getId());

        // Mostrar el JSON que se enviará para verificación
        String jsonEnviado = gson.toJson(usuario);
        Log.d("JSON_ENVIADO", "Datos a enviar:\n" + jsonEnviado);

        // Mostrar Toast de inicio de actualización
        try {
            Toast.makeText(this, "Actualizando perfil...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar Toast: " + e.getMessage());
        }

        // Llamada a la API para actualizar
        //showProgress(true);
        Call<ApiResponse<UsuarioData>> call = apiService.actualizarUsuario(usuario);
        call.enqueue(new Callback<ApiResponse<UsuarioData>>() {
            @Override
            public void onResponse(Call<ApiResponse<UsuarioData>> call, Response<ApiResponse<UsuarioData>> response) {
                //showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UsuarioData> apiResponse = response.body();
                    Log.d("RESPUESTA_API", "Respuesta del servidor:\n" + gson.toJson(apiResponse));

                    if (apiResponse.getStatus() == 200) {
                        // Log detallado para verificar datos
                        Log.d("DATOS_ACTUALIZADOS", "Usuario ID: " + apiResponse.getData().getId());
                        if (apiResponse.getData().getPersona() != null) {
                            Log.d("DATOS_ACTUALIZADOS", "Nombre: " + apiResponse.getData().getPersona().getNombre());
                            Log.d("DATOS_ACTUALIZADOS", "Apellido: " + apiResponse.getData().getPersona().getAPaterno());
                        }
                        if (apiResponse.getData().getInvernadero() != null) {
                            Log.d("DATOS_ACTUALIZADOS", "Invernadero: " + apiResponse.getData().getInvernadero().getNombre());
                        }

                        // Actualizar datos locales con la respuesta
                        usuario = apiResponse.getData();

                        // Forzar actualización de UI en hilo principal
                        runOnUiThread(() -> {
                            loadUserDataToUI();
                            try {
                                Toast.makeText(PerfilActivity.this, "¡Perfil actualizado correctamente!", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Error al mostrar Toast de éxito: " + e.getMessage());
                            }
                        });

                        // Devolver el usuario actualizado
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("usuarioActualizado", usuario);
                        setResult(RESULT_OK, resultIntent);

                        // Si estamos en modo edición, finalizar la actividad después de actualizar
                        if (isEditing) {
                            finish();
                        }
                    } else {
                        runOnUiThread(() -> {
                            try {
                                Toast.makeText(PerfilActivity.this, "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Error al mostrar Toast de error: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        Log.e("ERROR_SERVIDOR", "Código: " + response.code() + "\nError: " + errorBody);

                        runOnUiThread(() -> {
                            try {
                                Toast.makeText(PerfilActivity.this, "Error del servidor", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Error al mostrar Toast de error del servidor: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        Log.e("ERROR_PROCESO", "Error al procesar respuesta", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UsuarioData>> call, Throwable t) {
                //showProgress(false);
                Log.e("ERROR_CONEXION", "Fallo en la conexión", t);

                runOnUiThread(() -> {
                    try {
                        Toast.makeText(PerfilActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mostrar Toast de error de conexión: " + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Ajusta el scroll para que el campo enfocado sea visible al aparecer el teclado
     * y gestiona el espacio adecuadamente.
     * TODO: Falta ajuste con la tecla next para que vaya bajando dependiendo de la altura del teclado.
     */
    private void setupScrollView() {
        // Ajusta el padding para dar espacio al teclado
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            scrollView.setPadding(0, 0, 0, imeHeight);

            // Forzar scroll al elemento enfocado después de aplicar el padding
            View focusedView = getCurrentFocus();
            if (focusedView != null) {
                // Añadir una pequeña demora para que se aplique el padding primero
                focusedView.post(() -> {
                    // Calcular la posición del campo enfocado dentro del ScrollView
                    Rect scrollBounds = new Rect();
                    scrollView.getDrawingRect(scrollBounds);

                    // Obtener coordenadas relativas al ScrollView
                    int scrollViewHeight = scrollView.getHeight();
                    int[] location = new int[2];
                    focusedView.getLocationInWindow(location);
                    int[] scrollLocation = new int[2];
                    scrollView.getLocationInWindow(scrollLocation);

                    // Calcular la posición Y dentro del ScrollView
                    int relativeY = location[1] - scrollLocation[1];

                    // Si el elemento está fuera de vista o muy cerca del borde inferior
                    if (relativeY + focusedView.getHeight() > scrollBounds.bottom - 50) {
                        int scrollTo = relativeY - (scrollViewHeight / 2);
                        if (scrollTo > 0) {
                            scrollView.smoothScrollTo(0, scrollTo);
                        }
                    }
                });
            }

            return insets;
        });

        // Complementamos con el listener de layout para mayor robustez
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visibleArea = new Rect();
            scrollView.getWindowVisibleDisplayFrame(visibleArea);
            int screenHeight = scrollView.getRootView().getHeight();

            // Si la diferencia es mayor al 15% de la altura, el teclado probablemente está visible
            if (screenHeight - visibleArea.bottom > screenHeight * 0.15) {
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    // Añadir un pequeño offset para que el campo quede bien visible
                    int offset = 150; // Puedes ajustar este valor

                    int[] location = new int[2];
                    focusedView.getLocationOnScreen(location);

                    // Calcular si el campo está parcialmente oculto por el teclado
                    int fieldBottom = location[1] + focusedView.getHeight();
                    int visibleBottom = visibleArea.bottom;

                    if (fieldBottom > visibleBottom - offset) {
                        int scrollY = (fieldBottom - visibleBottom) + offset;
                        scrollView.smoothScrollBy(0, scrollY);
                    }
                }
            }
        });
    }

//    private void showProgress(boolean show) {
//        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//        btnUpdate.setEnabled(!show);
//        btnLogout.setEnabled(!show);
//    }
}