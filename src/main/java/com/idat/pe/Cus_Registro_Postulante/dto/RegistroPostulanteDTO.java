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
    @Pattern(regexp = "DNI|RUC", message = "El tipo de documento debe ser DNI o RUC")
    @Size(max = 10, message = "El tipo de documento no debe exceder 10 caracteres")
    private String tipoDocumento; // DNI, RUC

    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "\\d+", message = "El número de documento solo debe contener dígitos")
    @Size(min = 8, max = 11, message = "El número de documento debe tener entre 8 y 11 caracteres")
    private String numeroDocumento;

    // Persona Natural (si tipoDocumento es DNI)
    @Pattern(regexp = "^$|^[A-Za-zÀ-ÿÑñ\\s]+$", message = "El nombre solo debe contener letras y espacios")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @Pattern(regexp = "^$|^[A-Za-zÀ-ÿÑñ\\s]+$", message = "El apellido paterno solo debe contener letras y espacios")
    @Size(max = 100, message = "El apellido paterno no debe exceder 100 caracteres")
    private String apellidoPaterno;

    @Pattern(regexp = "^$|^[A-Za-zÀ-ÿÑñ\\s]+$", message = "El apellido materno solo debe contener letras y espacios")
    @Size(max = 100, message = "El apellido materno no debe exceder 100 caracteres")
    private String apellidoMaterno;

    // Persona Jurídica (si tipoDocumento es RUC)
    @Size(max = 200, message = "La razón social no debe exceder 200 caracteres")
    private String razonSocial;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
    private String correo;

    @NotBlank(message = "El tipo de teléfono es obligatorio")
    @Pattern(regexp = "FIJO|CELULAR", message = "El tipo de teléfono debe ser FIJO o CELULAR")
    @Size(max = 10, message = "El tipo de teléfono no debe exceder 10 caracteres")
    private String tipoTelefono;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 15, message = "El teléfono no debe exceder 15 caracteres")
    @Pattern(regexp = "^\\+?\\d+$", message = "El teléfono solo debe contener dígitos y el prefijo + cuando aplique")
    private String telefono;

    @Size(max = 200, message = "La dirección no debe exceder 200 caracteres")
    private String direccion;

    @Pattern(regexp = "^$|^[A-Za-zÀ-ÿÑñ\\s]+$", message = "La ciudad solo debe contener letras y espacios")
    @Size(max = 50, message = "La ciudad no debe exceder 50 caracteres")
    private String ciudad;

    private LocalDate fechaNacimiento;

    @Size(max = 20, message = "El tipo de interés no debe exceder 20 caracteres")
    private String tipoInteres;

    @Pattern(regexp = "^$|^\\d{5}$", message = "El código postal debe contener exactamente 5 dígitos numéricos")
    private String codigoPostal;
}
