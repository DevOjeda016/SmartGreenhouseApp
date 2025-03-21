package org.utl.dsm.deepcode.smartgreenhouseapp.api;

import org.utl.dsm.deepcode.smartgreenhouseapp.model.LoginRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.LoginResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SignupRequest;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SignupResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    @POST("auth/signup")
    Call<SignupResponse> signup(@Body SignupRequest signupRequest);
}
