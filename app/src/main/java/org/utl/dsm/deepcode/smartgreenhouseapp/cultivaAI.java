package org.utl.dsm.deepcode.smartgreenhouseapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.utl.dsm.deepcode.smartgreenhouseapp.api.ApiService;
import org.utl.dsm.deepcode.smartgreenhouseapp.api.DeepSeekApiService;
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
    private Button sendButton;
    private EditText inputMessage;
    private LinearLayout chatContainer;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cultiva_ai);

        // Habilitar el modo EdgeToEdge
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        sendButton = findViewById(R.id.sendButton);
        inputMessage = findViewById(R.id.inputMessage);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView = findViewById(R.id.scrollView2);

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

        String welcomeMessage = "👋 ¡Hola! Soy un experto en agronomía e invernaderos. 🌱\n" +
                "Puedo ayudarte con:\n" +
                "- 🌿 Cultivos en invernadero\n" +
                "- 🌡️ Control climático\n" +
                "- 💧 Riego automatizado\n" +
                "- 🐜 Manejo de plagas\n" +
                "- 🌱 Nutrición vegetal\n\n" +
                "Si tu pregunta no está relacionada, te avisaré amablemente. 😊";
        addAIMessage(welcomeMessage);
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
        String systemMessage = "Eres un experto en agronomía e invernaderos. Responde solo preguntas relacionadas con:\n" +
                "- Cultivos en invernadero\n" +
                "- Control climático\n" +
                "- Riego automatizado\n" +
                "- Manejo de plagas\n" +
                "- Nutrición vegetal\n" +
                "- Temperatura, humedad, luz, CO2\n" +
                "Si la pregunta está fuera de estos temas, responde que no puedes responder de manera amable y con emojis amigables'.";

        String[] typingMessages = {
                "Déjame pensar en eso... 🧠",
                "Cultivando la respuesta perfecta para ti... 🌿",
                "Regando las ideas para darte la mejor solución... 💧",
                "Un momento, estoy buscando la mejor respuesta... 🌱",
                "Procesando tu consulta, espera un instante... ⏳"
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
        DeepSeekRequest request = new DeepSeekRequest("deepseek-chat", List.of(message)); // Reemplaza "deepseek-chat" con el modelo correcto

        // Llamar a la API
        apiService.sendMessage("Bearer sk-02598b6112a44b568786f00b804a84c3", request).enqueue(new Callback<DeepSeekResponse>() {
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
                    Thread.sleep(50); // Simula el tiempo de escritura
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}