package org.utl.dsm.deepcode.smartgreenhouseapp.api;

import org.utl.dsm.deepcode.smartgreenhouseapp.model.MeasureResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SensorApiService {
    @GET("sensor/measure") // Endpoint de la API
    Call<MeasureResponse> getMeasureData();
}