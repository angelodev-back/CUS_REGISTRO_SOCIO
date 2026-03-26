package com.idat.pe.Cus_Registro_Postulante.client;

import com.idat.pe.Cus_Registro_Postulante.dto.ExternalDebtResponseDTO;
import java.util.List;

/**
 * Interface para consumir API remota de deudas externas
 * Aplica SOLID: Single Responsibility Principle
 * Responsabilidad única: consumir datos del API remoto
 */
public interface DeudaExternaClient {
    
    /**
     * Obtiene todas las deudas externas del JSON remoto con su estructura anidada
     * @return Lista de respuestas desde la API remota
     */
    List<ExternalDebtResponseDTO> obtenerDeudas();
}
