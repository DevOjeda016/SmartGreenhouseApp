package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.SensorApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.MeasureResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorDTO;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MonitoringActivity extends AppCompatActivity {

    private TextView txtTemperatura, txtHumedad, txtContaminacion, txtUltimaModificacion;
    private Button btnActualizarDatos;
    private MaterialButton iconButton;

    private float valorTemperatura = 0, valorHumedad = 0, valorContaminacion = 0;
    private TextView
            txtEstadoHumidificador,
            txtEstadoVentilacion,
            txtEstadoAlarmaTemp,
            txtEstadoAlarmaHum,
            txtEstadoAlarmaGas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitorizacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de los elementos de la UI
        txtTemperatura = findViewById(R.id.txtTemperatura);
        txtHumedad = findViewById(R.id.txtHumedad);
        txtContaminacion = findViewById(R.id.txtContaminacion);
        txtUltimaModificacion = findViewById(R.id.txtUltimaModificacion);
        btnActualizarDatos = findViewById(R.id.btnActualizarDatos);
        txtEstadoHumidificador = findViewById(R.id.txtEstadoHumidificador);
        txtEstadoVentilacion = findViewById(R.id.txtEstadoVentilacion);
        txtEstadoAlarmaTemp = findViewById(R.id.txtEstadoAlarmaTemp);
        txtEstadoAlarmaHum = findViewById(R.id.txtEstadoAlarmaHumedad);
        txtEstadoAlarmaGas = findViewById(R.id.txtEstadoAlarmaGas);
        iconButton = findViewById(R.id.iconButton);
        iconButton.setOnClickListener(v -> finish());

        btnActualizarDatos.setOnClickListener(v -> obtenerValoresMediciones());
        obtenerValoresMediciones();
    }


    private void obtenerValoresMediciones() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SensorApiService apiService = retrofit.create(SensorApiService.class);
        // Realizar las tres llamadas a la API (una por cada tipo de sensor)
        Call<MeasureResponse> callTemperatura = apiService.getMeasureData("temperatura");
        Call<MeasureResponse> callHumedad = apiService.getMeasureData("humedad");
        Call<MeasureResponse> callGas = apiService.getMeasureData("gas");

        // Contador para saber cuándo todas las llamadas han terminado
        final int[] contadorLlamadas = {0};
        final int TOTAL_LLAMADAS = 3;

        // Callback genérico para manejar las respuestas
        Callback<MeasureResponse> callback = new Callback<MeasureResponse>() {
            @Override
            public void onResponse(Call<MeasureResponse> call, Response<MeasureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeasureResponse measureResponse = response.body();
                    String endpoint = call.request().url().toString();

                    // Determinar qué tipo de datos estamos recibiendo
                    if (endpoint.contains("temperatura") && !measureResponse.getData().isEmpty()) {
                        valorTemperatura = measureResponse.getData().get(measureResponse.getData().size() - 1).getValor();
                    } else if (endpoint.contains("humedad") && !measureResponse.getData().isEmpty()) {
                        valorHumedad = measureResponse.getData().get(measureResponse.getData().size() - 1).getValor();
                    } else if (endpoint.contains("gas") && !measureResponse.getData().isEmpty()) {
                        valorContaminacion = measureResponse.getData().get(measureResponse.getData().size() - 1).getValor();
                    }
                }

                contadorLlamadas[0]++;
                if (contadorLlamadas[0] == TOTAL_LLAMADAS) {
                    //Obtener los limites despues de haber obtenido los valores
                    obtenerDatosSensores();
                }
            }

            @Override
            public void onFailure(Call<MeasureResponse> call, Throwable t) {
                Toast.makeText(MonitoringActivity.this, "Error al obtener datos de " + call.request().url(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                contadorLlamadas[0]++;
                if (contadorLlamadas[0] == TOTAL_LLAMADAS) {
                    //Obtener los limites despues de haber obtenido los valores
                    obtenerDatosSensores();
                }
            }
        };

        // Ejecutar las tres llamadas
        callTemperatura.enqueue(callback);
        callHumedad.enqueue(callback);
        callGas.enqueue(callback);
    }

    private void obtenerDatosSensores() {
        SensorApiService apiService = SmartGreenhouseClient.getClient().create(SensorApiService.class);
        Call<SensorResponse> call = apiService.getSensorData();

        call.enqueue(new Callback<SensorResponse>() {
            @Override
            public void onResponse(Call<SensorResponse> call, Response<SensorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actualizarInterfazSensores(response.body().getData());
                    actualizarFechaModificacion();
                } else {
                    Toast.makeText(MonitoringActivity.this,
                            "Error al obtener datos: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SensorResponse> call, Throwable t) {
                Toast.makeText(MonitoringActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarInterfazSensores(List<SensorDTO> sensores) {
        if (sensores == null || sensores.isEmpty()) {
            Toast.makeText(this, "No hay datos de sensores", Toast.LENGTH_SHORT).show();
            return;
        }

        // Valores actuales (deberías obtener estos de otra API o sensor real)
        // Estos son valores de ejemplo
        double temperaturaActual = valorTemperatura;
        double humedadActual = valorHumedad;
        double gasActual = valorContaminacion;

        for (SensorDTO sensor : sensores) {
            switch (sensor.getTipoSensor()) {
                case "TEMPERATURA":
                    String tempText = String.format(Locale.getDefault(),
                            "%.1f°C (Límites: %.1f°C - %.1f°C)",
                            temperaturaActual,
                            sensor.getLimiteInferior(),
                            sensor.getLimiteSuperior());
                    txtTemperatura.setText(tempText);
                    if (temperaturaActual < sensor.getLimiteInferior() || temperaturaActual > sensor.getLimiteSuperior()) {
                        txtEstadoVentilacion.setTextColor(ContextCompat.getColor(this, R.color.md_theme_tertiaryContainer_mediumContrast));
                        txtEstadoAlarmaTemp.setTextColor(ContextCompat.getColor(this, R.color.md_theme_tertiaryContainer_mediumContrast));
                        txtEstadoAlarmaTemp.setText("ON");
                        txtEstadoVentilacion.setText("ON");
                    } else {
                        txtEstadoAlarmaTemp.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
                        txtEstadoVentilacion.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
                        txtEstadoAlarmaTemp.setText("OFF");
                        txtEstadoVentilacion.setText("OFF");
                    }
                    break;

                case "HUMEDAD":
                    String humText = String.format(Locale.getDefault(),
                            "%.1f%% (Límites: %.1f%% - %.1f%%)",
                            humedadActual,
                            sensor.getLimiteInferior(),
                            sensor.getLimiteSuperior());
                    txtHumedad.setText(humText);
                    if (humedadActual < sensor.getLimiteInferior() || humedadActual > sensor.getLimiteSuperior()) {
                        txtEstadoHumidificador.setTextColor(ContextCompat.getColor(this, R.color.md_theme_tertiaryContainer_mediumContrast));
                        txtEstadoAlarmaHum.setTextColor(ContextCompat.getColor(this, R.color.md_theme_tertiaryContainer_mediumContrast));
                        txtEstadoAlarmaHum.setText("ON");
                        txtEstadoHumidificador.setText("ON");
                    } else {
                        txtEstadoAlarmaHum.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
                        txtEstadoHumidificador.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
                        txtEstadoAlarmaHum.setText("OFF");
                        txtEstadoHumidificador.setText("OFF");
                    }
                    break;

                case "GAS":
                    String gasText = String.format(Locale.getDefault(),
                            "%.1f%% (Límites: %.1f%% - %.1f%%)",
                            gasActual,
                            sensor.getLimiteInferior(),
                            sensor.getLimiteSuperior());
                    txtContaminacion.setText(gasText);
                    if (gasActual < sensor.getLimiteInferior() || gasActual > sensor.getLimiteSuperior()) {
                        txtEstadoAlarmaGas.setTextColor(ContextCompat.getColor(this, R.color.md_theme_tertiaryContainer_mediumContrast));
                        txtEstadoAlarmaGas.setText("ON");
                    } else {
                        txtEstadoAlarmaGas.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
                        txtEstadoAlarmaGas.setText("OFF");
                    }
                    break;
            }
        }
    }

    private void setTextColorBasedOnLimits(TextView textView, double actual,
                                           double limiteInferior, double limiteSuperior) {
        if (actual < limiteInferior || actual > limiteSuperior) {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
    }

    private void actualizarFechaModificacion() {
        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());
        txtUltimaModificacion.setText(getString(R.string.strUltimaActualizacion) + fecha);
    }
}