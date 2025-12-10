package com.peinadoscristy.Peluqueria.service;

import com.peinadoscristy.Peluqueria.model.*;
import com.peinadoscristy.Peluqueria.repository.TurnoRepository;
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
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final TurnoUniaRepository turnoUniaRepository;
    private final TurnoSyncManager turnoSyncManager;

    // =========================================================
    //                     PELUQUERÍA
    // =========================================================


    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha, Servicio servicio) {
        try {
            if (!turnoSyncManager.adquirirPelequeriaLock()) {
                log.warn(" Timeout al intentar obtener horarios disponibles para {}", fecha);
                return List.of();
            }

            try {
                DayOfWeek dow = fecha.getDayOfWeek();
                if (dow == DayOfWeek.MONDAY || dow == DayOfWeek.SUNDAY) return List.of();

                List<LocalTime> slots = generarSlotsPeluqueria(fecha);
                List<Turno> turnosDia =
                        turnoRepository.findByFechaAndEstadoNot(fecha, EstadoTurno.CANCELADO);

                List<LocalTime> disponibles = new ArrayList<>();
                for (LocalTime hora : slots) {
                    if (esHorarioDisponiblePeluqueria(hora, fecha, servicio, turnosDia)) {
                        disponibles.add(hora);
                    }
                }
                log.debug(" Horarios disponibles para {} ({} servicios): {}", fecha, servicio, disponibles.size());
                return disponibles;
            } finally {
                turnoSyncManager.liberarPelequeriaLock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" Interrupción en obtenerHorariosDisponibles", e);
            return List.of();
        }
    }

    /**
     * Lógica para grupos de peluquería (máx 2 por horario).
     */
    public List<LocalTime> obtenerHorariosDisponiblesParaGrupo(
            LocalDate fecha,
            Servicio servicioPersona1,
            int cantidadPersonas
    ) {
        try {
            if (!turnoSyncManager.adquirirPelequeriaLock()) {
                log.warn(" Timeout al intentar obtener horarios para grupo en {}", fecha);
                return List.of();
            }

            try {
                DayOfWeek dow = fecha.getDayOfWeek();
                if (dow == DayOfWeek.MONDAY || dow == DayOfWeek.SUNDAY) return List.of();

                List<LocalTime> slots = generarSlotsPeluqueria(fecha);
                List<Turno> turnosDia =
                        turnoRepository.findByFechaAndEstadoNot(fecha, EstadoTurno.CANCELADO);

                List<LocalTime> disponibles = new ArrayList<>();
                for (LocalTime hora : slots) {
                    if (esHorarioDisponibleParaGrupo(hora, fecha, servicioPersona1, cantidadPersonas, turnosDia)) {
                        disponibles.add(hora);
                    }
                }
                log.debug(" Horarios disponibles para grupo de {} personas: {}", cantidadPersonas, disponibles.size());
                return disponibles;
            } finally {
                turnoSyncManager.liberarPelequeriaLock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Interrupción en obtenerHorariosDisponiblesParaGrupo", e);
            return List.of();
        }
    }


    // =========================================================
    //                     GENERADOR DE SLOTS
    // =========================================================

    private List<LocalTime> generarSlotsPeluqueria(LocalDate fecha) {
        List<LocalTime> lista = new ArrayList<>();
        DayOfWeek dow = fecha.getDayOfWeek();

        if (dow == DayOfWeek.SATURDAY) {
            agregarTramo(lista, LocalTime.of(8,30), LocalTime.of(20,0), 30);
        } else {
            agregarTramo(lista, LocalTime.of(8,30), LocalTime.of(12,30), 30);
            agregarTramo(lista, LocalTime.of(15,0), LocalTime.of(20,0), 30);
        }
        return lista;
    }

    private void agregarTramo(List<LocalTime> lista, LocalTime inicio, LocalTime fin, int paso) {
        LocalTime actual = inicio;
        while (!actual.isAfter(fin)) {
            lista.add(actual);
            actual = actual.plusMinutes(paso);
        }
    }


    // =========================================================
    //                    LÓGICA DE PELUQUERÍA
    // =========================================================

    private boolean esHorarioDisponiblePeluqueria(
            LocalTime hora,
            LocalDate fecha,
            Servicio servicio,
            List<Turno> turnosDia
    ) {

        LocalDateTime iniNuevo = LocalDateTime.of(fecha, hora);
        LocalDateTime finNuevo = iniNuevo.plusMinutes(servicio.getDuracionMinutos());

        // Exclusividad MECHAS
        for (Turno t : turnosDia) {
            if (t.getEstado() == EstadoTurno.CANCELADO) continue;
            if (t.getServicio() == null || t.getHoraInicio() == null) continue;

            LocalDateTime iniEx = LocalDateTime.of(fecha, t.getHoraInicio());
            LocalDateTime finEx = iniEx.plusMinutes(t.getServicio().getDuracionMinutos());

            boolean solapa = !finEx.isBefore(iniNuevo) && !iniEx.isAfter(finNuevo);

            if (!solapa) continue;

            if (t.getServicio() == Servicio.MECHAS || servicio == Servicio.MECHAS) {
                return false;
            }
        }

        // Capacidad global máximo 2 personas por horario
        long ocupados = turnosDia.stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .filter(t -> hora.equals(t.getHoraInicio()))
                .count();

        return ocupados < 2;
    }

    private boolean esHorarioDisponibleParaGrupo(
            LocalTime hora,
            LocalDate fecha,
            Servicio servPersona1,
            int cantidadPersonas,
            List<Turno> turnosDia
    ) {

        // Primero revisar MECHAS igual que antes
        LocalDateTime iniNuevo = LocalDateTime.of(fecha, hora);
        LocalDateTime finNuevo = iniNuevo.plusMinutes(servPersona1.getDuracionMinutos());

        for (Turno t : turnosDia) {
            if (t.getEstado() == EstadoTurno.CANCELADO) continue;

            LocalDateTime iniEx = LocalDateTime.of(fecha, t.getHoraInicio());
            LocalDateTime finEx = iniEx.plusMinutes(t.getServicio().getDuracionMinutos());

            boolean solapa = !finEx.isBefore(iniNuevo) && !iniEx.isAfter(finNuevo);

            if (!solapa) continue;

            if (t.getServicio() == Servicio.MECHAS || servPersona1 == Servicio.MECHAS) {
                return false;
            }
        }

        long ocupados = turnosDia.stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .filter(t -> t.getHoraInicio().equals(hora))
                .count();

        return ocupados + cantidadPersonas <= 2;
    }


    // =========================================================
    //                CRUD + ADMIN (Peluquería)
    // =========================================================

    public Turno guardar(Turno turno) {
        try {
            // Adquirir lock específico del slot
            if (!turnoSyncManager.adquirirSlotLock(turno.getFecha(), turno.getHoraInicio())) {
                throw new RuntimeException("No se pudo adquirir lock para guardar turno");
            }

            try {
                if (turno.getEstado() == null) {
                    turno.setEstado(EstadoTurno.CONFIRMADO);
                }
                Turno guardado = turnoRepository.save(turno);
                log.info(" Turno guardado: {} - {} a las {}", 
                    turno.getNombreCliente(), turno.getFecha(), turno.getHoraInicio());
                return guardado;
            } finally {
                turnoSyncManager.liberarSlotLock(turno.getFecha(), turno.getHoraInicio());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" Interrupción en guardar turno", e);
            throw new RuntimeException("Interrupción al guardar turno", e);
        }
    }

    public List<Turno> listarActivos() {
        return turnoRepository.findByEstadoNot(EstadoTurno.CANCELADO);
    }

    public List<Turno> listarCancelados() {
        return turnoRepository.findByEstadoNot(EstadoTurno.CONFIRMADO)
                .stream()
                .filter(t -> t.getEstado() == EstadoTurno.CANCELADO)
                .toList();
    }

    public void cancelarTurno(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID no puede ser nulo");
        }
        Turno t = turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
        t.setEstado(EstadoTurno.CANCELADO);
        turnoRepository.save(t);
    }

    public void eliminarTurno(Long id) {
        if (id != null) {
            turnoRepository.deleteById(id);
        }
    }


    // =========================================================
    //                      PRECIOS PELUQUERÍA
    // =========================================================

    public BigDecimal calcularPrecioEstimado(
            Servicio servicio,
            LongitudPelo longitud,
            TipoPelo tipo
    ) {
        double base = switch (servicio) {
            case CORTE -> 8000;
            case TINTURA -> 25000;
            case MECHAS -> 65000;
            default -> 0;
        };

        double extraLong = switch (longitud) {
            case CORTO -> 0;
            case MEDIO -> 20000;
            case LARGO -> 40000;
        };

        double extraTipo = switch (tipo) {
            case LACIO -> 0;
            case ONDULADO -> 10000;
            case RIZADO -> 15000;
            case MUY_ABUNDANTE -> 20000;
        };

        return BigDecimal.valueOf(base + extraLong + extraTipo);
    }

    public BigDecimal calcularPrecioEstimado(
            Servicio servicio,
            LongitudPelo longitud,
            TipoPelo tipo,
            boolean incluyeCorte
    ) {
        BigDecimal precio = calcularPrecioEstimado(servicio, longitud, tipo);

        if (incluyeCorte && servicio != Servicio.CORTE) {
            precio = precio.add(BigDecimal.valueOf(8000));
        }
        return precio;
    }


    // =========================================================
    //                      UÑAS (MimoNails)
    // =========================================================

    /**
     * Turnos de uñas solo 1 persona por horario, duración fija 1h30.
     */
    public List<LocalTime> obtenerHorariosUnia(LocalDate fecha) {
        try {
            if (!turnoSyncManager.adquirirUniasLock()) {
                log.warn(" Timeout al intentar obtener horarios de uñas para {}", fecha);
                return List.of();
            }

            try {
                List<TurnoUnia> turnos = turnoUniaRepository.findByFechaAndEstadoNot(fecha, EstadoTurno.CANCELADO);

                List<LocalTime> disponibles = new ArrayList<>();
                LocalTime inicio = LocalTime.of(8,30);
                LocalTime fin = LocalTime.of(20,0);

                while (!inicio.isAfter(fin.minusMinutes(90))) {

                    LocalDateTime iniNuevo = LocalDateTime.of(fecha, inicio);
                    LocalDateTime finNuevo = iniNuevo.plusMinutes(90);

                    boolean ocupado = false;

                    for (TurnoUnia t : turnos) {
                        LocalDateTime iniEx = LocalDateTime.of(fecha, t.getHoraInicio());
                        LocalDateTime finEx = iniEx.plusMinutes(90);

                        boolean solapa = !finEx.isBefore(iniNuevo) && !iniEx.isAfter(finNuevo);

                        if (solapa) {
                            ocupado = true;
                            break;
                        }
                    }

                    if (!ocupado) disponibles.add(inicio);

                    inicio = inicio.plusMinutes(90);
                }
                log.debug(" Horarios disponibles de uñas para {}: {}", fecha, disponibles.size());
                return disponibles;
            } finally {
                turnoSyncManager.liberarUniasLock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" Interrupción en obtenerHorariosUnia", e);
            return List.of();
        }
    }


    // Para pantalla admin
    public List<TurnoUnia> listarTurnosUnia() {
        return turnoUniaRepository.findAll();
    }

    public void cancelarTurnoUnia(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID no puede ser nulo");
        }
        TurnoUnia t = turnoUniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
        t.setEstado(EstadoTurno.CANCELADO);
        turnoUniaRepository.save(t);
    }

    public void eliminarTurnoUnia(Long id) {
        if (id != null) {
            turnoUniaRepository.deleteById(id);
        }
    }

}
