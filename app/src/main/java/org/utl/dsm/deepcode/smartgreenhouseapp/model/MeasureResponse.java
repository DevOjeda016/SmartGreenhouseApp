package org.utl.dsm.deepcode.smartgreenhouseapp.model;

import java.util.List;

public class MeasureResponse {
    private int status;
    private String message;
    private List<MedicionDTO> data;

    // Getters y setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MedicionDTO> getData() {
        return data;
    }

    public void setMeasure(List<MedicionDTO> data) {
        this.data = data;
    }
}