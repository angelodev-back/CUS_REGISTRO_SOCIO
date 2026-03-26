package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.Embarcacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmbarcacionRepository extends JpaRepository<Embarcacion, Integer> {
    List<Embarcacion> findBySocio(com.idat.pe.Cus_Registro_Postulante.entity.Socio socio);
}
