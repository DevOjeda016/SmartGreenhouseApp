package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.animation.Easing;
import com.google.android.material.button.MaterialButton;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.SensorApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.MedicionDTO;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.MeasureResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class graficas extends AppCompatActivity {
    private LineChart chartTemperatura, chartHumedad, chartGas;
    private TextView txtUltimaActualizacion, txtUltimaTemperatura, txtUltimaHumedad, txtUltimaGas;
    private Button btnActualizar;

    private MaterialButton iconButton;
    private String ultimaFechaActualizacion = "-";

    // Límites de los sensores
    private float LIMITE_INFERIOR_TEMPERATURA = 10f;
    private  float LIMITE_SUPERIOR_TEMPERATURA = 35f;
    private  float LIMITE_INFERIOR_HUMEDAD = 30f;
    private float LIMITE_SUPERIOR_HUMEDAD = 80f;
    private float LIMITE_INFERIOR_GAS = 0f;
    private float LIMITE_SUPERIOR_GAS = 100f;

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
        getLimits();

        // Asignar gráficos desde el XML
        chartTemperatura = findViewById(R.id.chartTemperatura);
        chartHumedad = findViewById(R.id.chartHumedad);
        chartGas = findViewById(R.id.chartGas);

        // Configurar gráficos inicialmente
        configurarGrafica(chartTemperatura, "Temperatura (°C)");
        configurarGrafica(chartHumedad, "Humedad (%)");
        configurarGrafica(chartGas, "Gas (ppm)");

        // Asignar TextView y Button
        txtUltimaActualizacion = findViewById(R.id.txtUltimaActualizacion);
        txtUltimaTemperatura = findViewById(R.id.txtUltimaTemperatura);
        txtUltimaHumedad = findViewById(R.id.txtUltimaHumedad);
        txtUltimaGas = findViewById(R.id.txtUltimaGas);
        btnActualizar = findViewById(R.id.btnActualizar);
        iconButton = findViewById(R.id.iconButton);
        iconButton.setOnClickListener(v -> finish());

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SensorApiService apiService = retrofit.create(SensorApiService.class);

        // Configurar el botón de actualización
        btnActualizar.setOnClickListener(v -> actualizarDatos(apiService));

        // Realizar la primera actualización al iniciar la actividad
        actualizarDatos(apiService);
    }

    private void configurarGrafica(LineChart chart, String label) {
        // Configuración básica del gráfico
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        // Configurar el eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setGranularity(1f); // Pasos de 1 en 1
        xAxis.setLabelCount(10); // Mostrar aproximadamente 10 etiquetas
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Mostrar valores enteros en el eje X
            }
        });

        // Configurar el eje Y izquierdo
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);

        // Configurar el eje Y derecho
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false); // Deshabilitar eje derecho

        // Configurar la leyenda
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // Configurar la descripción
        Description description = new Description();
        description.setText(label);
        description.setTextColor(Color.BLACK);
        description.setTextSize(12f);
        chart.setDescription(description);
    }

    private void actualizarDatos(SensorApiService apiService) {
        // Realizar las tres llamadas a la API (una por cada tipo de sensor)
        Call<MeasureResponse> callTemperatura = apiService.getMeasureData("temperatura");
        Call<MeasureResponse> callHumedad = apiService.getMeasureData("humedad");
        Call<MeasureResponse> callGas = apiService.getMeasureData("gas");

        // Contador para saber cuándo todas las llamadas han terminado
        final int[] contadorLlamadas = {0};
        final int TOTAL_LLAMADAS = 3;

        // Listas para almacenar los datos de cada sensor
        final List<MedicionDTO>[] datosTemperatura = new List[]{new ArrayList<>()};
        final List<MedicionDTO>[] datosHumedad = new List[]{new ArrayList<>()};
        final List<MedicionDTO>[] datosGas = new List[]{new ArrayList<>()};

        // Callback genérico para manejar las respuestas
        Callback<MeasureResponse> callback = new Callback<MeasureResponse>() {
            @Override
            public void onResponse(Call<MeasureResponse> call, Response<MeasureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeasureResponse measureResponse = response.body();
                    String endpoint = call.request().url().toString();

                    // Determinar qué tipo de datos estamos recibiendo
                    if (endpoint.contains("temperatura")) {
                        datosTemperatura[0] = measureResponse.getData();
                    } else if (endpoint.contains("humedad")) {
                        datosHumedad[0] = measureResponse.getData();
                    } else if (endpoint.contains("gas")) {
                        datosGas[0] = measureResponse.getData();
                    }
                }

                contadorLlamadas[0]++;
                if (contadorLlamadas[0] == TOTAL_LLAMADAS) {
                    // Todas las llamadas han terminado, procesar los datos
                    procesarDatosYMostrarGraficas(datosTemperatura[0], datosHumedad[0], datosGas[0]);

                    // Actualizar la fecha de la última actualización
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getDefault());
                    ultimaFechaActualizacion = sdf.format(new Date());
                    txtUltimaActualizacion.setText("Última actualización: " + ultimaFechaActualizacion);
                }
            }

            @Override
            public void onFailure(Call<MeasureResponse> call, Throwable t) {
                t.printStackTrace();
                contadorLlamadas[0]++;
                if (contadorLlamadas[0] == TOTAL_LLAMADAS) {
                    // Aunque haya fallos, intentar mostrar los datos que sí se obtuvieron
                    procesarDatosYMostrarGraficas(datosTemperatura[0], datosHumedad[0], datosGas[0]);
                }
            }
        };

        // Ejecutar las tres llamadas
        callTemperatura.enqueue(callback);
        callHumedad.enqueue(callback);
        callGas.enqueue(callback);
    }

    private void procesarDatosYMostrarGraficas(List<MedicionDTO> datosTemperatura,
                                               List<MedicionDTO> datosHumedad,
                                               List<MedicionDTO> datosGas) {
        // Mostrar las últimas mediciones en los TextView
        if (datosTemperatura != null && !datosTemperatura.isEmpty()) {
            float ultimaTemp = datosTemperatura.get(datosTemperatura.size() - 1).getValor();
            txtUltimaTemperatura.setText("Última medición: " + ultimaTemp + " °C");
        }

        if (datosHumedad != null && !datosHumedad.isEmpty()) {
            float ultimaHum = datosHumedad.get(datosHumedad.size() - 1).getValor();
            txtUltimaHumedad.setText("Última medición: " + ultimaHum + "%");
        }

        if (datosGas != null && !datosGas.isEmpty()) {
            float ultimaGas = datosGas.get(datosGas.size() - 1).getValor();
            txtUltimaGas.setText("Última medición: " + ultimaGas + " ppm");
        }

        // Preparar datos para las gráficas
        List<Entry> entradasTemperatura = convertirMedicionesAEntradas(datosTemperatura);
        List<Entry> entradasHumedad = convertirMedicionesAEntradas(datosHumedad);
        List<Entry> entradasGas = convertirMedicionesAEntradas(datosGas);

        // Mostrar gráficas con sus respectivos límites
        mostrarGrafica(chartTemperatura, entradasTemperatura, "Temperatura", Color.RED,
                LIMITE_INFERIOR_TEMPERATURA, LIMITE_SUPERIOR_TEMPERATURA);
        mostrarGrafica(chartHumedad, entradasHumedad, "Humedad", Color.BLUE,
                LIMITE_INFERIOR_HUMEDAD, LIMITE_SUPERIOR_HUMEDAD);
        mostrarGrafica(chartGas, entradasGas, "Gas", Color.GREEN,
                LIMITE_INFERIOR_GAS, LIMITE_SUPERIOR_GAS);
    }

    private List<Entry> convertirMedicionesAEntradas(List<MedicionDTO> mediciones) {
        List<Entry> entradas = new ArrayList<>();
        if (mediciones == null) return entradas;

        // Convertir cada medición a una entrada en el gráfico
        for (int i = 0; i < mediciones.size(); i++) {
            MedicionDTO medicion = mediciones.get(i);
            entradas.add(new Entry(i, medicion.getValor()));
        }

        // Ordenar las entradas por el valor de X (índice)
        Collections.sort(entradas, new EntryXComparator());
        return entradas;
    }

    private void mostrarGrafica(LineChart chart, List<Entry> valores, String etiqueta,
                                int color, float limiteInferior, float limiteSuperior) {
        if (valores == null || valores.isEmpty()) return;

        // Crear el conjunto de datos
        LineDataSet dataSet = new LineDataSet(valores, etiqueta);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(true); // Mostrar valores sobre los puntos

        // Configurar el formato de los valores
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f", value);
            }
        });

        // Configurar los datos
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Configurar el eje Y izquierdo con límites
        YAxis leftAxis = chart.getAxisLeft();

        // Añadir líneas de límite
        LimitLine llInferior = new LimitLine(limiteInferior, "Límite Inferior");
        llInferior.setLineColor(Color.RED);
        llInferior.setLineWidth(1f);
        llInferior.setTextColor(Color.BLACK);
        llInferior.setTextSize(10f);

        LimitLine llSuperior = new LimitLine(limiteSuperior, "Límite Superior");
        llSuperior.setLineColor(Color.RED);
        llSuperior.setLineWidth(1f);
        llSuperior.setTextColor(Color.BLACK);
        llSuperior.setTextSize(10f);

        leftAxis.removeAllLimitLines(); // Limpiar líneas anteriores
        leftAxis.addLimitLine(llInferior);
        leftAxis.addLimitLine(llSuperior);

        // Calcular valores mínimos y máximos de los datos
        float minDataValue = Float.MAX_VALUE;
        float maxDataValue = Float.MIN_VALUE;

        for (Entry entry : valores) {
            if (entry.getY() < minDataValue) minDataValue = entry.getY();
            if (entry.getY() > maxDataValue) maxDataValue = entry.getY();
        }

        // Determinar los límites del eje Y considerando tanto los datos como los límites configurados
        float yMin = Math.min(minDataValue, limiteInferior);
        float yMax = Math.max(maxDataValue, limiteSuperior);

        // Añadir un margen del 15% para mejor visualización
        float margin = (yMax - yMin) * 0.15f;
        yMin = yMin - margin;
        yMax = yMax + margin;

        // Asegurar que no haya valores negativos si los límites inferiores son positivos
        if (limiteInferior >= 0 && yMin < 0) yMin = 0;

        // Configurar los límites del eje Y
        leftAxis.setAxisMinimum(yMin);
        leftAxis.setAxisMaximum(yMax);

        // Configurar el eje X para que muestre todos los puntos
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(Math.max(valores.size() - 1, 0)); // Asegurar que no sea negativo

        // Animación
        chart.animateY(1000, Easing.EaseInOutQuad);

        // Refrescar la gráfica
        chart.invalidate();
    }

    private void getLimits() {
        final SensorResponse[] sensorResponse = new SensorResponse[1];
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
                    sensorResponse[0] = response.body();
                    assert sensorResponse[0] != null;
                    LIMITE_INFERIOR_TEMPERATURA = sensorResponse[0].getData().get(0).getLimiteInferior();
                    LIMITE_SUPERIOR_TEMPERATURA = sensorResponse[0].getData().get(0).getLimiteSuperior();
                    LIMITE_INFERIOR_HUMEDAD = sensorResponse[0].getData().get(1).getLimiteInferior();
                    LIMITE_SUPERIOR_HUMEDAD = sensorResponse[0].getData().get(1).getLimiteSuperior();
                    LIMITE_INFERIOR_GAS = sensorResponse[0].getData().get(2).getLimiteInferior();
                    LIMITE_SUPERIOR_GAS = sensorResponse[0].getData().get(2).getLimiteSuperior();

                }
            }

            @Override
            public void onFailure(Call<SensorResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });

    }
}