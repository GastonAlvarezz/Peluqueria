package com.peinadoscristy.Peluqueria.controller;

import com.peinadoscristy.Peluqueria.model.*;
import com.peinadoscristy.Peluqueria.service.TurnoService;
import com.peinadoscristy.Peluqueria.util.TurnoSyncManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReservaController {

    private final TurnoService turnoService;
    private final TurnoSyncManager turnoSyncManager;

    // ========================
    // FORMULARIO DE RESERVA
    // ========================
    @GetMapping("/reservar")
    public String mostrarFormulario(Model model) {
        model.addAttribute("servicios", Servicio.values());
        model.addAttribute("longitudes", LongitudPelo.values());
        model.addAttribute("tipos", TipoPelo.values());
        return "reservar";
    }

    // ========================
    // PROCESAR RESERVA
    // ========================
    @PostMapping("/reservar")
    public String procesarReserva(
            @RequestParam String nombreCliente,
            @RequestParam String telefono,
            @RequestParam String fecha,
            @RequestParam String hora,

            // PERSONA 1 (OBLIGATORIA) 
            @RequestParam(name = "servicio1") Servicio servicio1,
            @RequestParam(name = "longitudPelo1") LongitudPelo longitudPelo1,
            @RequestParam(name = "tipoPelo1") TipoPelo tipoPelo1,
            @RequestParam(name = "incluyeCorte1", defaultValue = "false") boolean incluyeCorte1,

            // CANTIDAD TOTAL
            @RequestParam(name = "cantidadPersonas", defaultValue = "1") int cantidadPersonas,

            // PERSONA 2 (OPCIONAL)
            @RequestParam(name = "servicio2", required = false) Servicio servicio2,
            @RequestParam(name = "longitudPelo2", required = false) LongitudPelo longitudPelo2,
            @RequestParam(name = "tipoPelo2", required = false) TipoPelo tipoPelo2,
            @RequestParam(name = "incluyeCorte2", defaultValue = "false") boolean incluyeCorte2,

            // PERSONA 3 (OPCIONAL)
            @RequestParam(name = "servicio3", required = false) Servicio servicio3,
            @RequestParam(name = "longitudPelo3", required = false) LongitudPelo longitudPelo3,
            @RequestParam(name = "tipoPelo3", required = false) TipoPelo tipoPelo3,
            @RequestParam(name = "incluyeCorte3", defaultValue = "false") boolean incluyeCorte3,

            Model model) {

        LocalDate fechaSeleccionada = LocalDate.parse(fecha);
        LocalTime horaSeleccionada = LocalTime.parse(hora);

        try {
            // Adquirir lock para toda la operación de reserva
            if (!turnoSyncManager.adquirirPelequeriaLock()) {
                log.warn(" No se pudo completar la reserva (timeout en semáforo)");
                model.addAttribute("error", "Sistema saturado. Intenta de nuevo en unos segundos.");
                return "reservar";
            }

            try {
                log.info(" Procesando reserva para {} personas el {} a las {}", 
                    cantidadPersonas, fechaSeleccionada, horaSeleccionada);

                List<Turno> turnosGrupo = new ArrayList<>();
                BigDecimal totalGrupo = BigDecimal.ZERO;

                // =========================
                // PERSONA 1 (OBLIGATORIA)
                // =========================
                BigDecimal precio1 = turnoService.calcularPrecioEstimado(
                        servicio1, longitudPelo1, tipoPelo1, incluyeCorte1
                );

                Turno t1 = Turno.builder()
                        .nombreCliente(nombreCliente) 
                        .telefono(telefono)
                        .servicio(servicio1)
                        .longitudPelo(longitudPelo1)
                        .tipoPelo(tipoPelo1)
                        .incluyeCorte(incluyeCorte1)
                        .fecha(fechaSeleccionada)
                        .horaInicio(horaSeleccionada)
                        .precioEstimado(precio1)
                        .estado(EstadoTurno.PENDIENTE)
                        .build();

                turnosGrupo.add(t1);
                totalGrupo = totalGrupo.add(precio1);

                // =========================
                // PERSONA 2 (SI CORRESPONDE)
                // =========================
                if (cantidadPersonas >= 2 &&
                        servicio2 != null && longitudPelo2 != null && tipoPelo2 != null) {

                    BigDecimal precio2 = turnoService.calcularPrecioEstimado(
                            servicio2, longitudPelo2, tipoPelo2, incluyeCorte2
                    );

                    Turno t2 = Turno.builder()
                            .nombreCliente(nombreCliente + " - Persona 2")
                            .telefono(telefono)
                            .servicio(servicio2)
                            .longitudPelo(longitudPelo2)
                            .tipoPelo(tipoPelo2)
                            .incluyeCorte(incluyeCorte2)
                            .fecha(fechaSeleccionada)
                            .horaInicio(horaSeleccionada)
                            .precioEstimado(precio2)
                            .estado(EstadoTurno.PENDIENTE)
                            .build();

                    turnosGrupo.add(t2);
                    totalGrupo = totalGrupo.add(precio2);
                }

                // =========================
                // PERSONA 3 (SI CORRESPONDE)
                // =========================
                if (cantidadPersonas >= 3 &&
                        servicio3 != null && longitudPelo3 != null && tipoPelo3 != null) {

                    BigDecimal precio3 = turnoService.calcularPrecioEstimado(
                            servicio3, longitudPelo3, tipoPelo3, incluyeCorte3
                    );

                    Turno t3 = Turno.builder()
                            .nombreCliente(nombreCliente + " - Persona 3")
                            .telefono(telefono)
                            .servicio(servicio3)
                            .longitudPelo(longitudPelo3)
                            .tipoPelo(tipoPelo3)
                            .incluyeCorte(incluyeCorte3)
                            .fecha(fechaSeleccionada)
                            .horaInicio(horaSeleccionada)
                            .precioEstimado(precio3)
                            .estado(EstadoTurno.PENDIENTE)
                            .build();

                    turnosGrupo.add(t3);
                    totalGrupo = totalGrupo.add(precio3);
                }

                // =========================
                // GUARDAR TURNOS EN BD
                // =========================
                for (Turno t : turnosGrupo) {
                    turnoService.guardar(t);
                }

                log.info(" Reserva completada: {} turnos guardados", turnosGrupo.size());

                // =========================
                // ENVIAR A LA VISTA
                // =========================
                model.addAttribute("turnosGrupo", turnosGrupo);
                model.addAttribute("totalGrupo", totalGrupo);

                return "reserva-ok";
                
            } finally {
                turnoSyncManager.liberarPelequeriaLock();
                log.debug(" Semáforo liberado después de procesar reserva");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" Interrupción al procesar reserva", e);
            model.addAttribute("error", "Error al procesar la reserva. Intenta de nuevo.");
            return "reservar";
        }
    }
}
