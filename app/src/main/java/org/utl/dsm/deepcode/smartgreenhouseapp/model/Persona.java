package org.utl.dsm.deepcode.smartgreenhouseapp.model;

import java.io.Serializable;

public class Persona implements Serializable {

    private int id;  // Añadido el campo id que faltaba
    private String nombre;
    private String aPaterno;
    private String aMaterno;

    // Constructor completo
    public Persona(int id, String nombre, String aPaterno, String aMaterno) {
        this.id = id;
        this.nombre = nombre;
        this.aPaterno = aPaterno;
        this.aMaterno = aMaterno;
    }

    // Constructor sin ID
    public Persona(String nombre, String aPaterno, String aMaterno) {
        this.nombre = nombre;
        this.aPaterno = aPaterno;
        this.aMaterno = aMaterno;
    }

    // Constructor vacío requerido para Serializable
    public Persona() {
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAPaterno() {
        return aPaterno;
    }

    public void setAPaterno(String aPaterno) {
        this.aPaterno = aPaterno;
    }

    public String getAMaterno() {
        return aMaterno;
    }

    public void setAMaterno(String aMaterno) {
        this.aMaterno = aMaterno;
    }
}