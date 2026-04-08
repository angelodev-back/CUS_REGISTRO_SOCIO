package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InformeAdmisionRepository extends JpaRepository<InformeAdmision, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT i FROM InformeAdmision i WHERE i.postulante.id = :idPostulante")
    Optional<InformeAdmision> findByPostulante_Id(@org.springframework.data.repository.query.Param("idPostulante") Integer idPostulante);
}
