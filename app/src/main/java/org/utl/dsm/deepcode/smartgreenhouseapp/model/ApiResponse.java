package org.utl.dsm.deepcode.smartgreenhouseapp.model;

import java.util.List;

public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;  // Genérico para manejar tanto objetos como arrays

    // Getters y setters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
