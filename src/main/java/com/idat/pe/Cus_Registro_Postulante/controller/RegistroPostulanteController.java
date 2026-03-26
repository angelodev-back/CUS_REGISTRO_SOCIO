package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.genericResponse.GenericResponse;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador auxiliar para validaciones vía JavaScript en el frontend.
 * Mantiene compatibilidad con el flujo de autocompletado del formulario de registro.
 */
@RestController
@RequestMapping("/api/postulantes")
public class RegistroPostulanteController {

    @Autowired
    private PostulanteService postulanteService;

    @GetMapping("/buscar-por-documento/{numero}")
    public ResponseEntity<GenericResponse<String, PostulanteDTO>> buscarPorDocumento(@PathVariable String numero) {
        PostulanteDTO response = postulanteService.buscarPorNumeroDocumento(numero);
        return ResponseEntity.ok(GenericResponse.<String, PostulanteDTO>builder()
                .message(response != null ? "Postulante encontrado" : "No se encontró postulante")
                .body(response)
                .build());
    }
}
