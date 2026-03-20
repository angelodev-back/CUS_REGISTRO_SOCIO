package com.idat.pe.Cus_Registro_Postulante.genericResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse<M, T> {
    private M message;
    private T body;
    private String statusCode;
}
