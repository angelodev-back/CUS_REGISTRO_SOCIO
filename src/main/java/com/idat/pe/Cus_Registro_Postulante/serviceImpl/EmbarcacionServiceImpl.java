package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.dto.EmbarcacionDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.Embarcacion;
import com.idat.pe.Cus_Registro_Postulante.entity.Socio;
import com.idat.pe.Cus_Registro_Postulante.repository.EmbarcacionRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.SocioRepository;
import com.idat.pe.Cus_Registro_Postulante.service.EmbarcacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbarcacionServiceImpl implements EmbarcacionService {

    @Autowired
    private EmbarcacionRepository embarcacionRepository;
    
    @Autowired
    private SocioRepository socioRepository;

    @Override
    @Transactional
    public EmbarcacionDTO registrarEmbarcacion(EmbarcacionDTO dto, Integer idSocio) {
        try {
            Socio socio = socioRepository.findById(idSocio)
                    .orElseThrow(() -> new RuntimeException("Socio no encontrado con id: " + idSocio));
            
            Embarcacion embarcacion = Embarcacion.builder()
                    .nombre(dto.getNombre())
                    .tipo(dto.getTipo())
                    .matricula(dto.getMatricula())
                    .descripcion(dto.getDescripcion())
                    .estado("Activa")
                    .socio(socio)
                    .build();
            
            Embarcacion guardada = embarcacionRepository.save(embarcacion);
            return mapearADTO(guardada);
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar embarcación: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmbarcacionDTO> listarPorSocio(Integer idSocio) {
        Socio socio = socioRepository.findById(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado con id: " + idSocio));
        return embarcacionRepository.findBySocio(socio).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmbarcacionDTO actualizarEmbarcacion(Integer id, EmbarcacionDTO dto) {
        Embarcacion embarcacion = embarcacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Embarcación no encontrada con id: " + id));
        
        embarcacion.setNombre(dto.getNombre());
        embarcacion.setTipo(dto.getTipo());
        embarcacion.setMatricula(dto.getMatricula());
        embarcacion.setDescripcion(dto.getDescripcion());
        embarcacion.setEstado(dto.getEstado());
        
        if (dto.getIdSocio() != null && !embarcacion.getSocio().getId().equals(dto.getIdSocio())) {
            Socio nuevoSocio = socioRepository.findById(dto.getIdSocio())
                    .orElseThrow(() -> new RuntimeException("Socio no encontrado con id: " + dto.getIdSocio()));
            embarcacion.setSocio(nuevoSocio);
        }

        Embarcacion actualizada = embarcacionRepository.save(embarcacion);
        return mapearADTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminarEmbarcacion(Integer id) {
        embarcacionRepository.deleteById(id);
    }

    private EmbarcacionDTO mapearADTO(Embarcacion e) {
        return EmbarcacionDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .matricula(e.getMatricula())
                .descripcion(e.getDescripcion())
                .estado(e.getEstado())
                .idSocio(e.getSocio().getId())
                .build();
    }
}
