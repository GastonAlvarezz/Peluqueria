package com.peinadoscristy.Peluqueria.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.*;


@Slf4j
@Component
public class TurnoSyncManager {

    // Máximo 2 threads simultáneos accediendo a reservas de peluquería
    private final Semaphore pelequeriaReservaSemaphore = new Semaphore(2);

    // Máximo 1 thread simultáneo accediendo a reservas de uñas
    private final Semaphore uniasReservaSemaphore = new Semaphore(1);

    private final ConcurrentHashMap<String, Semaphore> slotSemaphores = new ConcurrentHashMap<>();

    private static final long TIMEOUT_SECONDS = 5;


    public boolean adquirirPelequeriaLock() throws InterruptedException {
        boolean adquirido = pelequeriaReservaSemaphore.tryAcquire(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (adquirido) {
            log.debug("✓ Semáforo de peluquería adquirido (permisos disponibles: {})", 
                pelequeriaReservaSemaphore.availablePermits());
        } else {
            log.warn("✗ No se pudo adquirir semáforo de peluquería después de {} segundos", TIMEOUT_SECONDS);
        }
        return adquirido;
    }

    /**
     * Libera el semáforo de peluquería.
     */
    public void liberarPelequeriaLock() {
        pelequeriaReservaSemaphore.release();
        log.debug("✓ Semáforo de peluquería liberado (permisos disponibles: {})", 
            pelequeriaReservaSemaphore.availablePermits());
    }

    /*
      Adquiere un semáforo para reservar un turno de uñas.
     */
    public boolean adquirirUniasLock() throws InterruptedException {
        boolean adquirido = uniasReservaSemaphore.tryAcquire(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (adquirido) {
            log.debug("✓ Semáforo de uñas adquirido (permisos disponibles: {})", 
                uniasReservaSemaphore.availablePermits());
        } else {
            log.warn("✗ No se pudo adquirir semáforo de uñas después de {} segundos", TIMEOUT_SECONDS);
        }
        return adquirido;
    }

    /*
    Libera el semáforo de uñas.
     */
    public void liberarUniasLock() {
        uniasReservaSemaphore.release();
        log.debug("✓ Semáforo de uñas liberado (permisos disponibles: {})", 
            uniasReservaSemaphore.availablePermits());
    }

    public Semaphore obtenerSemáforoSlot(LocalDate fecha, LocalTime hora) {
        String key = fecha + "|" + hora;
        return slotSemaphores.computeIfAbsent(key, k -> new Semaphore(1));
    }

    /**
     * Intenta adquirir el semáforo de un slot específico.
     */
    public boolean adquirirSlotLock(LocalDate fecha, LocalTime hora) throws InterruptedException {
        Semaphore semaphore = obtenerSemáforoSlot(fecha, hora);
        boolean adquirido = semaphore.tryAcquire(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (adquirido) {
            log.debug("✓ Semáforo de slot adquirido: {} {} (permisos: {})", 
                fecha, hora, semaphore.availablePermits());
        }
        return adquirido;
    }

    /**
     * Libera el semáforo de un slot específico.
     */
    public void liberarSlotLock(LocalDate fecha, LocalTime hora) {
        Semaphore semaphore = slotSemaphores.get(fecha + "|" + hora);
        if (semaphore != null) {
            semaphore.release();
            log.debug("✓ Semáforo de slot liberado: {} {}", fecha, hora);
        }
    }

    public void limpiarSemáforosAntiguos(LocalDate fechaActual) {
        slotSemaphores.entrySet().removeIf(entry -> {
            try {
                String[] parts = entry.getKey().split("\\|");
                LocalDate fecha = LocalDate.parse(parts[0]);
                return fecha.isBefore(fechaActual);
            } catch (Exception e) {
                return false;
            }
        });
        log.info("Semáforos de slots antiguos limpiados");
    }

    /**
     * Retorna el estado actual del semáforo de peluquería 
     */
    public int obtenerPermisosDisponiblesPeluqueria() {
        return pelequeriaReservaSemaphore.availablePermits();
    }

    /**
     * Retorna el estado actual del semáforo de uñas 
     */
    public int obtenerPermisosDisponiblesUnias() {
        return uniasReservaSemaphore.availablePermits();
    }
}
