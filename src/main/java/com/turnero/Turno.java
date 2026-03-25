package com.turnero;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Turno {
    private Long id;
    private String numero;
    private Integer puesto;
    private Integer cantidadLlamadas;

    // Getters y setters

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPuesto() {
        return puesto;
    }

    public void setPuesto(Integer puesto) {
        this.puesto = puesto;
    }

    public Integer getCantidadLlamadas() {
        return cantidadLlamadas;
    }

    public void setCantidadLlamadas(Integer cantidadLlamadas) {
        this.cantidadLlamadas = cantidadLlamadas;
    }
}
