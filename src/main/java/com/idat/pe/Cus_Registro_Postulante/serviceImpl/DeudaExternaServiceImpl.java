package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.service.DeudaExternaService;
import com.idat.pe.Cus_Registro_Postulante.client.DeudaExternaClient;
import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.ExternalDebtResponseDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de deudas externas
 */
@Service
public class DeudaExternaServiceImpl implements DeudaExternaService {

    private static final Logger logger = LoggerFactory.getLogger(DeudaExternaServiceImpl.class);

    private final DeudaExternaClient deudaExternaClient;
    private final PostulanteRepository postulanteRepository;

    // Estado temporal en memoria para no depender de tabla local de deuda_externa.
    private final Map<Integer, DeudaExternaDTO> deudasVerificadas = new ConcurrentHashMap<>();

    public DeudaExternaServiceImpl(
            DeudaExternaClient deudaExternaClient,
            PostulanteRepository postulanteRepository) {
        this.deudaExternaClient = deudaExternaClient;
        this.postulanteRepository = postulanteRepository;
    }

    @Override
    public List<ExternalDebtResponseDTO> obtenerTodasLasDeudas() {
        logger.info("Obteniendo todas las deudas externas desde API remoto");
        return deudaExternaClient.obtenerDeudas();
    }

    @Override
    public ExternalDebtResponseDTO obtenerDatosExternos(String tipoDoc, String numDoc) {
        logger.info("Buscando datos externos para {} {}", tipoDoc, numDoc);
        try {
            return obtenerTodasLasDeudas().stream()
                .filter(d -> d.getTipoDocumento() != null && d.getTipoDocumento().equalsIgnoreCase(tipoDoc) 
                          && d.getNumeroDocumento() != null && d.getNumeroDocumento().equals(numDoc))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar datos externos sync: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<DeudaExternaDTO> obtenerDeudasPorPostulante(Integer idPostulante) {
        logger.info("Obteniendo deudas desde API externa para postulante ID: {}", idPostulante);

        Postulante postulante = postulanteRepository.findById(idPostulante)
            .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));

        ExternalDebtResponseDTO external = obtenerDatosExternos(
            postulante.getTipoDocumento().name(),
            postulante.getNumeroDocumento()
        );

        if (external == null || external.getDeudas() == null) {
            return Collections.emptyList();
        }

        return external.getDeudas().stream()
            .map(this::aplicarVerificacionTemporal)
            .collect(Collectors.toList());
    }

    @Override
    public void verificarDeuda(Integer idDeuda, Integer idJefe, String observaciones) {
        logger.info("Marcando verificación temporal de deuda externa ID: {} por usuario ID: {}", idDeuda, idJefe);

        if (idDeuda == null || idDeuda <= 0) {
            throw new RuntimeException("Identificador de deuda inválido");
        }

        DeudaExternaDTO verificada = DeudaExternaDTO.builder()
            .id(idDeuda)
            .verificada(true)
            .fechaVerificacion(LocalDate.now().toString())
            .jefe(idJefe != null && idJefe > 0 ? "usuario-" + idJefe : "jefe")
            .observacionesVerificacion(observaciones)
            .build();

        deudasVerificadas.put(idDeuda, verificada);
    }

    @Override
    public String clasificarPostulante(List<DeudaExternaDTO> deudas) {
        logger.info("Clasificando postulante con {} deudas", deudas == null ? 0 : deudas.size());

        if (deudas == null || deudas.isEmpty()) {
            return "Socio Pagador";
        }

        boolean tieneVencida = deudas.stream()
                .anyMatch(d -> "vencido".equalsIgnoreCase(d.getEstado()));

        boolean tienePendiente = deudas.stream()
                .anyMatch(d -> "pendiente".equalsIgnoreCase(d.getEstado()));

        boolean tienePagada = deudas.stream()
                .anyMatch(d -> "pagado".equalsIgnoreCase(d.getEstado()));

        if (tieneVencida || tienePendiente) {
            return "Socio Renuente a Pago";
        }

        if (tienePagada) {
            return "Socio Pagador Esporádico";
        }

        return "Socio Pagador";
    }

    private DeudaExternaDTO aplicarVerificacionTemporal(DeudaExternaDTO deuda) {
        if (deuda == null) {
            return null;
        }

        if (deuda.getId() == null) {
            if (deuda.getVerificada() == null) {
                deuda.setVerificada(false);
            }
            return deuda;
        }

        DeudaExternaDTO temporal = deudasVerificadas.get(deuda.getId());
        if (temporal != null) {
            deuda.setVerificada(true);
            deuda.setFechaVerificacion(temporal.getFechaVerificacion());
            deuda.setJefe(temporal.getJefe());
            deuda.setObservacionesVerificacion(temporal.getObservacionesVerificacion());
        } else if (deuda.getVerificada() == null) {
            deuda.setVerificada(false);
        }

        return deuda;
    }
}
