package com.idat.pe.Cus_Registro_Postulante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaEstadoDTO {

    private String nombre;
    private String numeroDocumento;
    private String email;
    private String estado;
    private Boolean tieneAcceso;
    private String mensajeEstado;
    private String motivoRechazo;
}
