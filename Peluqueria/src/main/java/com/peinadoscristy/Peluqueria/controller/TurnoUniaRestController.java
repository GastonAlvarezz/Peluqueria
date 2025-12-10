package com.peinadoscristy.Peluqueria.controller;

import com.peinadoscristy.Peluqueria.service.TurnoUniaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/turnos-unias")
@RequiredArgsConstructor
public class TurnoUniaRestController {

    private final TurnoUniaService turnoUniaService;

    @GetMapping("/horarios")
    public List<String> obtenerHorarios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<LocalTime> disponibles = turnoUniaService.obtenerHorariosDisponibles(fecha);

        return disponibles.stream()
                .map(t -> t.toString()) 
                .toList();
    }
}
