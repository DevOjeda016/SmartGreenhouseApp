package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class control_de_emergencia extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_control_de_emergencia);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar el botón de retroceso
        MaterialButton backButton = findViewById(R.id.iconButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Configurar el botón de emergencia
        Button emergencyButton = findViewById(R.id.button);
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar mensaje de que el botón está temporalmente inhabilitado
                showEmergencyButtonDisabledMessage();
            }
        });
    }

    private void showEmergencyButtonDisabledMessage() {
        // Mostrar un diálogo de alerta
        new MaterialAlertDialogBuilder(this)
                .setTitle("Botón Inhabilitado")
                .setMessage("Por seguridad, el botón de paro de emergencia se encuentra temporalmente inhabilitado.")
                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                .show();

        // Alternativamente, puedes usar un Toast
        // Toast.makeText(this, "Por seguridad, el botón de paro de emergencia se encuentra temporalmente inhabilitado.", Toast.LENGTH_LONG).show();
    }
}