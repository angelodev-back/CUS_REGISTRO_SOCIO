package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.service.DeudaExternaService;
import com.idat.pe.Cus_Registro_Postulante.client.DeudaExternaClient;
import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.ExternalDebtResponseDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.DeudaExterna;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.repository.DeudaExternaRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de deudas externas
 */
@Service
public class DeudaExternaServiceImpl implements DeudaExternaService {

    private static final Logger logger = LoggerFactory.getLogger(DeudaExternaServiceImpl.class);

    private final DeudaExternaClient deudaExternaClient;
    private final DeudaExternaRepository deudaExternaRepository;
    private final UsuarioRepository usuarioRepository;

    public DeudaExternaServiceImpl(
            DeudaExternaClient deudaExternaClient,
            DeudaExternaRepository deudaExternaRepository,
            UsuarioRepository usuarioRepository) {
        this.deudaExternaClient = deudaExternaClient;
        this.deudaExternaRepository = deudaExternaRepository;
        this.usuarioRepository = usuarioRepository;
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
        logger.info("Obteniendo deudas de la BD para postulante ID: {}", idPostulante);

        List<DeudaExterna> deudas = deudaExternaRepository.findByPostulante_Id(idPostulante);

        return deudas.stream()
                .map(this::mapearDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void verificarDeuda(Integer idDeuda, Integer idJefe, String observaciones) {
        logger.info("Verificando deuda ID: {} por jefe ID: {}", idDeuda, idJefe);

        try {
            DeudaExterna deuda = deudaExternaRepository.findById(idDeuda)
                    .orElseThrow(() -> new RuntimeException("Deuda no encontrada con id: " + idDeuda));

            Usuario jefe = usuarioRepository.findById(idJefe)
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado con id: " + idJefe));

            deuda.setVerificada(true);
            deuda.setFechaVerificacion(LocalDate.now());
            deuda.setVerificador(jefe);
            deuda.setObservacionesVerificacion(observaciones);

            deudaExternaRepository.save(deuda);
            logger.info("Deuda verificada exitosamente por jefe: {}", jefe.getUsername());

        } catch (Exception e) {
            logger.error("Error al verificar deuda: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar deuda: " + e.getMessage());
        }
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

    private DeudaExternaDTO mapearDTO(DeudaExterna entity) {
        String nombreJefe = null;
        if (entity.getVerificador() != null) {
            nombreJefe = entity.getVerificador().getNombres() + " " + entity.getVerificador().getApellidoPaterno();
        }

        return DeudaExternaDTO.builder()
                .id(entity.getId())
                .nombreClubOrigen(entity.getNombreClubOrigen())
                .montoDeuda(entity.getMontoDeuda())
                .fechaRegistro(entity.getFechaRegistro() != null ? entity.getFechaRegistro().toString() : null)
                .estado(entity.getEstado())
                .verificada(entity.getVerificada())
                .fechaVerificacion(entity.getFechaVerificacion() != null ? entity.getFechaVerificacion().toString() : null)
                .jefe(nombreJefe)
                .observacionesVerificacion(entity.getObservacionesVerificacion())
                .build();
    }
}
