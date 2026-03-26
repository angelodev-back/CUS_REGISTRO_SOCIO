package com.idat.pe.Cus_Registro_Postulante.mapper;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.entity.EstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.TipoDocumento;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class PostulanteMapper {

    public Postulante toEntity(RegistroPostulanteDTO dto) {
        if (dto == null) return null;

        TipoDocumento tipo = TipoDocumento.valueOf(dto.getTipoDocumento());
        
        Postulante.PostulanteBuilder builder = Postulante.builder()
                .tipoDocumento(tipo)
                .numeroDocumento(dto.getNumeroDocumento())
                .correoElectronico(dto.getCorreo())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .ciudad(dto.getCiudad())
                .tipoInteres(dto.getTipoInteres())
                .codigoPostal(dto.getCodigoPostal())
                .fechaRegistro(LocalDate.now())
                .estado(EstadoPostulante.PENDIENTE);

        if (tipo == TipoDocumento.DNI) {
            builder.nombres(blankToNull(dto.getNombre()))
                   .apellidoPaterno(blankToNull(dto.getApellidoPaterno()))
                   .apellidoMaterno(blankToNull(dto.getApellidoMaterno()))
                   .fechaNacimiento(dto.getFechaNacimiento())
                   .razonSocial(null);
        } else {
            builder.razonSocial(blankToNull(dto.getRazonSocial()))
                   .nombres(null)
                   .apellidoPaterno(null)
                   .apellidoMaterno(null)
                   .fechaNacimiento(dto.getFechaNacimiento());
        }

        return builder.build();
    }

    private String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }

    public PostulanteDTO toDTO(Postulante entity) {
        if (entity == null) return null;

        return PostulanteDTO.builder()
                .idPostulante(entity.getId())
                .tipoDocumento(entity.getTipoDocumento().name())
                .numeroDocumento(entity.getNumeroDocumento())
                .nombres(entity.getNombres())
                .apellidoPaterno(entity.getApellidoPaterno())
                .apellidoMaterno(entity.getApellidoMaterno())
                .razonSocial(entity.getRazonSocial())
                .correoElectronico(entity.getCorreoElectronico())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .ciudad(entity.getCiudad())
                .fechaNacimiento(entity.getFechaNacimiento())
                .tipoInteres(entity.getTipoInteres())
                .codigoPostal(entity.getCodigoPostal())
                .fechaRegistro(entity.getFechaRegistro())
                .estadoPostulacion(entity.getEstado() != null ? entity.getEstado().name().toLowerCase() : null)
                .build();
    }
}
