package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.SensorApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.api.UsuarioApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.MeasureResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.UsuarioData;

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
    private MaterialButton iconButton, iconButton3;
    private TextView percentageText;  // Temperatura
    private TextView percentageText1; // Humedad
    private TextView percentageText2; // Gas
    private String ultimaFechaActualizacion = "-";

    private TextView txtTitle;
    private int id;
    private UsuarioData usuario;

    @SuppressLint("SetTextI18n")
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

        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String aPaterno = intent.getStringExtra("aPaterno");
        id = intent.getIntExtra("id", 0);


        setupSmallCards();
        setupLargeCards();

        // Inicializar TextViews
        textView2 = findViewById(R.id.textView2);
        percentageText = findViewById(R.id.percentageText);
        percentageText1 = findViewById(R.id.percentageText1);
        percentageText2 = findViewById(R.id.percentageText2);
        iconButton = findViewById(R.id.iconButton);
        iconButton.setOnClickListener(v -> goToProfileActivity());
        iconButton3 = findViewById(R.id.iconButton3);
        iconButton3.setOnClickListener(v -> logout());
        loadDataUser();
        txtTitle = findViewById(R.id.txtTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SensorApiService apiService = retrofit.create(SensorApiService.class);
        actualizarDatos(apiService);
        loadDataUser();
    }

    private void setupSmallCards() {
        MaterialCardView specialCard = findViewById(R.id.specialCard);
        MaterialCardView smallCard1 = findViewById(R.id.smallCard1);
        MaterialCardView smallCard2 = findViewById(R.id.smallCard2);
        // Puedes añadir listeners si es necesario
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        finishAffinity();
    }

    private void actualizarDatos(SensorApiService apiService) {
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
                        float ultimaTemp = measureResponse.getData().get(measureResponse.getData().size() - 1).getValor();
                        percentageText.setText(ultimaTemp + " °C");
                    } else if (endpoint.contains("humedad") && !measureResponse.getData().isEmpty()) {
                        float ultimaHum = measureResponse.getData().get(measureResponse.getData().size() - 1).getValor();
                        percentageText1.setText(ultimaHum + " %");
                    } else if (endpoint.contains("gas") && !measureResponse.getData().isEmpty()) {
                        float ultimaGas = measureResponse.getData().get(measureResponse.getData().size() - 1).getValor();
                        percentageText2.setText(ultimaGas + " ppm");
                    }
                }

                contadorLlamadas[0]++;
                if (contadorLlamadas[0] == TOTAL_LLAMADAS) {
                    // Actualizar la fecha solo cuando todas las llamadas hayan terminado
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getDefault());
                    ultimaFechaActualizacion = sdf.format(new Date());
                    textView2.setText("Última actualización: " + ultimaFechaActualizacion);
                }
            }

            @Override
            public void onFailure(Call<MeasureResponse> call, Throwable t) {
                Toast.makeText(menu_admin.this, "Error al obtener datos de " + call.request().url(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                contadorLlamadas[0]++;
            }
        };

        // Ejecutar las tres llamadas
        callTemperatura.enqueue(callback);
        callHumedad.enqueue(callback);
        callGas.enqueue(callback);
    }

    private void loadDataUser() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsuarioApiService apiService = retrofit.create(UsuarioApiService.class);
        Call<ApiResponse<UsuarioData>> call = apiService.findUserById(id);
        call.enqueue(new Callback<ApiResponse<UsuarioData>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ApiResponse<UsuarioData>> call, Response<ApiResponse<UsuarioData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UsuarioData> apiResponse = response.body();
                    if (apiResponse.getStatus() == 200 && apiResponse.getData() != null) {
                        usuario = apiResponse.getData();
                        String[] names = usuario.getPersona().getNombre().split(" ");
                        String[] apellidos = usuario.getPersona().getAPaterno().split(" ");
                        txtTitle.setText(names[0] + " " + apellidos[0]);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UsuarioData>> call, Throwable t) {
                Toast.makeText(menu_admin.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
    private void goToProfileActivity() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsuarioApiService apiService = retrofit.create(UsuarioApiService.class);
        Call<ApiResponse<UsuarioData>> call = apiService.findUserById(id);
        call.enqueue(new Callback<ApiResponse<UsuarioData>>() {
            @Override
            public void onResponse(Call<ApiResponse<UsuarioData>> call, Response<ApiResponse<UsuarioData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UsuarioData> apiResponse = response.body();
                    if (apiResponse.getStatus() == 200 && apiResponse.getData() != null) {
                        UsuarioData usuarioFound = apiResponse.getData();
                        Intent intent = new Intent(menu_admin.this, PerfilActivity.class);
                        intent.putExtra("usuario", usuarioFound);
                        intent.putExtra("isProfile", true);
                        intent.putExtra("usuarioLogged", usuario);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UsuarioData>> call, Throwable t) {
                Toast.makeText(menu_admin.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void setupLargeCards() {
        // Tarjeta de plantación
        MaterialCardView plantingCard = findViewById(R.id.materialCardView5);
        plantingCard.setOnClickListener(v -> startActivity(new Intent(this, ficha_plantacion.class)));

        // Tarjeta de Cultiva IA
        MaterialCardView cultivaAICard = findViewById(R.id.materialCardView7);
        cultivaAICard.setOnClickListener(v -> startActivity(new Intent(this, cultivaAI.class)));

        // Tarjeta de monitoreo de sensores
        MaterialCardView monitorSensorsCard = findViewById(R.id.materialCardView);
        monitorSensorsCard.setOnClickListener(v -> startActivity(new Intent(this, MonitoringActivity.class)));

        // Tarjeta de control de sensores
        MaterialCardView controlSensorsCard = findViewById(R.id.materialCardView6);
        controlSensorsCard.setOnClickListener(v -> startActivity(new Intent(this, controladores.class)));

        // Tarjeta de gráficos de datos
        MaterialCardView graphDataCard = findViewById(R.id.materialCardView8);
        graphDataCard.setOnClickListener(v -> startActivity(new Intent(this, graficas.class)));

        // Tarjeta de control de emergencia
        MaterialCardView emergencyControlCard = findViewById(R.id.materialCardView11);
        emergencyControlCard.setOnClickListener(v -> startActivity(new Intent(this, control_de_emergencia.class)));

        // Tarjeta de usuarios
        MaterialCardView usersCard = findViewById(R.id.materialCardView12);
        usersCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsuariosActivity.class);
            intent.putExtra("usuarioLogged", usuario);
            startActivity(intent);
        });
    }
}