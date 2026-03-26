package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import java.util.List;

public interface PostulanteService {
    PostulanteDTO registrarPostulante(RegistroPostulanteDTO dto);
    List<PostulanteDTO> listarPostulantes();
    PostulanteDTO buscarPorId(Integer id);
    PostulanteDTO actualizarDatos(Integer id, RegistroPostulanteDTO dto);
    PostulanteDTO cambiarEstado(Integer id, String nuevoEstado);
    
    // Métodos para flujo JEFE
    List<PostulanteConDeudasDTO> obtenerPostulantesPendientesConDeudas();
    PostulanteConDeudasDTO obtenerPostulanteConDeudasDetalle(Integer idPostulante);
    void aprobarPostulante(Integer idPostulante, Integer idJefe);
    void rechazarPostulante(Integer idPostulante, Integer idJefe, String motivo);
    void subsanarPostulante(Integer idPostulante, RegistroPostulanteDTO dto);
    PostulanteDTO buscarPorNumeroDocumento(String numero);
}
