package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class Sensor {
    private String tipoSensor;
    private int limiteInferior;
    private int limiteSuperior;

    // Constructor
    public Sensor(String tipoSensor, int limiteInferior, int limiteSuperior) {
        this.tipoSensor = tipoSensor;
        this.limiteInferior = limiteInferior;
        this.limiteSuperior = limiteSuperior;
    }

    // Getters y setters
    public String getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    public int getLimiteInferior() {
        return limiteInferior;
    }

    public void setLimiteInferior(int limiteInferior) {
        this.limiteInferior = limiteInferior;
    }

    public int getLimiteSuperior() {
        return limiteSuperior;
    }

    public void setLimiteSuperior(int limiteSuperior) {
        this.limiteSuperior = limiteSuperior;
    }
}