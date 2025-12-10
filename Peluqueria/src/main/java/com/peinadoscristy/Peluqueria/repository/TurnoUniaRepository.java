package com.peinadoscristy.Peluqueria.repository;

import com.peinadoscristy.Peluqueria.model.EstadoTurno;
import com.peinadoscristy.Peluqueria.model.TurnoUnia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TurnoUniaRepository extends JpaRepository<TurnoUnia, Long> {

    List<TurnoUnia> findByFecha(LocalDate fecha);

    List<TurnoUnia> findByFechaAndEstadoNot(LocalDate fecha, EstadoTurno estado);
}
