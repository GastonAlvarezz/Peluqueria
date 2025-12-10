    package com.peinadoscristy.Peluqueria.controller;

    import com.peinadoscristy.Peluqueria.service.TurnoService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;

    @Controller
    @RequestMapping("/admin/turnos")
    @RequiredArgsConstructor
    public class AdminTurnoController {

        private final TurnoService turnoService;

        // =======================
        //  TURNOS ACTIVOS (PELU)
        // =======================
        @GetMapping
        public String listarActivos(Model model) {
            model.addAttribute("vista", "activos");
            model.addAttribute("turnos", turnoService.listarActivos());
            return "admin-turnos";
        }

        // =======================
        //  TURNOS CANCELADOS
        // =======================
        @GetMapping("/cancelados")
        public String listarCancelados(Model model) {
            model.addAttribute("vista", "cancelados");
            model.addAttribute("turnos", turnoService.listarCancelados());
            return "admin-turnos";
        }

        // =======================
        //  TURNOS UÑAS (MimoNails)
        // =======================
        @GetMapping("/unias")
        public String listarUnia(Model model) {
            model.addAttribute("vista", "unias");
            model.addAttribute("titulo", "Turnos de Uñas");
            model.addAttribute("turnos", turnoService.listarTurnosUnia());
            return "admin-turnos";
        }

        // =======================
        //      ACCIONES PELU
        // =======================
        @PostMapping("/{id}/cancelar")
        public String cancelar(@PathVariable Long id) {
            turnoService.cancelarTurno(id);
            return "redirect:/admin/turnos";
        }

        @PostMapping("/{id}/eliminar")
        public String eliminar(@PathVariable Long id) {
            turnoService.eliminarTurno(id);
            return "redirect:/admin/turnos";
        }

        // =======================
        //      ACCIONES UÑAS
        // =======================
        @PostMapping("/unias/{id}/cancelar")
        public String cancelarUnia(@PathVariable Long id) {
            turnoService.cancelarTurnoUnia(id);
            return "redirect:/admin/turnos/unias";
        }

        @PostMapping("/unias/{id}/eliminar")
        public String eliminarUnia(@PathVariable Long id) {
            turnoService.eliminarTurnoUnia(id);
            return "redirect:/admin/turnos/unias";
        }
    }
