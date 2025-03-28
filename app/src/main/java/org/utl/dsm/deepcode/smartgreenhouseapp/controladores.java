package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        getLimits();
        btnUpdateTemperatureLimits.setOnClickListener(v -> sendLimitsTemperature());
        btnUpdateHumidityLimits.setOnClickListener(v -> sendLimitsHumidity());
        btnUpdateGasLimits.setOnClickListener(v -> sendLimitsGas());
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
            public void onResponse(Call<SensorResponse> call, Response<SensorResponse> response) {
                if (response.isSuccessful()) {
                    System.out.println(response.body());
                    sensorResponse = response.body();
                    assert sensorResponse != null;
                    txtCurrentLowerLimitTemperature.setText("Limite inferior actual: " + sensorResponse.getData().get(0).getLimiteInferior() + " °C");
                    txtCurrentUpperLimitTemperature.setText("Limite superior actual: " + sensorResponse.getData().get(0).getLimiteSuperior() + " °C");
                    txtCurrentLowerLimitHumidity.setText("Limite inferior actual: " + sensorResponse.getData().get(1).getLimiteInferior() + " %");
                    txtCurrentUpperLimitHumidity.setText("Limite superior actual: " + sensorResponse.getData().get(1).getLimiteSuperior() + " %");
                    txtCurrentLowerLimitGas.setText("Limite inferior actual: " + sensorResponse.getData().get(2).getLimiteInferior() + " ppm");
                    txtCurrentUpperLimitGas.setText("Limite superior actual: " + sensorResponse.getData().get(2).getLimiteSuperior() + " ppm");


                }
            }

            @Override
            public void onFailure(Call<SensorResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendLimitsTemperature() {
        float newLowerLimitTemperature;
        float newUpperLimitTemperature;

        if (editTextNewLowerLimitTemperature.getText().toString().isEmpty()) {
            newLowerLimitTemperature = sensorResponse.getData().get(0).getLimiteInferior();
        } else {
            newLowerLimitTemperature = Float.parseFloat(editTextNewLowerLimitTemperature.getText().toString());
        }
        if (editTextNewUpperLimitTemperature.getText().toString().isEmpty()) {
            newUpperLimitTemperature = sensorResponse.getData().get(0).getLimiteSuperior();
        } else {
            newUpperLimitTemperature = Float.parseFloat(editTextNewUpperLimitTemperature.getText().toString());
        }
        updateLimits("TEMPERATURA", newLowerLimitTemperature, newUpperLimitTemperature);
    }

    private void sendLimitsHumidity() {
        float newLowerLimitHumidity;
        float newUpperLimitHumidity;
        if (editTextNewLowerLimitHumidity.getText().toString().isEmpty()) {
            newLowerLimitHumidity = sensorResponse.getData().get(1).getLimiteInferior();
        } else {
            newLowerLimitHumidity = Float.parseFloat(editTextNewLowerLimitHumidity.getText().toString());
        }
        if (editTextNewUpperLimitHumidity.getText().toString().isEmpty()) {
            newUpperLimitHumidity = sensorResponse.getData().get(1).getLimiteSuperior();
        } else {
            newUpperLimitHumidity = Float.parseFloat(editTextNewUpperLimitHumidity.getText().toString());
        }
        updateLimits("HUMEDAD", newLowerLimitHumidity, newUpperLimitHumidity);
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
            public void onResponse(Call<SensorDTO> call, Response<SensorDTO> response) {
                if (response.isSuccessful()) {
                    getLimits();
                }
            }

            @Override
            public void onFailure(Call<SensorDTO> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendLimitsGas() {
        float newLowerLimitGas;
        float newUpperLimitGas;
        if (editTextNewLowerLimitGas.getText().toString().isEmpty()) {
            newLowerLimitGas = sensorResponse.getData().get(2).getLimiteInferior();
        } else {
            newLowerLimitGas = Float.parseFloat(editTextNewLowerLimitGas.getText().toString());
        }
        if (editTextNewUpperLimitGas.getText().toString().isEmpty()) {
            newUpperLimitGas = sensorResponse.getData().get(2).getLimiteSuperior();
        } else {
            newUpperLimitGas = Float.parseFloat(editTextNewUpperLimitGas.getText().toString());
        }
        updateLimits("GAS", newLowerLimitGas, newUpperLimitGas);
    }
}