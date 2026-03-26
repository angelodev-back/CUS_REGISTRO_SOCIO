package com.idat.pe.Cus_Registro_Postulante.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para representar una deuda externa de un postulante
 * Usa @JsonProperty para mapear snake_case del JSON externo a camelCase Java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeudaExternaDTO {
    private Integer id;

    @JsonProperty("nombre_club_origen")
    private String nombreClubOrigen;

    @JsonProperty("monto_deuda")
    private BigDecimal montoDeuda;

    @JsonProperty("fecha_registro")
    private String fechaRegistro;

    private String estado; // pendiente, pagado, vencido

    private Boolean verificada;

    @JsonProperty("fecha_verificacion")
    private String fechaVerificacion;

    // Nombre del jefe que verificó (enriquecido en backend)
    private String jefe;

    @JsonProperty("observaciones_verificacion")
    private String observacionesVerificacion;

    // Para buscar en JSON externo por persona
    @JsonProperty("numero_documento")
    private String numeroDocumento;

    @JsonProperty("tipo_documento")
    private String tipoDocumento;

    @JsonProperty("id_verificador")
    private Integer idVerificador;
}
