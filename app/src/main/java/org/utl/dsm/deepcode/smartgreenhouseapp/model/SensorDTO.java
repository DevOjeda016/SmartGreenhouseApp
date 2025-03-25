package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class SensorDTO {
    private String tipoSensor;
    private Float limiteInferior;
    private Float limiteSuperior;

    public SensorDTO() {
    }

    public SensorDTO(String tipoSensor, Float limiteInferior, Float limiteSuperior) {
        this.tipoSensor = tipoSensor;
        this.limiteInferior = limiteInferior;
        this.limiteSuperior = limiteSuperior;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    public Float getLimiteInferior() {
        return limiteInferior;
    }

    public void setLimiteInferior(Float limiteInferior) {
        this.limiteInferior = limiteInferior;
    }

    public Float getLimiteSuperior() {
        return limiteSuperior;
    }

    public void setLimiteSuperior(Float limiteSuperior) {
        this.limiteSuperior = limiteSuperior;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sensor{");
        sb.append(", tipoSensor=").append(tipoSensor);
        sb.append(", limiteInferior=").append(limiteInferior);
        sb.append(", limiteSuperior=").append(limiteSuperior);
        sb.append('}');
        return sb.toString();
    }

}