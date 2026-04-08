package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.repository.InformeAdmisionRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import com.idat.pe.Cus_Registro_Postulante.service.InformeAdmisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InformeAdmisionServiceImpl implements InformeAdmisionService {

    @Autowired
    private InformeAdmisionRepository informeRepository;

    @Autowired
    private PostulanteRepository postulanteRepository;

    @Override
    @Transactional
    public InformeAdmision guardarOActualizar(Integer idPostulante, String observaciones, String recomendacion, String estado) {
        InformeAdmision informe = informeRepository.findByPostulante_Id(idPostulante)
                .orElseGet(() -> {
                    Postulante p = postulanteRepository.findById(idPostulante)
                            .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
                    return InformeAdmision.builder()
                            .postulante(p)
                            .build();
                });

        informe.setObservaciones(observaciones);
        informe.setRecomendacionManual(recomendacion);
        if (estado != null) {
            informe.setEstado(estado);
        }
        
        return informeRepository.save(informe);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InformeAdmision> obtenerPorPostulante(Integer idPostulante) {
        return informeRepository.findByPostulante_Id(idPostulante);
    }
}
