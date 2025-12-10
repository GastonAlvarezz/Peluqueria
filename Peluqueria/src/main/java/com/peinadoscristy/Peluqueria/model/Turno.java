package com.peinadoscristy.Peluqueria.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "turno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos del cliente
    private String nombreCliente;
    private String telefono;

    // Fecha y hora
    private LocalDate fecha;
    private LocalTime horaInicio;

    @Enumerated(EnumType.STRING)
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    private EstadoTurno estado;

    @Enumerated(EnumType.STRING)
    private LongitudPelo longitudPelo;

    @Enumerated(EnumType.STRING)
    private TipoPelo tipoPelo;

    private boolean incluyeCorte;

    // Precio estimado que se mostró al cliente
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
