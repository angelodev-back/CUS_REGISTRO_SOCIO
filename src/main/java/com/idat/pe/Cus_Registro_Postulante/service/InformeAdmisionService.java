package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision;
import java.util.Optional;

public interface InformeAdmisionService {
    InformeAdmision guardarOActualizar(Integer idPostulante, String observaciones, String recomendacion, String estado);
    Optional<InformeAdmision> obtenerPorPostulante(Integer idPostulante);
}
