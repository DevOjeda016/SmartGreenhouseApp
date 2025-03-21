package org.utl.dsm.deepcode.smartgreenhouseapp.api;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.DeepSeekRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.DeepSeekResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
public interface DeepSeekApiService {
    @POST("/chat/completions")
    Call<DeepSeekResponse> sendMessage(
            @Header("Authorization") String authorization,
            @Body DeepSeekRequest request
    );
}