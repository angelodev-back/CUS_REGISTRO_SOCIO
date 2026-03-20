package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Socio
 * Mapea la tabla: socio
 */
@Repository
public interface SocioRepository extends JpaRepository<Socio, Integer> {
    
    /**
     * Busca un socio por ID de postulante
     */
    Optional<Socio> findByIdPostulante(Integer idPostulante);
    
    /**
     * Busca un socio por ID de usuario
     */
    Optional<Socio> findByIdUsuario(Integer idUsuario);
}
