package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad HistorialEstadoPostulante
 * Mapea la tabla: historial_estado_postulante
 */
@Repository
public interface HistorialEstadoPostulanteRepository extends JpaRepository<HistorialEstadoPostulante, Integer> {
    
    /**
     * Busca el historial por ID de postulante
     */
    List<HistorialEstadoPostulante> findByIdPostulanteOrderByFechaCambioDesc(Integer idPostulante);
}
