package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.Socio;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio para la entidad Socio
 * Mapea la tabla: socio
 */
@Repository
public interface SocioRepository extends JpaRepository<Socio, Integer> {
    
    Optional<Socio> findByPostulante(Postulante postulante);
    
    Optional<Socio> findByUsuario(Usuario usuario);
}
