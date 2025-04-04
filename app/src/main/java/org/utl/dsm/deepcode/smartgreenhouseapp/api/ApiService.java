package org.utl.dsm.deepcode.smartgreenhouseapp.api;

import org.utl.dsm.deepcode.smartgreenhouseapp.model.ApiResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.LoginRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SignupRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.UsuarioDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<ApiResponse<UsuarioDTO>> login(@Body LoginRequest loginRequest);

    @POST("auth/signup")
    Call<ApiResponse<UsuarioDTO>> signup(@Body SignupRequest signupRequest);
}