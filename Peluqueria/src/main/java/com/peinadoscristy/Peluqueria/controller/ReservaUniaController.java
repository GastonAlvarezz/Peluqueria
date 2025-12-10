package com.peinadoscristy.Peluqueria.controller;

import com.peinadoscristy.Peluqueria.model.EstadoTurno;
import com.peinadoscristy.Peluqueria.model.ServicioUnia;
import com.peinadoscristy.Peluqueria.model.TurnoUnia;
import com.peinadoscristy.Peluqueria.service.TurnoUniaService;
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
public class ReservaUniaController {

    private final TurnoUniaService turnoUniaService;
    private final TurnoSyncManager turnoSyncManager;

    // FORMULARIO
    @GetMapping("/reservar-unias")
    public String mostrarFormularioUnas(Model model) {
        model.addAttribute("serviciosUnia", ServicioUnia.values());
        return "reservar-unias";
    }

    // PROCESAR ENVÍO
    @PostMapping("/reservar-unias")
    public String procesarReservaUnas(
            @RequestParam String nombreCliente,
            @RequestParam String telefono,
            @RequestParam String fecha,

            @RequestParam(name = "cantidadPersonas", defaultValue = "1") int cantidadPersonas,

            // Persona 1 (obligatoria)
            @RequestParam ServicioUnia servicio1,
            @RequestParam String hora1,

            // Persona 2
            @RequestParam(name = "servicio2", required = false) ServicioUnia servicio2,
            @RequestParam(name = "hora2", required = false) String hora2,

            // Persona 3
            @RequestParam(name = "servicio3", required = false) ServicioUnia servicio3,
            @RequestParam(name = "hora3", required = false) String hora3,

            Model model) {

        LocalDate fechaSeleccionada = LocalDate.parse(fecha);

        try {
            // Adquirir lock para toda la operación de reserva de uñas
            if (!turnoSyncManager.adquirirUniasLock()) {
                log.warn(" No se pudo completar la reserva de uñas (timeout en semáforo)");
                model.addAttribute("error", "Sistema saturado. Intenta de nuevo en unos segundos.");
                return "reservar-unias";
            }

            try {
                log.info(" Procesando reserva de uñas para {} personas el {}", 
                    cantidadPersonas, fechaSeleccionada);

                List<TurnoUnia> turnosGrupo = new ArrayList<>();
                BigDecimal totalGrupo = BigDecimal.ZERO;

                // PERSONA 1
                LocalTime h1 = LocalTime.parse(hora1);
                BigDecimal p1 = turnoUniaService.calcularPrecio(servicio1);

                TurnoUnia t1 = TurnoUnia.builder()
                        .nombreCliente(nombreCliente)
                        .telefono(telefono)
                        .fecha(fechaSeleccionada)
                        .horaInicio(h1)
                        .servicio(servicio1)
                        .precioEstimado(p1)
                        .estado(EstadoTurno.PENDIENTE)
                        .build();

                turnosGrupo.add(t1);
                totalGrupo = totalGrupo.add(p1);

                // PERSONA 2 (si corresponde)
                if (cantidadPersonas >= 2 && servicio2 != null && hora2 != null && !hora2.isBlank()) {
                    LocalTime h2 = LocalTime.parse(hora2);
                    BigDecimal p2 = turnoUniaService.calcularPrecio(servicio2);

                    TurnoUnia t2 = TurnoUnia.builder()
                            .nombreCliente(nombreCliente + " - Persona 2")
                            .telefono(telefono)
                            .fecha(fechaSeleccionada)
                            .horaInicio(h2)
                            .servicio(servicio2)
                            .precioEstimado(p2)
                            .estado(EstadoTurno.PENDIENTE)
                            .build();

                    turnosGrupo.add(t2);
                    totalGrupo = totalGrupo.add(p2);
                }

                // PERSONA 3 (si corresponde)
                if (cantidadPersonas >= 3 && servicio3 != null && hora3 != null && !hora3.isBlank()) {
                    LocalTime h3 = LocalTime.parse(hora3);
                    BigDecimal p3 = turnoUniaService.calcularPrecio(servicio3);

                    TurnoUnia t3 = TurnoUnia.builder()
                            .nombreCliente(nombreCliente + " - Persona 3")
                            .telefono(telefono)
                            .fecha(fechaSeleccionada)
                            .horaInicio(h3)
                            .servicio(servicio3)
                            .precioEstimado(p3)
                            .estado(EstadoTurno.PENDIENTE)
                            .build();

                    turnosGrupo.add(t3);
                    totalGrupo = totalGrupo.add(p3);
                }

                // Guardar todos 
                for (TurnoUnia t : turnosGrupo) {
                    turnoUniaService.guardar(t);
                }

                log.info(" Reserva de uñas completada: {} turnos guardados", turnosGrupo.size());

                model.addAttribute("turnosGrupo", turnosGrupo);
                model.addAttribute("totalGrupo", totalGrupo);

                return "reserva-unias-ok";
            } finally {
                turnoSyncManager.liberarUniasLock();
                log.debug(" Semáforo de uñas liberado después de procesar reserva");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" Interrupción al procesar reserva de uñas", e);
            model.addAttribute("error", "Error al procesar la reserva. Intenta de nuevo.");
            return "reservar-unias";
        }
    }
}
