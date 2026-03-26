package com.idat.pe.Cus_Registro_Postulante.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostulanteDTO {
    
    @JsonProperty("id_postulante")
    private Integer idPostulante;
    
    @JsonProperty("tipo_documento")
    private String tipoDocumento;
    
    @JsonProperty("numero_documento")
    private String numeroDocumento;
    
    private String nombres;
    
    @JsonProperty("apellido_paterno")
    private String apellidoPaterno;
    
    @JsonProperty("apellido_materno")
    private String apellidoMaterno;
    
    @JsonProperty("razon_social")
    private String razonSocial;
    
    @JsonProperty("correo_electronico")
    private String correoElectronico;
    
    private String telefono;
    private String direccion;
    private String ciudad;
    
    @JsonProperty("fecha_nacimiento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;
    
    @JsonProperty("tipo_interes")
    private String tipoInteres;
    
    @JsonProperty("codigo_postal")
    private String codigoPostal;
    
    @JsonProperty("fecha_registro")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;
    
    @JsonProperty("estado_postulacion")
    private String estadoPostulacion;
}
