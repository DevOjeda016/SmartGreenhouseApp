package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;

public class menu_admin extends AppCompatActivity {

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
    }

    private void setupSmallCards() {
        MaterialCardView specialCard = findViewById(R.id.specialCard);
        MaterialCardView smallCard1 = findViewById(R.id.smallCard1);
        MaterialCardView smallCard2 = findViewById(R.id.smallCard2);

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
            startActivity(new Intent(this, UsuariosActivity.class));
        });
    }
}