package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.SensorApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorDTO;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class controladores extends AppCompatActivity {
    EditText editTextNewLowerLimitTemperature;
    EditText editTextNewUpperLimitTemperature;
    EditText editTextNewLowerLimitHumidity;
    EditText editTextNewUpperLimitHumidity;
    Button btnUpdateTemperatureLimits;
    Button btnUpdateHumidityLimits;

    TextView txtCurrentLowerLimitTemperature;
    TextView txtCurrentUpperLimitTemperature;
    TextView txtCurrentLowerLimitHumidity;
    TextView txtCurrentUpperLimitHumidity;

    TextView txtCurrentLowerLimitGas;
    TextView txtCurrentUpperLimitGas;

    EditText editTextNewLowerLimitGas;
    EditText editTextNewUpperLimitGas;
    Button btnUpdateGasLimits;
    SensorResponse sensorResponse;
    ScrollView scrollView;
    MaterialButton iconButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controladores);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeComponents();
        setupScrollView();
        getLimits();
    }

    private void initializeComponents() {
        editTextNewLowerLimitTemperature = findViewById(R.id.editTextNewLowerLimitTemperature);
        editTextNewUpperLimitTemperature = findViewById(R.id.editTextNewUpperLimitTemperature);
        editTextNewLowerLimitHumidity = findViewById(R.id.editTextNewLowerLimitHumidity);
        editTextNewUpperLimitHumidity = findViewById(R.id.editTextNewUpperLimitHumidity);
        btnUpdateTemperatureLimits = findViewById(R.id.btnUpdateTemperatureLimits);
        btnUpdateHumidityLimits = findViewById(R.id.btnUpdateHumidityLimits);

        txtCurrentLowerLimitTemperature = findViewById(R.id.txtCurrentLowerLimitTemperature);
        txtCurrentUpperLimitTemperature = findViewById(R.id.txtCurrentUpperLimitTemperature);
        txtCurrentLowerLimitHumidity = findViewById(R.id.txtCurrentLowerLimitHumidity);
        txtCurrentUpperLimitHumidity = findViewById(R.id.txtCurrentUpperLimitHumidity);

        txtCurrentLowerLimitGas = findViewById(R.id.txtCurrentLowerLimitGas);
        txtCurrentUpperLimitGas = findViewById(R.id.txtCurrentUpperLimitGas);
        editTextNewLowerLimitGas = findViewById(R.id.editTextNewLowerLimitGas);
        editTextNewUpperLimitGas = findViewById(R.id.editTextNewUpperLimitGas);
        btnUpdateGasLimits = findViewById(R.id.btnUpdateGasLimits);
        iconButton = findViewById(R.id.iconButton);

        btnUpdateTemperatureLimits.setOnClickListener(v -> sendLimitsTemperature());
        btnUpdateHumidityLimits.setOnClickListener(v -> sendLimitsHumidity());
        btnUpdateGasLimits.setOnClickListener(v -> sendLimitsGas());
        iconButton.setOnClickListener(v -> finish());
        scrollView = findViewById(R.id.scrollView);
    }

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

    private void getLimits() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SensorApiService apiService = retrofit.create(SensorApiService.class);
        Call<SensorResponse> call = apiService.getSensorData();

        call.enqueue(new Callback<SensorResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<SensorResponse> call, @NonNull Response<SensorResponse> response) {
                if (response.isSuccessful()) {
                    sensorResponse = response.body();
                    if (sensorResponse != null && !sensorResponse.getData().isEmpty()) {
                        updateUIWithCurrentLimits();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SensorResponse> call, @NonNull Throwable t) {
                showToast("Error al obtener los límites actuales");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateUIWithCurrentLimits() {
        // Temperatura
        txtCurrentLowerLimitTemperature.setText("Límite inferior actual: "
                + sensorResponse.getData().get(0).getLimiteInferior() + " °C");
        txtCurrentUpperLimitTemperature.setText("Límite superior actual: "
                + sensorResponse.getData().get(0).getLimiteSuperior() + " °C");

        // Humedad
        txtCurrentLowerLimitHumidity.setText("Límite inferior actual: "
                + sensorResponse.getData().get(1).getLimiteInferior() + " %");
        txtCurrentUpperLimitHumidity.setText("Límite superior actual: "
                + sensorResponse.getData().get(1).getLimiteSuperior() + " %");

        // Gas
        txtCurrentLowerLimitGas.setText("Límite inferior actual: "
                + sensorResponse.getData().get(2).getLimiteInferior() + " ppm");
        txtCurrentUpperLimitGas.setText("Límite superior actual: "
                + sensorResponse.getData().get(2).getLimiteSuperior() + " ppm");
    }

    private void sendLimitsTemperature() {
        try {
            float currentLower = sensorResponse.getData().get(0).getLimiteInferior();
            float currentUpper = sensorResponse.getData().get(0).getLimiteSuperior();

            float newLower = getValueFromEditText(editTextNewLowerLimitTemperature, currentLower);
            float newUpper = getValueFromEditText(editTextNewUpperLimitTemperature, currentUpper);

            if (!validateLimits(newLower, newUpper, -10, 50, "temperatura")) return;

            updateLimits("TEMPERATURA", newLower, newUpper);
        } catch (NumberFormatException e) {
            showToast("Valores numéricos inválidos para temperatura");
        }
    }

    private void sendLimitsHumidity() {
        try {
            float currentLower = sensorResponse.getData().get(1).getLimiteInferior();
            float currentUpper = sensorResponse.getData().get(1).getLimiteSuperior();

            float newLower = getValueFromEditText(editTextNewLowerLimitHumidity, currentLower);
            float newUpper = getValueFromEditText(editTextNewUpperLimitHumidity, currentUpper);

            if (!validateLimits(newLower, newUpper, 0, 100, "humedad")) return;

            updateLimits("HUMEDAD", newLower, newUpper);
        } catch (NumberFormatException e) {
            showToast("Valores numéricos inválidos para humedad");
        }
    }

    private void sendLimitsGas() {
        try {
            float currentLower = sensorResponse.getData().get(2).getLimiteInferior();
            float currentUpper = sensorResponse.getData().get(2).getLimiteSuperior();

            float newLower = getValueFromEditText(editTextNewLowerLimitGas, currentLower);
            float newUpper = getValueFromEditText(editTextNewUpperLimitGas, currentUpper);

            if (!validateLimits(newLower, newUpper, 0, 5000, "gas")) return;

            updateLimits("GAS", newLower, newUpper);
        } catch (NumberFormatException e) {
            showToast("Valores numéricos inválidos para gas");
        }
    }

    private float getValueFromEditText(EditText editText, float defaultValue) {
        return editText.getText().toString().isEmpty()
                ? defaultValue
                : Float.parseFloat(editText.getText().toString());
    }

    @SuppressLint("DefaultLocale")
    private boolean validateLimits(float lower, float upper, float min, float max, String sensorType) {
        if (lower >= upper) {
            showToast("Error: Límite inferior debe ser menor al superior");
            return false;
        }

        if (lower < min || lower > max) {
            showToast(String.format("Límite inferior de %s debe estar entre %.1f y %.1f",
                    sensorType, min, max));
            return false;
        }

        if (upper < min || upper > max) {
            showToast(String.format("Límite superior de %s debe estar entre %.1f y %.1f",
                    sensorType, min, max));
            return false;
        }

        return true;
    }

    private void updateLimits(String tipoSensor, float newLowerLimit, float newUpperLimit) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SensorApiService service = retrofit.create(SensorApiService.class);
        SensorDTO sensorDTO = new SensorDTO();
        sensorDTO.setTipoSensor(tipoSensor);
        sensorDTO.setLimiteInferior(newLowerLimit);
        sensorDTO.setLimiteSuperior(newUpperLimit);

        Call<SensorDTO> call = service.createSensor(sensorDTO);
        call.enqueue(new Callback<SensorDTO>() {
            @Override
            public void onResponse(@NonNull Call<SensorDTO> call, @NonNull Response<SensorDTO> response) {
                if (response.isSuccessful()) {
                    showToast("Límites actualizados correctamente");
                    getLimits();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SensorDTO> call, @NonNull Throwable t) {
                showToast("Error al actualizar los límites");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}