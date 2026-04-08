package com.idat.pe.Cus_Registro_Postulante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformeAdmisionDTO {
    private Integer idInforme;
    private Integer idPostulante;
    private String observaciones;
    private String recomendacionManual;
    private String estado;
    private String fechaUltimaEdicion;
}
