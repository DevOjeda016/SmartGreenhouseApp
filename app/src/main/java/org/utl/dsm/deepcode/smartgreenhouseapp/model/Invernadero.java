package org.utl.dsm.deepcode.smartgreenhouseapp.model;

import java.io.Serializable;

public class Invernadero implements Serializable {
    private int id;
    private String nombre;
    private String numSerie;
    private String modelo;

    // Constructor por defecto REQUERIDO
    public Invernadero() {
    }

    // Constructor con parámetros
    public Invernadero(String nombre, String numSerie, String modelo) {
        this.id = id;
        this.nombre = nombre;
        this.numSerie = numSerie;
        this.modelo = modelo;
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

    public String getNumSerie() {
        return numSerie;
    }

    public void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
}