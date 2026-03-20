package com.idat.pe.Cus_Registro_Postulante.client;

import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import java.util.List;

/**
 * Interface para consumir API remota de deudas externas
 * Aplica SOLID: Single Responsibility Principle
 * Responsabilidad única: consumir datos del API remoto
 */
public interface DeudaExternaClient {
    
    /**
     * Obtiene todas las deudas externas del JSON remoto
     * @return Lista de deudas desde la API remota
     */
    List<DeudaExternaDTO> obtenerDeudas();
}
