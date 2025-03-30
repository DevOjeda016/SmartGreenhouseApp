package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.SensorApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.MeasureResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class menu_admin extends AppCompatActivity {
    private TextView textView2;
    private TextView percentageText;
    private TextView percentageText1;
    private TextView percentageText2;
    private String ultimaFechaActualizacion = "-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupSmallCards();

        setupLargeCards();
        textView2 = findViewById(R.id.textView2);
        percentageText = findViewById(R.id.percentageText);
        percentageText1 = findViewById(R.id.percentageText1);
        percentageText2 = findViewById(R.id.percentageText2);



    }

    protected void onResume() {

        super.onResume();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL) // URL base de la API
                .addConverterFactory(GsonConverterFactory.create()) // Conversor de JSON a objetos Java
                .build();
        SensorApiService apiService = retrofit.create(SensorApiService.class);
        actualizarDatos(apiService);
    }

    private void setupSmallCards() {
        MaterialCardView specialCard = findViewById(R.id.specialCard);
        MaterialCardView smallCard1 = findViewById(R.id.smallCard1);
        MaterialCardView smallCard2 = findViewById(R.id.smallCard2);

    }

    private void actualizarDatos(SensorApiService apiService) {
        // Realizar la llamada a la API
        Call<MeasureResponse> call = apiService.getMeasureData();
        call.enqueue(new Callback<MeasureResponse>() {
            @Override
            public void onResponse(Call<MeasureResponse> call, Response<MeasureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeasureResponse measureResponse = response.body();
                    for (int  i = 0; i < measureResponse.getData().size(); i++) {
                        if (measureResponse.getData().get(i).getSensor().getTipoSensor().equalsIgnoreCase("Temperatura")) {
                            percentageText.setText(measureResponse.getData().get(i).getValor() + " °C");
                        }
                        if (measureResponse.getData().get(i).getSensor().getTipoSensor().equalsIgnoreCase("Humedad")) {
                            percentageText1.setText(measureResponse.getData().get(i).getValor() + " %");
                        }
                        if (measureResponse.getData().get(i).getSensor().getTipoSensor().equalsIgnoreCase("Gas")) {
                            percentageText2.setText(measureResponse.getData().get(i).getValor() + " ppm");
                        }
                    }
                    measureResponse.getData().get(0).getSensor().getTipoSensor();
                    percentageText.setText(measureResponse.getData().get(3).getValor() + " °C");
                    percentageText1.setText(measureResponse.getData().get(2).getValor() + " %");
                    percentageText2.setText(measureResponse.getData().get(1).getValor() + " ppm");

                    // Actualizar la fecha de la última actualización
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getDefault()); // Usar la zona horaria del dispositivo
                    ultimaFechaActualizacion = sdf.format(new Date());
                    textView2.setText("Última actualización: " + ultimaFechaActualizacion);
                }
            }

            @Override
            public void onFailure(Call<MeasureResponse> call, Throwable t) {
                Toast.makeText(menu_admin.this, "Error al obtener los datos", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void setupLargeCards() {
        // Tarjeta de plantación
        MaterialCardView plantingCard = findViewById(R.id.materialCardView5);
        plantingCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ficha_plantacion.class));
        });

        // Tarjeta de Cultiva IA
        MaterialCardView cultivaAICard = findViewById(R.id.materialCardView7);
        cultivaAICard.setOnClickListener(v -> {
            startActivity(new Intent(this, cultivaAI.class));
        });

        // Tarjeta de monitoreo de sensores
        MaterialCardView monitorSensorsCard = findViewById(R.id.materialCardView);
        monitorSensorsCard.setOnClickListener(v -> {
            startActivity(new Intent(this, MonitoringActivity.class));
        });

        // Tarjeta de control de sensores
        MaterialCardView controlSensorsCard = findViewById(R.id.materialCardView6);
        controlSensorsCard.setOnClickListener(v -> {
            startActivity(new Intent(this, controladores.class));
        });

        // Tarjeta de gráficos de datos
        MaterialCardView graphDataCard = findViewById(R.id.materialCardView8);
        graphDataCard.setOnClickListener(v -> {
            startActivity(new Intent(this, graficas.class));
        });

        // Tarjeta de control de emergencia
        MaterialCardView emergencyControlCard = findViewById(R.id.materialCardView11);
        emergencyControlCard.setOnClickListener(v -> {
            startActivity(new Intent(this, control_de_emergencia.class));
        });

        // Tarjeta de usuarios
        MaterialCardView usersCard = findViewById(R.id.materialCardView12);
        usersCard.setOnClickListener(v -> {
            startActivity(new Intent(this, usuarios.class));
        });
    }
}