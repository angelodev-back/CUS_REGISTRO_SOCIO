package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.service.DeudaExternaService;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deudas")
public class DeudaExternaRestController {

    @Autowired
    private DeudaExternaService deudaExternaService;

    @Autowired
    private PostulanteService postulanteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/detalle-postulante/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Integer id) {
        try {
            PostulanteConDeudasDTO detalle = postulanteService.obtenerPostulanteConDeudasDetalle(id);
            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verificar/{id}")
    public ResponseEntity<?> verificarDeuda(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            Integer idJefe = obtenerIdUsuarioActual();
            String obs = body.getOrDefault("observaciones", "Verificado por Jefe");
            deudaExternaService.verificarDeuda(id, idJefe, obs);
            return ResponseEntity.ok(Map.of("mensaje", "Deuda verificada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Integer obtenerIdUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return 0;
        return usuarioRepository.findByUsername(auth.getName())
                .map(Usuario::getId)
                .orElse(0);
    }
}
