package com.peinadoscristy.Peluqueria.service;

import com.peinadoscristy.Peluqueria.model.EstadoTurno;
import com.peinadoscristy.Peluqueria.model.ServicioUnia;
import com.peinadoscristy.Peluqueria.model.TurnoUnia;
import com.peinadoscristy.Peluqueria.repository.TurnoUniaRepository;
import com.peinadoscristy.Peluqueria.util.TurnoSyncManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnoUniaService {

    private final TurnoUniaRepository turnoUniaRepository;
    private final TurnoSyncManager turnoSyncManager;

    /**
     * Devuelve todos los horarios disponibles
     */
    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {
        try {
            if (!turnoSyncManager.adquirirUniasLock()) {
                log.warn(" Timeout al obtener horarios disponibles de uñas para {}", fecha);
                return List.of();
            }

            try {
                DayOfWeek dow = fecha.getDayOfWeek();
                if (dow == DayOfWeek.MONDAY || dow == DayOfWeek.SUNDAY) {
                    return List.of();
                }

                List<LocalTime> slots = generarSlots(fecha);

                List<TurnoUnia> turnosDelDia =
                        turnoUniaRepository.findByFechaAndEstadoNot(fecha, EstadoTurno.CANCELADO);

                List<LocalTime> disponibles = new ArrayList<>();

                for (LocalTime slot : slots) {
                    boolean ocupado = turnosDelDia.stream()
                            .anyMatch(t -> t.getHoraInicio() != null && t.getHoraInicio().equals(slot)
                                    && t.getEstado() != EstadoTurno.CANCELADO);

                    if (!ocupado) {
                        disponibles.add(slot);
                    }
                }
                log.debug(" Horarios de uñas disponibles para {}: {}", fecha, disponibles.size());
                return disponibles;
            } finally {
                turnoSyncManager.liberarUniasLock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupción en obtenerHorariosDisponibles", e);
            return List.of();
        }
    }

    /**
     * Slots de 90 minutos, con el mismo horario de trabajo que peluquería.
     */
    private List<LocalTime> generarSlots(LocalDate fecha) {
        List<LocalTime> result = new ArrayList<>();
        DayOfWeek dow = fecha.getDayOfWeek();

        if (dow == DayOfWeek.SATURDAY) {
            agregarTramo(result, LocalTime.of(8, 30), LocalTime.of(20, 0), 90);
        } else {
            agregarTramo(result, LocalTime.of(8, 30), LocalTime.of(12, 30), 90);
            agregarTramo(result, LocalTime.of(15, 0), LocalTime.of(20, 0), 90);
        }

        return result;
    }

    private void agregarTramo(List<LocalTime> lista, LocalTime inicio, LocalTime fin, int minutosPaso) {
        LocalTime actual = inicio;
        while (!actual.isAfter(fin)) {
            lista.add(actual);
            actual = actual.plusMinutes(minutosPaso);
        }
    }

    // ===== CRUD =====

    public TurnoUnia guardar(TurnoUnia turno) {
        try {
            if (!turnoSyncManager.adquirirSlotLock(turno.getFecha(), turno.getHoraInicio())) {
                throw new RuntimeException("No se pudo adquirir lock para guardar turno de uñas");
            }

            try {
                if (turno.getEstado() == null) {
                    turno.setEstado(EstadoTurno.PENDIENTE);
                }
                TurnoUnia guardado = turnoUniaRepository.save(turno);
                log.info(" Turno de uñas guardado: {} - {} a las {}", 
                    turno.getNombreCliente(), turno.getFecha(), turno.getHoraInicio());
                return guardado;
            } finally {
                turnoSyncManager.liberarSlotLock(turno.getFecha(), turno.getHoraInicio());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" Interrupción en guardar turno de uñas", e);
            throw new RuntimeException("Interrupción al guardar turno de uñas", e);
        }
    }

    public BigDecimal calcularPrecio(ServicioUnia servicio) {
        if (servicio == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(servicio.getPrecioBase());
    }
}
