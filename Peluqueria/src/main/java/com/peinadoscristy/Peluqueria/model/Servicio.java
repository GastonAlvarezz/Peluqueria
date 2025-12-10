package com.peinadoscristy.Peluqueria.model;

public enum Servicio {

    // ---- Peluquería clásica ----
    CORTE(30, false),        // 2 por horario
    TINTURA(60, false),      // 2 por horario
    MECHAS(90, true),        // exclusivo, bloquea 1h30

    // ---- Nuevos que agregamos ----
    PERMANENTE(90, false),   // similar a tintura en duración
    NUTRICION(45, false),    // hidratación
    AMPOLLITA(30, false);    // hidratación rápida

    private final int duracionMinutos;
    private final boolean exclusivo;

    Servicio(int duracionMinutos, boolean exclusivo) {
        this.duracionMinutos = duracionMinutos;
        this.exclusivo = exclusivo;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public boolean isExclusivo() {
        return exclusivo;
    }
}
