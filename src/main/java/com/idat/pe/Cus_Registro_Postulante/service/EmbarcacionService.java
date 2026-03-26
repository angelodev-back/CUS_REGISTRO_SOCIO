package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.dto.EmbarcacionDTO;
import java.util.List;

public interface EmbarcacionService {
    EmbarcacionDTO registrarEmbarcacion(EmbarcacionDTO dto, Integer idSocio);
    List<EmbarcacionDTO> listarPorSocio(Integer idSocio);
    EmbarcacionDTO actualizarEmbarcacion(Integer id, EmbarcacionDTO dto);
    void eliminarEmbarcacion(Integer id);
}
