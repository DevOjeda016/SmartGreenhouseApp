package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class UsuarioData {
    private int id;
    private String nombreUsuario;
    private String contrasenia;
    private Persona persona;
    private String rol;
    private Invernadero invernadero;

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Invernadero getInvernadero() {
        return invernadero;
    }

    public void setInvernadero(Invernadero invernadero) {
        this.invernadero = invernadero;
    }
}
