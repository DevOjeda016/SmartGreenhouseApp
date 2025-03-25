package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.animation.Easing;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.SensorApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.MeasureResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.Measure;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class graficas extends AppCompatActivity {
    private LineChart chartTemperatura, chartHumedad, chartGas;
    private TextView txtUltimaActualizacion, txtUltimaTemperatura, txtUltimaHumedad, txtUltimaGas;
    private Button btnActualizar;
    private String ultimaFechaActualizacion = "-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_graficas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Asignar gráficos desde el XML
        chartTemperatura = findViewById(R.id.chartTemperatura);
        chartHumedad = findViewById(R.id.chartHumedad);
        chartGas = findViewById(R.id.chartGas);

        // Asignar TextView y Button
        txtUltimaActualizacion = findViewById(R.id.txtUltimaActualizacion);
        txtUltimaTemperatura = findViewById(R.id.txtUltimaTemperatura);
        txtUltimaHumedad = findViewById(R.id.txtUltimaHumedad);
        txtUltimaGas = findViewById(R.id.txtUltimaGas);
        btnActualizar = findViewById(R.id.btnActualizar);

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.100.7:8080/SmartGreenhouse/api/") // URL base de la API
                .addConverterFactory(GsonConverterFactory.create()) // Conversor de JSON a objetos Java
                .build();

        SensorApiService apiService = retrofit.create(SensorApiService.class);

        // Configurar el botón de actualización
        btnActualizar.setOnClickListener(v -> actualizarDatos(apiService));

        // Realizar la primera actualización al iniciar la actividad
        actualizarDatos(apiService);
    }

    private void actualizarDatos(SensorApiService apiService) {
        // Realizar la llamada a la API
        Call<MeasureResponse> call = apiService.getMeasureData();
        call.enqueue(new Callback<MeasureResponse>() {
            @Override
            public void onResponse(Call<MeasureResponse> call, Response<MeasureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeasureResponse measureResponse = response.body();
                    parseDataAndShowCharts(measureResponse.getData());

                    // Actualizar la fecha de la última actualización
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getDefault()); // Usar la zona horaria del dispositivo
                    ultimaFechaActualizacion = sdf.format(new Date());
                    txtUltimaActualizacion.setText("Última actualización: " + ultimaFechaActualizacion);
                }
            }

            @Override
            public void onFailure(Call<MeasureResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void parseDataAndShowCharts(List<Measure> data) {
        // Variables para almacenar las últimas mediciones
        float ultimaTemperatura = 0;
        float ultimaHumedad = 0;
        float ultimaGas = 0;

        // Procesar datos y organizar por tipo de sensor
        for (Measure measure : data) {
            String tipoSensor = measure.getSensor().getTipoSensor();
            float valor = measure.getValor();

            // Actualizar la última medición según el tipo de sensor
            switch (tipoSensor) {
                case "TEMPERATURA":
                    ultimaTemperatura = valor;
                    break;
                case "HUMEDAD":
                    ultimaHumedad = valor;
                    break;
                case "GAS":
                    ultimaGas = valor;
                    break;
            }
        }

        // Mostrar las últimas mediciones en los TextView
        txtUltimaTemperatura.setText("Última medición: " + ultimaTemperatura + " °C");
        txtUltimaHumedad.setText("Última medición: " + ultimaHumedad + "%");
        txtUltimaGas.setText("Última medición: " + ultimaGas + " ppm");


        // Crear listas para cada tipo de sensor
        Map<String, List<Entry>> datosPorSensor = new HashMap<>();
        datosPorSensor.put("TEMPERATURA", new ArrayList<>());
        datosPorSensor.put("HUMEDAD", new ArrayList<>());
        datosPorSensor.put("GAS", new ArrayList<>());

        // Variables para almacenar los límites de cada sensor
        float limiteInferiorTemperatura = Float.MAX_VALUE;
        float limiteSuperiorTemperatura = Float.MIN_VALUE;
        float limiteInferiorHumedad = Float.MAX_VALUE;
        float limiteSuperiorHumedad = Float.MIN_VALUE;
        float limiteInferiorGas = Float.MAX_VALUE;
        float limiteSuperiorGas = Float.MIN_VALUE;

        // Procesar datos y organizar por tipo de sensor
        for (int i = 0; i < data.size(); i++) {
            Measure measure = data.get(i);
            String tipoSensor = measure.getSensor().getTipoSensor();
            float valor = measure.getValor();

            if (datosPorSensor.containsKey(tipoSensor)) {
                datosPorSensor.get(tipoSensor).add(new Entry(i, valor));

                // Obtener los límites del sensor
                switch (tipoSensor) {
                    case "TEMPERATURA":
                        limiteInferiorTemperatura = measure.getSensor().getLimiteInferior();
                        limiteSuperiorTemperatura = measure.getSensor().getLimiteSuperior();
                        break;
                    case "HUMEDAD":
                        limiteInferiorHumedad = measure.getSensor().getLimiteInferior();
                        limiteSuperiorHumedad = measure.getSensor().getLimiteSuperior();
                        break;
                    case "GAS":
                        limiteInferiorGas = measure.getSensor().getLimiteInferior();
                        limiteSuperiorGas = measure.getSensor().getLimiteSuperior();
                        break;
                }
            }
        }

        // Ordenar las entradas por el valor de X (índice)
        for (List<Entry> entries : datosPorSensor.values()) {
            Collections.sort(entries, new EntryXComparator());
        }

        // Crear gráficos con límites dinámicos
        mostrarGrafica(chartTemperatura, datosPorSensor.get("TEMPERATURA"), "Temperatura", Color.RED, limiteInferiorTemperatura, limiteSuperiorTemperatura);
        mostrarGrafica(chartHumedad, datosPorSensor.get("HUMEDAD"), "Humedad", Color.BLUE, limiteInferiorHumedad, limiteSuperiorHumedad);
        mostrarGrafica(chartGas, datosPorSensor.get("GAS"), "Gas", Color.GREEN, limiteInferiorGas, limiteSuperiorGas);
    }

    private void mostrarGrafica(LineChart chart, List<Entry> valores, String etiqueta, int color, float limiteInferior, float limiteSuperior) {
        if (valores == null || valores.isEmpty()) return;

        // Crear el conjunto de datos
        LineDataSet dataSet = new LineDataSet(valores, etiqueta);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.BLACK); // Color del texto de los valores
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(color); // Color de los círculos en los puntos de datos
        dataSet.setCircleRadius(4f); // Tamaño de los círculos
        dataSet.setDrawCircleHole(false); // No dibujar un agujero en los círculos
        dataSet.setDrawValues(true); // Mostrar los valores encima de los puntos

        // Configurar el formato de los valores (opcional)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f", value); // Formatear a un decimal
            }
        });

        // Configurar el fondo de la gráfica
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);

        // Configurar el eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setValueFormatter(new IndexAxisValueFormatter()); // Formateador de etiquetas del eje X

        // Configurar el eje Y izquierdo
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);

        // Deshabilitar el eje Y derecho
        chart.getAxisRight().setEnabled(false);


        // Configurar la leyenda
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        // Configurar la descripción
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        // Configurar la interactividad
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        // Configurar los datos
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        //Configuración en eje Y para los límites
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == limiteInferior) {
                    return "Lím. Inf: " + value;
                } else if (value == limiteSuperior) {
                    return "Lím. Sup: " + value;
                }
                return String.valueOf(value); // Mostrar otros valores normalmente
            }
        });

        // Animación
        chart.animateY(1000, Easing.EaseInOutQuad);

        // Refrescar la gráfica
        chart.invalidate();
    }

    // Formateador personalizado para el eje X
    private static class IndexAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            // Aquí puedes formatear el valor como desees, por ejemplo, convertirlo a una fecha
            return String.valueOf((int) value); // Ejemplo simple: muestra el índice
        }
    }
}