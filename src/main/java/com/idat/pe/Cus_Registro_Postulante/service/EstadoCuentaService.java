package com.idat.pe.Cus_Registro_Postulante.service;

import com.idat.pe.Cus_Registro_Postulante.dto.EstadoCuentaDTO;
import java.util.List;

public interface EstadoCuentaService {
    List<EstadoCuentaDTO> obtenerEstadoCuentaPorSocio(Integer idSocio);
    Double calcularSaldoTotalPendiente(Integer idSocio);
}
