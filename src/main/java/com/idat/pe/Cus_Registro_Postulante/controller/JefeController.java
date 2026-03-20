package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.genericResponse.GenericResponse;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.service.DeudaExternaService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador REST para operaciones del JEFE de Atención al Cliente
 * Endpoint base: /api/jefe
 * 
 * Aplica SOLID:
 * - Single Responsibility: solo maneja lógica de JEFE
 * - Separation of Concerns: delegación a servicios
 * - Dependency Inversion: inyección de interfaces (PostulanteService, DeudaExternaService)
 */
@RestController
@RequestMapping("/api/jefe")
public class JefeController {
    
    private static final Logger logger = LoggerFactory.getLogger(JefeController.class);
    
    @Autowired
    private PostulanteService postulanteService;
    
    @Autowired
    private DeudaExternaService deudaExternaService;
    
    /**
     * GET /api/jefe/postulantes
     * Obtiene lista de postulantes pendientes/subsanados con sus deudas
     */
    @GetMapping("/postulantes")
    public ResponseEntity<GenericResponse<String, List<PostulanteConDeudasDTO>>> listarPostulantes() {
        try {
            logger.info("JEFE solicitando lista de postulantes pendientes");
            List<PostulanteConDeudasDTO> postulantes = postulanteService.obtenerPostulantesPendientesConDeudas();
            
            return ResponseEntity.ok(GenericResponse.<String, List<PostulanteConDeudasDTO>>builder()
                    .message("Postulantes obtenidos exitosamente")
                    .body(postulantes)
                    .statusCode("200")
                    .build());
                    
        } catch (Exception e) {
            logger.error("Error al listar postulantes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.<String, List<PostulanteConDeudasDTO>>builder()
                            .message("Error: " + e.getMessage())
                            .statusCode("500")
                            .body(null)
                            .build());
        }
    }
    
    /**
     * GET /api/jefe/postulantes/{idPostulante}
     * Obtiene detalles de un postulante específico con sus deudas
     */
    @GetMapping("/postulantes/{idPostulante}")
    public ResponseEntity<GenericResponse<String, PostulanteConDeudasDTO>> obtenerPostulante(
            @PathVariable Integer idPostulante) {
        try {
            logger.info("JEFE solicitando detalles de postulante ID: {}", idPostulante);
            PostulanteConDeudasDTO postulante = postulanteService.obtenerPostulanteConDeudasDetalle(idPostulante);
            
            return ResponseEntity.ok(GenericResponse.<String, PostulanteConDeudasDTO>builder()
                    .message("Postulante obtenido exitosamente")
                    .body(postulante)
                    .statusCode("200")
                    .build());
                    
        } catch (Exception e) {
            logger.error("Error al obtener postulante: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.<String, PostulanteConDeudasDTO>builder()
                            .message("Error: " + e.getMessage())
                            .statusCode("404")
                            .body(null)
                            .build());
        }
    }
    
    /**
     * POST /api/jefe/postulantes/{idPostulante}/aprobar
     * Aprueba un postulante
     */
    @PostMapping("/postulantes/{idPostulante}/aprobar")
    public ResponseEntity<GenericResponse<String, String>> aprobarPostulante(
            @PathVariable Integer idPostulante) {
        try {
            Integer idJefe = obtenerIdJefeActual();
            logger.info("JEFE {} aprobando postulante ID: {}", idJefe, idPostulante);
            
            postulanteService.aprobarPostulante(idPostulante, idJefe);
            
            return ResponseEntity.ok(GenericResponse.<String, String>builder()
                    .message("Postulante aprobado exitosamente")
                    .body("ID: " + idPostulante)
                    .statusCode("200")
                    .build());
                    
        } catch (Exception e) {
            logger.error("Error al aprobar postulante: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.<String, String>builder()
                            .message("Error: " + e.getMessage())
                            .statusCode("500")
                            .body(null)
                            .build());
        }
    }
    
    /**
     * POST /api/jefe/postulantes/{idPostulante}/rechazar
     * Rechaza un postulante con motivo
     */
    @PostMapping("/postulantes/{idPostulante}/rechazar")
    public ResponseEntity<GenericResponse<String, String>> rechazarPostulante(
            @PathVariable Integer idPostulante,
            @RequestBody RechazarRequest request) {
        try {
            Integer idJefe = obtenerIdJefeActual();
            logger.info("JEFE {} rechazando postulante ID: {} - Motivo: {}", 
                       idJefe, idPostulante, request.getMotivo());
            
            postulanteService.rechazarPostulante(idPostulante, idJefe, request.getMotivo());
            
            return ResponseEntity.ok(GenericResponse.<String, String>builder()
                    .message("Postulante rechazado exitosamente")
                    .body("ID: " + idPostulante)
                    .statusCode("200")
                    .build());
                    
        } catch (Exception e) {
            logger.error("Error al rechazar postulante: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.<String, String>builder()
                            .message("Error: " + e.getMessage())
                            .statusCode("500")
                            .body(null)
                            .build());
        }
    }
    
    /**
     * POST /api/jefe/deudas/{idDeuda}/verificar
     * Verifica una deuda específica
     */
    @PostMapping("/deudas/{idDeuda}/verificar")
    public ResponseEntity<GenericResponse<String, String>> verificarDeuda(
            @PathVariable Integer idDeuda,
            @RequestBody VerificarDeudaRequest request) {
        try {
            Integer idJefe = obtenerIdJefeActual();
            logger.info("JEFE {} verificando deuda ID: {}", idJefe, idDeuda);
            
            deudaExternaService.verificarDeuda(idDeuda, idJefe, request.getObservaciones());
            
            return ResponseEntity.ok(GenericResponse.<String, String>builder()
                    .message("Deuda verificada exitosamente")
                    .body("ID: " + idDeuda)
                    .statusCode("200")
                    .build());
                    
        } catch (Exception e) {
            logger.error("Error al verificar deuda: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.<String, String>builder()
                            .message("Error: " + e.getMessage())
                            .statusCode("500")
                            .body(null)
                            .build());
        }
    }
    
    /**
     * Obtiene el ID del JEFE actual desde el contexto de seguridad
     * TODO: Implementar mapping de username a ID
     */
    private Integer obtenerIdJefeActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Usuario autenticado: {}", auth == null ? "NONE" : auth.getName());
        // Placeholder: idealmente debería mapear username a ID del usuario
        return 1;
    }
    
    // =====================================================
    // DTOs para Requests
    // =====================================================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RechazarRequest {
        private String motivo;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificarDeudaRequest {
        private String observaciones;
    }
}
