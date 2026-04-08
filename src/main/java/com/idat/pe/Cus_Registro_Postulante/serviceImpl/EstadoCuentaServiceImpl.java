package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.dto.EstadoCuentaDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.EstadoCuenta;
import com.idat.pe.Cus_Registro_Postulante.repository.EstadoCuentaRepository;
import com.idat.pe.Cus_Registro_Postulante.service.EstadoCuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstadoCuentaServiceImpl implements EstadoCuentaService {

    @Autowired
    private EstadoCuentaRepository estadoCuentaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EstadoCuentaDTO> obtenerEstadoCuentaPorSocio(Integer idSocio) {
        return estadoCuentaRepository.findBySocio_IdOrderByPeriodoDesc(idSocio)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularSaldoTotalPendiente(Integer idSocio) {
        return estadoCuentaRepository.findBySocio_IdOrderByPeriodoDesc(idSocio)
                .stream()
                .mapToDouble(EstadoCuenta::getSaldoPendiente)
                .sum();
    }

    private EstadoCuentaDTO convertirADto(EstadoCuenta entity) {
        return EstadoCuentaDTO.builder()
                .id(entity.getId())
                .periodo(entity.getPeriodo())
                .montoTotal(entity.getMontoTotal())
                .montoPagado(entity.getMontoPagado())
                .saldoPendiente(entity.getSaldoPendiente())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }
}
