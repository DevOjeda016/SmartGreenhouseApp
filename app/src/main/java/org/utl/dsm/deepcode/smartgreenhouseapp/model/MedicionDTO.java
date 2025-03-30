package org.utl.dsm.deepcode.smartgreenhouseapp.model;

public class MedicionDTO {
    private String tipoSensor;
    private Float valor;

    public MedicionDTO() {
    }


    public MedicionDTO(String tipoSensor, Float valor) {
        this.tipoSensor = tipoSensor;
        this.valor = valor;
    }

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MedicionDTO{");
        sb.append("tipoSensor=").append(tipoSensor);
        sb.append(", valor=").append(valor);
        sb.append('}');
        return sb.toString();
    }


}
