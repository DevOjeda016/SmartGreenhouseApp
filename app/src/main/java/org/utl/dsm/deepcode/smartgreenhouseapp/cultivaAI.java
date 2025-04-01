package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.graphics.Rect;
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
    private LinearLayout inputLayout; // <-- Añade esta línea
    private Button sendButton;
    private EditText inputMessage;
    private LinearLayout chatContainer;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.activity_cultiva_ai);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inputLayout = findViewById(R.id.inputLayout); // <-- Añade esta línea
        sendButton = findViewById(R.id.sendButton);
        inputMessage = findViewById(R.id.inputMessage);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView = findViewById(R.id.scrollView2);

        // Estilizar el campo de entrada
        styleInputField();

        // Configurar focus listener para el input
        inputMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Desplaza hacia abajo cuando el EditText obtiene foco
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        });

        // Configurar el botón de enviar
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

        String welcomeMessage = getString(R.string.welcome_message);
        addAIMessage(welcomeMessage);

        // Configurar listener para detectar cambios en el layout cuando aparece el teclado
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
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    // Método para agregar un mensaje de la IA
    private TextView addAIMessage(String message) {
        View aiMessageView = getLayoutInflater().inflate(R.layout.item_message_ai, null);
        TextView messageText = aiMessageView.findViewById(R.id.messageText);
        messageText.setText(message);
        chatContainer.addView(aiMessageView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
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