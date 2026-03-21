package com.idat.pe.Cus_Registro_Postulante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta que combina datos del postulante con sus deudas
 * Permite al JEFE visualizar postulante + deudas asociadas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulanteConDeudasDTO {
    private Integer idPostulante;
    private String tipoDocumento; // DNI o RUC
    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String razonSocial; // para RUC
    private String correoElectronico;
    private String telefono;
    private String direccion;
    private String fechaNacimiento;
    private String tipoInteres; // Nautico o Social
    private String fechaRegistro;
    private String estadoPostulacion; // pendiente, aprobado, rechazado, subsanado
    
    // Deudas asociadas
    private List<DeudaExternaDTO> deudas;
    
    // Clasificación automática
    private String clasificacion; // Pagador, Pagador Esporádico, Renuente
}
