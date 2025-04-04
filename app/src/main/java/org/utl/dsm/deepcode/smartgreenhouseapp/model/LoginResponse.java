package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class LoginResponse {
    private int status;
    private String message;
    private UsuarioDTO data;

    // Getters y setters
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UsuarioDTO getData() {
        return data;
    }
}