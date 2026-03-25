package com.idat.pe.Cus_Registro_Postulante.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulanteDTO {
    private Integer id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String razonSocial;
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private LocalDate fechaNacimiento;
    private String tipoInteres;
    private String codigoPostal;
    private LocalDate fechaRegistro;
    private String estado;
}
