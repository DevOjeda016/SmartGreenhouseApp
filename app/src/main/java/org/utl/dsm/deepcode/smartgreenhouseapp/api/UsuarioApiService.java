package org.utl.dsm.deepcode.smartgreenhouseapp.api;

import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UsuarioApiService {
    @GET("usuarios")
    Call<ApiResponse> getUsuarios();
    @POST("usuarios/delete/{id}")
    Call<ApiResponse> deleteUsuario(@Path("id") int idUsuario);
}
