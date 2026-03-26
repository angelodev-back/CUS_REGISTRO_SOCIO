package com.idat.pe.Cus_Registro_Postulante.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbarcacionDTO {
    private Integer id;
    private String nombre;
    private String tipo;
    private String matricula;
    private String descripcion;
    private String estado;
    private Integer idSocio;
}
