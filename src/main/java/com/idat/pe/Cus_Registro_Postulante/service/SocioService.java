package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.dto.ConsultaEstadoDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.SocioAprobadoDTO;

import java.util.List;
import java.util.Map;

public interface SocioService {

    List<SocioAprobadoDTO> listarSociosAprobados();

    Map<String, Object> generarCuentaSocio(Integer socioId);

    ConsultaEstadoDTO consultarEstadoPublico(String numeroDocumento);
}
