package org.utl.dsm.deepcode.smartgreenhouseapp.model;

import java.util.List;

public class SensorResponse {
    private int status;
    private String message;
    private List<SensorDTO> data;

    public SensorResponse() {
    }

    public SensorResponse(int status, String message, List<SensorDTO> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getEstatus() {
        return status;
    }

    public void setEstatus(int status) {
        this.status = status;
    }

    public String getmessage() {
        return message;
    }

    public void setmessage(String message) {
        this.message = message;
    }

    public List<SensorDTO> getData() {
        return data;
    }

    public void setData(List<SensorDTO> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SensorDTOResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
