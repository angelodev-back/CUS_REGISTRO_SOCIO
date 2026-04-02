package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    @Autowired
    private SocioService socioService;

    @GetMapping("/aprobados")
    public ResponseEntity<?> listarSociosAprobados() {
        try {
            return ResponseEntity.ok(socioService.listarSociosAprobados());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generar-cuenta/{socioId}")
    public ResponseEntity<?> generarCuentaSocio(@PathVariable Integer socioId) {
        try {
            return ResponseEntity.ok(socioService.generarCuentaSocio(socioId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/consultar-publico/{numeroDocumento}")
    public ResponseEntity<?> consultarEstadoPublico(@PathVariable String numeroDocumento) {
        try {
            return ResponseEntity.ok(socioService.consultarEstadoPublico(numeroDocumento));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
