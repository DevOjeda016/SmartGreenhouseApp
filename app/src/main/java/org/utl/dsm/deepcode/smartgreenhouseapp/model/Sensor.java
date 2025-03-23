package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class Sensor {
    private int id;
    private String tipoSensor;
    private float limiteInferior;
    private float limiteSuperior;
    private Invernadero invernadero;

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    public float getLimiteInferior() {
        return limiteInferior;
    }

    public void setLimiteInferior(float limiteInferior) {
        this.limiteInferior = limiteInferior;
    }

    public float getLimiteSuperior() {
        return limiteSuperior;
    }

    public void setLimiteSuperior(float limiteSuperior) {
        this.limiteSuperior = limiteSuperior;
    }

    public Invernadero getInvernadero() {
        return invernadero;
    }

    public void setInvernadero(Invernadero invernadero) {
        this.invernadero = invernadero;
    }
}