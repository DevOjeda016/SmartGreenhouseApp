package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class UsuarioDTO {
    private int id;
    private String nombre;
    private String aPaterno;
    private int idRol;

    public UsuarioDTO() {
    }

    public UsuarioDTO(int id, String nombre, String aPaterno, int idRol) {
        this.id = id;
        this.nombre = nombre;
        this.aPaterno = aPaterno;
        this.idRol = idRol;
    }

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

    public String getaPaterno() {
        return aPaterno;
    }

    public void setaPaterno(String aPaterno) {
        this.aPaterno = aPaterno;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    @Override
    public String toString() {
        return "UsuarioDTO{" + "id=" + id + ", nombre=" + nombre + ", aPaterno=" + aPaterno + ", idRol=" + idRol + '}';
    }
}

