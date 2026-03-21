package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.UbicacionGeografica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UbicacionGeograficaRepository extends JpaRepository<UbicacionGeografica, Integer> {
    Optional<UbicacionGeografica> findByNombreAndTipoUbicacionAndPadre(String nombre, String tipoUbicacion, UbicacionGeografica padre);
    Optional<UbicacionGeografica> findByNombreAndTipoUbicacionAndPadreIsNull(String nombre, String tipoUbicacion);
}
