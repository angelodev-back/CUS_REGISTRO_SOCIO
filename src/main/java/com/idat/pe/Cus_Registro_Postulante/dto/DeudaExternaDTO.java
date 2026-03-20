package com.idat.pe.Cus_Registro_Postulante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para representar una deuda externa de un postulante
 * Mapea datos tanto del JSON externo como de la base de datos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeudaExternaDTO {
    private Integer id;
    private String nombreClubOrigen;
    private BigDecimal montoDeuda;
    private LocalDate fechaRegistro;
    private String estado; // pendiente, pagado, vencido
    private Boolean verificada;
    private LocalDate fechaVerificacion;
    private String jefe; // nombre del jefe que verificó
    private String observacionesVerificacion;
    private String numeroDocumento; // DNI o RUC del postulante
    private String tipoDocumento; // DNI o RUC
}
