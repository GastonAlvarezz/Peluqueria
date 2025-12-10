package com.peinadoscristy.Peluqueria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "turno_unia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoUnia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCliente;
    private String telefono;

    private LocalDate fecha;
    private LocalTime horaInicio;

    @Enumerated(EnumType.STRING)
    private ServicioUnia servicio;

    @Enumerated(EnumType.STRING)
    private EstadoTurno estado; 

    private BigDecimal precioEstimado;

    @Transient
    public LocalDateTime getFechaHoraInicio() {
        if (fecha == null || horaInicio == null) return null;
        return LocalDateTime.of(fecha, horaInicio);
    }

    @Transient
    public LocalDateTime getFechaHoraFin() {
        if (fecha == null || horaInicio == null || servicio == null) return null;
        return LocalDateTime.of(fecha, horaInicio)
                .plusMinutes(servicio.getDuracionMinutos());
    }
}
