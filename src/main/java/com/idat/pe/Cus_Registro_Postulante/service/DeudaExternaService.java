package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import java.util.List;

/**
 * Interface para servicios de deudas externas
 * Aplica SOLID: Dependency Inversion Principle
 * Permite abstracción de fuentes de datos (JSON, BD, APIs)
 */
public interface DeudaExternaService {
    
    /**
     * Obtiene todas las deudas externas del JSON remoto
     */
    List<DeudaExternaDTO> obtenerTodasLasDeudas();
    
    /**
     * Obtiene deudas de la BD local para un postulante por su ID
     */
    List<DeudaExternaDTO> obtenerDeudasPorPostulante(Integer idPostulante);
    
    /**
     * Marca una deuda como verificada por el jefe
     */
    void verificarDeuda(Integer idDeuda, Integer idJefe, String observaciones);
    
    /**
     * Clasifica al postulante según sus deudas
     */
    String clasificarPostulante(List<DeudaExternaDTO> deudas);
}
