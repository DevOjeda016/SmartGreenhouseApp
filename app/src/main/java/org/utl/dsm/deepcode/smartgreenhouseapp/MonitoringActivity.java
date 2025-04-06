package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.SensorApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorDTO;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitoringActivity extends AppCompatActivity {

    private TextView txtTemperatura, txtHumedad, txtContaminacion, txtUltimaModificacion;
    private Button btnActualizarDatos;
    private MaterialButton iconButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizacion);

        // Inicialización de los elementos de la UI
        txtTemperatura = findViewById(R.id.txtTemperatura);
        txtHumedad = findViewById(R.id.txtHumedad);
        txtContaminacion = findViewById(R.id.txtContaminacion);
        txtUltimaModificacion = findViewById(R.id.txtUltimaModificacion);
        btnActualizarDatos = findViewById(R.id.btnActualizarDatos);
        iconButton = findViewById(R.id.iconButton);
        iconButton.setOnClickListener(v -> finish());

        btnActualizarDatos.setOnClickListener(v -> obtenerDatosSensores());
        obtenerDatosSensores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        obtenerDatosSensores();
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
        double temperaturaActual = 10.5;
        double humedadActual = 20.0;
        double gasActual = 30.0;

        for (SensorDTO sensor : sensores) {
            switch (sensor.getTipoSensor()) {
                case "TEMPERATURA":
                    String tempText = String.format(Locale.getDefault(),
                            "%.1f°C (Límites: %.1f°C - %.1f°C)",
                            temperaturaActual,
                            sensor.getLimiteInferior(),
                            sensor.getLimiteSuperior());
                    txtTemperatura.setText(tempText);
                    break;

                case "HUMEDAD":
                    String humText = String.format(Locale.getDefault(),
                            "%.1f%% (Límites: %.1f%% - %.1f%%)",
                            humedadActual,
                            sensor.getLimiteInferior(),
                            sensor.getLimiteSuperior());
                    txtHumedad.setText(humText);
                    break;

                case "GAS":
                    String gasText = String.format(Locale.getDefault(),
                            "%.1f%% (Límites: %.1f%% - %.1f%%)",
                            gasActual,
                            sensor.getLimiteInferior(),
                            sensor.getLimiteSuperior());
                    txtContaminacion.setText(gasText);
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
        txtUltimaModificacion.setText("Última actualización: " + fecha);
    }
}