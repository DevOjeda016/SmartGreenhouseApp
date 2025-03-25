package org.utl.dsm.deepcode.smartgreenhouseapp.model;

import java.util.List;

public class MeasureResponse {
    private int status;
    private String message;
    private List<Measure> data;

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

    public List<Measure> getData() {
        return data;
    }

    public void setMeasure(List<Measure> data) {
        this.data = data;
    }
}