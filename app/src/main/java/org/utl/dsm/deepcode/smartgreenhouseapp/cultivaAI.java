package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.ApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.api.DeepSeekApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.DeepSeekRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.DeepSeekResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.Random;

public class cultivaAI extends AppCompatActivity {
    private LinearLayout inputLayout;
    private Button sendButton;
    private EditText inputMessage;
    private LinearLayout chatContainer;
    private ScrollView scrollView;
    MaterialButton iconButton;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Aseguramos que el teclado empujará hacia arriba la UI
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.activity_cultiva_ai);

        // Habilitamos EdgeToEdge para mejor visualización
        EdgeToEdge.enable(this);

        // Inicializar vistas
        rootView = findViewById(R.id.main);
        inputLayout = findViewById(R.id.inputLayout);
        sendButton = findViewById(R.id.sendButton);
        inputMessage = findViewById(R.id.inputMessage);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView = findViewById(R.id.scrollView2);
        iconButton = findViewById(R.id.iconButton);
        iconButton.setOnClickListener(v -> finish());

        // Configurar insets para manejar correctamente la interfaz de sistema
        setupSystemInsets();

        // Estilizar el campo de entrada
        styleInputField();

        // Configurar focus listener para el input
        setupInputFocus();

        // Configurar el botón de enviar
        setupSendButton();

        // Mensaje de bienvenida
        String welcomeMessage = getString(R.string.welcome_message);
        addAIMessage(welcomeMessage);

        // Añadir detector de cambios de teclado para ajustar la vista
        setupKeyboardVisibilityDetector();
    }

    private void setupSystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Quitamos el padding inferior

            // Configurar insets para inputLayout para que esté por encima del teclado
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            inputLayout.setPadding(inputLayout.getPaddingLeft(),
                    inputLayout.getPaddingTop(),
                    inputLayout.getPaddingRight(),
                    imeInsets.bottom);

            return insets;
        });
    }

    private void setupInputFocus() {
        inputMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Desplaza hacia abajo cuando el EditText obtiene foco
                scrollToBottom();
            }
        });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // Agregar el mensaje del usuario al chat
                addUserMessage(message);

                // Limpiar el campo de entrada
                inputMessage.setText("");

                // Llamar a la API para obtener una respuesta
                callChatAPI(message);
            }
        });
    }

    // Configurar detector de visibilidad del teclado más robusto
    private void setupKeyboardVisibilityDetector() {
        // Usa WindowInsetsControllerCompat para detectar cambios en la UI
        final View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            boolean isKeyboardVisible = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            }

            if (isKeyboardVisible) {
                // Cuando el teclado es visible, aseguramos que el área de chat se desplaza
                scrollToBottom();
            }

            return v.onApplyWindowInsets(insets);
        });

        // Añadimos un detector de layout para casos en los que el listener anterior no se active
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastHeight = 0;

            @Override
            public void onGlobalLayout() {
                int currentHeight = rootView.getHeight();

                // Si la altura cambia significativamente, posiblemente sea por el teclado
                if (Math.abs(lastHeight - currentHeight) > currentHeight * 0.15) {
                    lastHeight = currentHeight;
                    scrollToBottom();
                }
            }
        });
    }

    // Método para desplazar el chat hacia abajo
    private void scrollToBottom() {
        // Aseguramos que se hace después de que el layout se haya actualizado
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    // Método para estilizar el campo de entrada
    private void styleInputField() {
        inputMessage.setBackgroundResource(R.drawable.bg_input_field);
    }

    // Método para agregar un mensaje del usuario
    private void addUserMessage(String message) {
        View userMessageView = getLayoutInflater().inflate(R.layout.item_message_user, null);
        TextView messageText = userMessageView.findViewById(R.id.messageText);
        messageText.setText(message);
        chatContainer.addView(userMessageView);
        scrollToBottom();
    }

    // Método para agregar un mensaje de la IA
    private TextView addAIMessage(String message) {
        View aiMessageView = getLayoutInflater().inflate(R.layout.item_message_ai, null);
        TextView messageText = aiMessageView.findViewById(R.id.messageText);
        messageText.setText(message);
        chatContainer.addView(aiMessageView);
        scrollToBottom();
        return messageText;
    }

    // Método para llamar a la API de chat
    private void callChatAPI(String userMessage) {
        String systemMessage = getString(R.string.system_message);

        String[] typingMessages = {
                getString(R.string.typing_message_1),
                getString(R.string.typing_message_2),
                getString(R.string.typing_message_3),
                getString(R.string.typing_message_4),
                getString(R.string.typing_message_5)
        };

        String typingMessage = getRandomTypingMessage(typingMessages);

        TextView aiMessageView = addAIMessage(typingMessage);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deepseek.com/") // Reemplaza con la URL correcta
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        DeepSeekApiService apiService = retrofit.create(DeepSeekApiService.class);


        // Crear la solicitud
        DeepSeekRequest.Message message = new DeepSeekRequest.Message("user", systemMessage + userMessage);
        DeepSeekRequest request = new DeepSeekRequest(Globals.DEEPSEEK_MODEL, List.of(message)); // Reemplaza "deepseek-chat" con el modelo correcto

        // Llamar a la API
        apiService.sendMessage("Bearer " + Globals.DEEPSEEK_API_KEY, request).enqueue(new Callback<DeepSeekResponse>() {
            @Override
            public void onResponse(Call<DeepSeekResponse> call, Response<DeepSeekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getChoices().get(0).getMessage().getContent();
                    simulateTyping(aiResponse, aiMessageView);
                    //aiMessageView.setText(aiResponse);
                } else {
                    String errorBody = "Error desconocido";
                    try {
                        errorBody = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("API_ERROR", "Error en la respuesta: " + errorBody);
                    addAIMessage("Error al obtener la respuesta de la IA: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<DeepSeekResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error de conexión: " + t.getMessage());
                addAIMessage("Error de conexión: " + t.getMessage());
            }
        });
    }

    private String getRandomTypingMessage(String[] messages) {
        Random random = new Random();
        int index = random.nextInt(messages.length); // Índice aleatorio
        return messages[index];
    }

    private void simulateTyping(String message, TextView messageText) {
        new Thread(() -> {
            StringBuilder response = new StringBuilder();
            for (char c : message.toCharArray()) {
                response.append(c);
                // Metodo para actualizar la interfaz de la UI
                runOnUiThread(() -> messageText.setText(response.toString()));
                try {
                    Thread.sleep(10); // Simula el tiempo de escritura
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}