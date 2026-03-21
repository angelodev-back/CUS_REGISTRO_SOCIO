package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.service.DeudaExternaService;
import com.idat.pe.Cus_Registro_Postulante.client.DeudaExternaClient;
import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
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
 * Aplica SOLID:
 * - Single Responsibility: maneja lógica de deudas
 * - Dependency Inversion: depende de interfaces, no de implementaciones
 * - Interface Segregation: usa especialización de interfaces (Client vs Service)
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
    public List<DeudaExternaDTO> obtenerTodasLasDeudas() {
        logger.info("Obteniendo todas las deudas externas desde API remoto");
        return deudaExternaClient.obtenerDeudas();
    }

    /**
     * Bug 2 fix: ahora busca en la BD local por idPostulante
     * La entidad DeudaExterna tiene id_postulante, no numero_documento
     */
    @Override
    public List<DeudaExternaDTO> obtenerDeudasPorPostulante(Integer idPostulante) {
        logger.info("Obteniendo deudas de la BD para postulante ID: {}", idPostulante);

        List<DeudaExterna> deudas = deudaExternaRepository.findByIdPostulante(idPostulante);

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
            // Bug 1 fix: idJefe es Integer, no necesita .longValue()
            deuda.setIdVerificador(idJefe);
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
            logger.info("Clasificación: Socio Pagador (Sin deudas)");
            return "Socio Pagador";
        }

        // Verificar si hay deudas vencidas o pendientes sin pagar
        boolean tieneVencida = deudas.stream()
                .anyMatch(d -> "vencido".equalsIgnoreCase(d.getEstado()));

        boolean tienePendiente = deudas.stream()
                .anyMatch(d -> "pendiente".equalsIgnoreCase(d.getEstado()));

        // Verificar si hay deudas pagadas (historial de pagos)
        boolean tienePagada = deudas.stream()
                .anyMatch(d -> "pagado".equalsIgnoreCase(d.getEstado()));

        // Renuente: tiene pendientes o vencidas sin resolver
        if (tieneVencida || tienePendiente) {
            logger.info("Clasificación: Socio Renuente a Pago (Tiene deudas pendientes o vencidas)");
            return "Socio Renuente a Pago";
        }

        // Pagador esporádico: todas sus deudas están pagadas (tiene historial pero pagó)
        if (tienePagada) {
            logger.info("Clasificación: Socio Pagador Esporádico (Tiene deudas pagadas)");
            return "Socio Pagador Esporádico";
        }

        logger.info("Clasificación: Socio Pagador");
        return "Socio Pagador";
    }

    // =====================================================
    // MÉTODOS PRIVADOS HELPER
    // =====================================================

    /**
     * Mapea una entidad DeudaExterna a DTO
     */
    private DeudaExternaDTO mapearDTO(DeudaExterna entity) {
        String nombreJefe = null;
        if (entity.getIdVerificador() != null) {
            nombreJefe = usuarioRepository.findById(entity.getIdVerificador())
                    .map(u -> u.getNombres() + " " + u.getApellidoPaterno())
                    .orElse("Desconocido");
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
