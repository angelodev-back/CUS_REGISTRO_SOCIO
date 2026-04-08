package com.idat.pe.Cus_Registro_Postulante.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO que mapea la respuesta raíz del JSON de deudas externas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalDebtResponseDTO {
    
    @JsonProperty("tipo_documento")
    private String tipoDocumento;
    
    @JsonProperty("numero_documento")
    private String numeroDocumento;
    
    @JsonProperty("clasificacion_sugerida")
    private String clasificacionSugerida;

    @JsonProperty("ciudad")
    private String ciudad;

    @JsonProperty("distrito")
    private String distrito;

    @JsonProperty("provincia")
    private String provincia;

    @JsonProperty("departamento")
    private String departamento;
    
    @JsonProperty("deudas")
    private List<DeudaExternaDTO> deudas;
}
