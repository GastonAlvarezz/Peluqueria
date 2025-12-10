package com.peinadoscristy.Peluqueria.model;

import lombok.Getter;

@Getter
public enum ServicioUnia {

    SEMI_PERMANENTE("Semi permanente", 90, 14000),
    SOFT_GEL("Soft Gel", 90, 18000),
    CAPPING("Capping", 90, 16000),
    ESCULPIDA("Esculpida", 90, 19000),
    ESMALTADO("Manicuria Rusa", 90, 8000),
    RETIRADO("Retirado", 90, 10000);

    private final String descripcion;
    private final int duracionMinutos;
    private final double precioBase;

    ServicioUnia(String descripcion, int duracionMinutos, double precioBase) {
        this.descripcion = descripcion;
        this.duracionMinutos = duracionMinutos;
        this.precioBase = precioBase;
    }

    @Override
    public String toString() {
        // para que en logs salga más lindo, pero en Thymeleaf
        // igual vamos a usar getDescripcion()
        return descripcion;
    }
}
