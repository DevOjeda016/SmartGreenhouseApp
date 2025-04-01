package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class LoginResponse {
    private int status;
    private String message;

    private UsuarioDTO data;

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
    public UsuarioDTO getdata() {
        return data;
    }
    public void setdata(UsuarioDTO data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
