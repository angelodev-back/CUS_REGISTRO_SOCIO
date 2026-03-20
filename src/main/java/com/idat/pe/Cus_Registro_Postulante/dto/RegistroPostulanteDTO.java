package com.idat.pe.Cus_Registro_Postulante.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroPostulanteDTO {

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento; // DNI, RUC

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, max = 11, message = "El número de documento debe tener entre 8 y 11 caracteres")
    private String numeroDocumento;

    // Persona Natural (si tipoDocumento es DNI)
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    // Persona Jurídica (si tipoDocumento es RUC)
    private String razonSocial;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    private String telefono;
    private String direccion;
    private Integer idCiudad;
    private LocalDate fechaNacimiento;
    private String tipoInteres;
    private String codigoPostal;

    // Campos de ubicación detallada
    private String pais;
    private String departamento;
    private String provincia;
    private String distrito;
}
