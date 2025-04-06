package org.utl.dsm.deepcode.smartgreenhouseapp.api;

import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.UsuarioData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UsuarioApiService {
    @GET("usuarios")
    Call<ApiResponse<List<UsuarioData>>> getUsuarios();

    @GET("usuarios/{id}")
    Call<ApiResponse<UsuarioData>> findUserById(@Path("id") int idUsuario);

    @POST("usuarios/delete/{id}")
    Call<ApiResponse<Void>> deleteUsuario(@Path("id") int idUsuario);
    @POST("usuarios/update")
    Call<ApiResponse<UsuarioData>> actualizarUsuario(@Body UsuarioData usuario);
}
