package com.idat.pe.Cus_Registro_Postulante.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCuentaDTO {
    private Integer id;
    private String periodo;
    private Double montoTotal;
    private Double montoPagado;
    private Double saldoPendiente;
    private LocalDateTime fechaActualizacion;
}
