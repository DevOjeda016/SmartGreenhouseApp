package org.utl.dsm.deepcode.smartgreenhouseapp.api;

import org.utl.dsm.deepcode.smartgreenhouseapp.model.MeasureResponse;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorDTO;
import org.utl.dsm.deepcode.smartgreenhouseapp.model.SensorResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SensorApiService {
    @GET("sensor/measure") // Endpoint de la API
    Call<MeasureResponse> getMeasureData();

    @GET("sensor")
    Call<SensorResponse> getSensorData();

    @POST("sensor/limits")
    Call<SensorDTO> createSensor(@Body SensorDTO sensor);

    @GET("sensor/measure/{sensorType}")
    Call<MeasureResponse> getMeasureData(@Path("sensorType") String sensorType);
}