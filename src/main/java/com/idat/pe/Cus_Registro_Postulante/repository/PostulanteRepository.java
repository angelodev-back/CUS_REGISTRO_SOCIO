package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.entity.EstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PostulanteRepository extends JpaRepository<Postulante, Integer> {
    Optional<Postulante> findByNumeroDocumento(String numeroDocumento);
    Optional<Postulante> findByCorreoElectronico(String correoElectronico);
    List<Postulante> findByEstado(EstadoPostulante estado);
    Optional<Postulante> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipo, String numeroDocumento);
}
