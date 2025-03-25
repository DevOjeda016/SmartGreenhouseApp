package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.ApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Sensor;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitoringActivity extends AppCompatActivity {

    // Definición de variables para los elementos de la UI
    private TextView txtTemperatura;
    private TextView txtHumedad;
    private TextView txtContaminacion;
    private Button btnActualizarDatos;

    // Variables para almacenar los datos actuales
    private List<Sensor> sensores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizacion);

        // Inicialización de los elementos de la UI
        txtTemperatura = findViewById(R.id.txtTemperatura);
        txtHumedad = findViewById(R.id.txtHumedad);
        txtContaminacion = findViewById(R.id.txtContaminacion);
        btnActualizarDatos = findViewById(R.id.btnActualizarDatos);

        // Configurar el botón de actualización
        btnActualizarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerDatosSensores();
            }
        });

        // Cargar datos iniciales
        obtenerDatosSensores();
    }

    /**
     * Método para obtener los datos de los sensores desde la API
     */
    private void obtenerDatosSensores() {
        // Crear instancia de ApiService usando SmartGreenhouseClient
        ApiService apiService = SmartGreenhouseClient.getClient().create(ApiService.class);

        // Realizar la llamada a la API
        Call<ApiResponse<List<Sensor>>> call = apiService.getSensores();

        call.enqueue(new Callback<ApiResponse<List<Sensor>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Sensor>>> call, Response<ApiResponse<List<Sensor>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Sensor>> apiResponse = response.body();

                    if (apiResponse.getStatus() == 200) {
                        sensores = apiResponse.getData();
                        actualizarInterfazSensores(sensores);
                    } else {
                        // Manejar respuesta de error del servidor
                        Toast.makeText(MonitoringActivity.this,
                                "Error: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Manejar error de respuesta
                    Toast.makeText(MonitoringActivity.this,
                            "Error en la respuesta del servidor",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Sensor>>> call, Throwable t) {
                // Manejar error de conexión
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(MonitoringActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para actualizar la interfaz con los datos de los sensores
     */
    private void actualizarInterfazSensores(List<Sensor> sensores) {
        if (sensores == null || sensores.isEmpty()) {
            return;
        }

        // Valores simulados para la demostración (deberían venir de otra API o cálculos)
        int temperaturaActual = 24;
        int temperaturaDiferencia = 2;
        int humedadActual = 65;
        int contaminacionActual = 95;

        // Recorrer la lista de sensores y actualizar la interfaz según el tipo
        for (Sensor sensor : sensores) {
            switch (sensor.getTipoSensor()) {
                case "TEMPERATURA":
                    // Mostrar datos de temperatura con límites
                    String temperaturaTexto = temperaturaActual + "°C (↑" + temperaturaDiferencia + "°C desde ayer)";
                    txtTemperatura.setText(temperaturaTexto);

                    // Opcionalmente, puedes cambiar el color del texto según si está dentro de los límites
                    if (temperaturaActual < sensor.getLimiteInferior() ||
                            temperaturaActual > sensor.getLimiteSuperior()) {
                        txtTemperatura.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        txtTemperatura.setTextColor(getResources().getColor(android.R.color.black));
                    }
                    break;

                case "HUMEDAD":
                    // Mostrar datos de humedad con límites
                    String humedadTexto = humedadActual + "%";
                    txtHumedad.setText(humedadTexto);

                    if (humedadActual < sensor.getLimiteInferior() ||
                            humedadActual > sensor.getLimiteSuperior()) {
                        txtHumedad.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        txtHumedad.setTextColor(getResources().getColor(android.R.color.black));
                    }
                    break;

                case "GAS":
                    // Para el caso de GAS, lo mostramos como "Contaminación" en la UI
                    String contaminacionTexto = contaminacionActual + "% (Alta)";
                    txtContaminacion.setText(contaminacionTexto);

                    if (contaminacionActual < sensor.getLimiteInferior() ||
                            contaminacionActual > sensor.getLimiteSuperior()) {
                        txtContaminacion.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        txtContaminacion.setTextColor(getResources().getColor(android.R.color.black));
                    }
                    break;
            }
        }
    }
}