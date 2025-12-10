package com.peinadoscristy.Peluqueria.controller;

import com.peinadoscristy.Peluqueria.model.Servicio;
import com.peinadoscristy.Peluqueria.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoRestController {

    private final TurnoService turnoService;

    @GetMapping("/horarios")
    public List<LocalTime> obtenerHorarios(
            @RequestParam String fecha,
            @RequestParam Servicio servicio,
            @RequestParam(name = "cantidadPersonas", defaultValue = "1") int cantidadPersonas
    ) {
        LocalDate fechaSeleccionada = LocalDate.parse(fecha);

  
        return turnoService.obtenerHorariosDisponiblesParaGrupo(
                fechaSeleccionada,
                servicio,
                cantidadPersonas
        );
    }
}
