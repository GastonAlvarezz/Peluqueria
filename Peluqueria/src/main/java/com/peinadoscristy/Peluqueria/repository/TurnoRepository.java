package com.peinadoscristy.Peluqueria.repository;

import com.peinadoscristy.Peluqueria.model.EstadoTurno;
import com.peinadoscristy.Peluqueria.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByFecha(LocalDate fecha);

    List<Turno> findByFechaAndEstadoNot(LocalDate fecha, EstadoTurno estado);

    List<Turno> findByEstadoNot(EstadoTurno estado);
}
