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
    
    // Nuevos métodos para flujo JEFE (Paso 2)
    /**
     * Obtiene postulantes pendientes/subsanados con sus deudas para revisión del JEFE
     */
    List<PostulanteConDeudasDTO> obtenerPostulantesPendientesConDeudas();
    
    /**
     * Obtiene un postulante específico con detalles de deudas
     */
    PostulanteConDeudasDTO obtenerPostulanteConDeudasDetalle(Integer idPostulante);
    
    /**
     * Aprueba un postulante y crea automáticamente socio + usuario
     */
    void aprobarPostulante(Integer idPostulante, Integer idJefe);
    
    /**
     * Rechaza un postulante registrando el motivo
     */
    void rechazarPostulante(Integer idPostulante, Integer idJefe, String motivo);
}
