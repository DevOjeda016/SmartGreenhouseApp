package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class SignupResponse {
    private int status;
    private String message;
    private UsuarioData data;

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

    public UsuarioData getData() {
        return data;
    }

    public void setData(UsuarioData data) {
        this.data = data;
    }
}