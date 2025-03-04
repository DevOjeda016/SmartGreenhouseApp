package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    Button btnRegister;
    ScrollView scrollView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar el botón de registro
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        // Inicializar el ScrollView
        scrollView = findViewById(R.id.scrollView);

        // Configurar el listener para ocultar el teclado al tocar fuera de los campos de texto
        View mainLayout = findViewById(R.id.main);
        mainLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });

        // Configurar el ScrollView para que se ajuste al teclado
        setupScrollView();
    }

    // Método para ocultar el teclado
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Método para configurar el ScrollView
    private void setupScrollView() {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Verificar si el teclado está visible
            int scrollViewHeight = scrollView.getRootView().getHeight();
            int scrollViewBottom = scrollView.getBottom();
            int keyboardHeight = scrollViewHeight - scrollViewBottom;

            if (keyboardHeight > scrollViewHeight * 0.15) { // Teclado visible
                // Desplazar el ScrollView para enfocar el campo de texto activo
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    scrollView.smoothScrollTo(0, focusedView.getBottom());
                }
            }
        });
    }
}