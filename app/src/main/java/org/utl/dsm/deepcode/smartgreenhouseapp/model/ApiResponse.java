package org.utl.dsm.deepcode.smartgreenhouseapp.model;

import java.util.List;

public class ApiResponse {
    private int status;
    private String message;
    private List<UsuarioData> data;

    public ApiResponse(int status, String message, List<UsuarioData> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<UsuarioData> getData() {
        return data;
    }
}
