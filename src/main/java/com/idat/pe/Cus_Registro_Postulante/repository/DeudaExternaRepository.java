package com.idat.pe.Cus_Registro_Postulante.repository;

import com.idat.pe.Cus_Registro_Postulante.entity.DeudaExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeudaExternaRepository extends JpaRepository<DeudaExterna, Integer> {
    
    /**
     * Busca deudas por ID del postulante
     */
    List<DeudaExterna> findByIdPostulante(Integer idPostulante);
    
    /**
     * Busca deudas verificadas
     */
    List<DeudaExterna> findByVerificadaTrue();
}
