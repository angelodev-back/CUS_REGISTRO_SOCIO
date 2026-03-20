package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.*;
import com.idat.pe.Cus_Registro_Postulante.mapper.PostulanteMapper;
import com.idat.pe.Cus_Registro_Postulante.repository.*;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.service.DeudaExternaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostulanteServiceImpl implements PostulanteService {
    
    private static final Logger logger = LoggerFactory.getLogger(PostulanteServiceImpl.class);

    @Autowired
    private PostulanteRepository postulanteRepository;

    @Autowired
    private PostulanteMapper postulanteMapper;
    
    @Autowired
    private DeudaExternaService deudaExternaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private HistorialEstadoPostulanteRepository historialRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =====================================================
    // MÉTODOS BÁSICOS
    // =====================================================

    @Override
    @Transactional
    public PostulanteDTO registrarPostulante(RegistroPostulanteDTO dto) {
        if (postulanteRepository.findByNumeroDocumento(dto.getNumeroDocumento()).isPresent()) {
            throw new RuntimeException("El documento " + dto.getNumeroDocumento() + " ya se encuentra registrado.");
        }
        if (postulanteRepository.findByCorreoElectronico(dto.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo " + dto.getCorreo() + " ya se encuentra registrado.");
        }

        Postulante postulante = postulanteMapper.toEntity(dto);
        Postulante guardado = postulanteRepository.save(postulante);
        return postulanteMapper.toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulanteDTO> listarPostulantes() {
        List<Postulante> postulantes = postulanteRepository.findAll();
        return postulantes.stream()
                .map(postulanteMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostulanteDTO buscarPorId(Integer id) {
        Postulante postulante = postulanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + id));
        return postulanteMapper.toDTO(postulante);
    }

    @Override
    @Transactional
    public PostulanteDTO actualizarDatos(Integer id, RegistroPostulanteDTO dto) {
        Postulante postulante = postulanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + id));

        postulante.setTipoDocumento(TipoDocumento.valueOf(dto.getTipoDocumento()));
        postulante.setNumeroDocumento(dto.getNumeroDocumento());
        postulante.setCorreoElectronico(dto.getCorreo());
        postulante.setTelefono(dto.getTelefono());
        postulante.setDireccion(dto.getDireccion());
        postulante.setIdCiudad(dto.getIdCiudad());
        postulante.setTipoInteres(dto.getTipoInteres());
        postulante.setCodigoPostal(dto.getCodigoPostal());

        if (postulante.getTipoDocumento() == TipoDocumento.DNI) {
            postulante.setNombres(dto.getNombre());
            postulante.setApellidoPaterno(dto.getApellidoPaterno());
            postulante.setApellidoMaterno(dto.getApellidoMaterno());
            postulante.setFechaNacimiento(dto.getFechaNacimiento());
            postulante.setRazonSocial(null);
        } else {
            postulante.setRazonSocial(dto.getRazonSocial());
            postulante.setNombres(null);
            postulante.setApellidoPaterno(null);
            postulante.setApellidoMaterno(null);
            postulante.setFechaNacimiento(null);
        }

        Postulante actualizado = postulanteRepository.save(postulante);
        return postulanteMapper.toDTO(actualizado);
    }

    @Override
    @Transactional
    public PostulanteDTO cambiarEstado(Integer id, String nuevoEstado) {
        Postulante postulante = postulanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + id));

        try {
            postulante.setEstado(EstadoPostulante.valueOf(nuevoEstado));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado no válido: " + nuevoEstado);
        }

        Postulante actualizado = postulanteRepository.save(postulante);
        return postulanteMapper.toDTO(actualizado);
    }

    // =====================================================
    // MÉTODOS PARA FLUJO JEFE (Paso 2)
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<PostulanteConDeudasDTO> obtenerPostulantesPendientesConDeudas() {
        logger.info("Obteniendo postulantes pendientes/subsanados con deudas");
        
        List<Postulante> postulantes = postulanteRepository.findByEstado(EstadoPostulante.PENDIENTE);
        postulantes.addAll(postulanteRepository.findByEstado(EstadoPostulante.SUBSANADO));
        
        return postulantes.stream()
                .map(this::construirPostulanteConDeudas)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostulanteConDeudasDTO obtenerPostulanteConDeudasDetalle(Integer idPostulante) {
        logger.info("Obteniendo detalles de postulante ID: {}", idPostulante);
        
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));
        
        return construirPostulanteConDeudas(postulante);
    }

    @Override
    @Transactional
    public void aprobarPostulante(Integer idPostulante, Integer idJefe) {
        logger.info("======= INICIANDO APROBACIÓN DE POSTULANTE =======");
        logger.info("Postulante ID: {}, Jefe ID: {}", idPostulante, idJefe);
        
        // 1. Buscar postulante
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));
        logger.info("Postulante encontrado: {}", postulante.getNumeroDocumento());
        
        // 2. Guardar estado anterior
        EstadoPostulante estadoAnterior = postulante.getEstado();
        
        try {
            // 3. Crear Usuario con rol SOCIO
            Rol rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new RuntimeException("Rol SOCIO no encontrado"));
            logger.info("Rol SOCIO obtenido");
            
            // Generar contraseña temporal (por ahora: 123456)
            String passwordTemporal = "123456";
            String passwordHash = passwordEncoder.encode(passwordTemporal);
            
            Usuario nuevoUsuario = Usuario.builder()
                    .rol(rolSocio)
                    .dni(postulante.getNumeroDocumento())
                    .nombres(postulante.getNombres() != null ? postulante.getNombres() : "")
                    .apellidoPaterno(postulante.getApellidoPaterno() != null ? postulante.getApellidoPaterno() : "")
                    .apellidoMaterno(postulante.getApellidoMaterno() != null ? postulante.getApellidoMaterno() : "")
                    .correoElectronico(postulante.getCorreoElectronico())
                    .username(postulante.getNumeroDocumento()) // DNI como username
                    .password(passwordHash)
                    .estado("ACTIVO") // Bug 5 fix: valor en mayúsculas según el flujo del sistema
                    .build();
            
            Usuario usuarioCreado = usuarioRepository.save(nuevoUsuario);
            logger.info("Usuario creado exitosamente - ID: {}", usuarioCreado.getId());
            
            // 4. Crear Socio
            // Bug 4 fix: comparación case-insensitive con el valor del enum NAUTICO
            String tipoSocio = postulante.getTipoInteres() != null
                    ? ("NAUTICO".equalsIgnoreCase(postulante.getTipoInteres()) ? "Nautico" : "Social")
                    : "Social";
            
            Socio nuevoSocio = Socio.builder()
                    .idPostulante(postulante.getId())
                    .idUsuario(usuarioCreado.getId())
                    .tipoSocio(tipoSocio)
                    .estadoSocio("activo")
                    .fechaActivacion(LocalDate.now())
                    .build();
            
            Socio socioCreado = socioRepository.save(nuevoSocio);
            logger.info("Socio creado exitosamente - ID: {}", socioCreado.getId());
            
            // 5. Cambiar estado postulante a APROBADO
            postulante.setEstado(EstadoPostulante.APROBADO);
            postulanteRepository.save(postulante);
            logger.info("Estado postulante cambiado a APROBADO");
            
            // 6. Registrar en historial
            HistorialEstadoPostulante historial = HistorialEstadoPostulante.builder()
                    .idPostulante(postulante.getId())
                    .idJefe(idJefe)
                    .fechaCambio(LocalDate.now())
                    .estadoAnterior(estadoAnterior.toString())
                    .estadoNuevo("APROBADO")
                    .motivo("Postulante aprobado - Creacion de Socio y Usuario")
                    .build();
            
            historialRepository.save(historial);
            logger.info("Historial registrado exitosamente");
            
            logger.info("======= APROBACIÓN COMPLETADA EXITOSAMENTE =======");
            
        } catch (Exception e) {
            logger.error("Error durante la aprobación del postulante: {}", e.getMessage(), e);
            throw new RuntimeException("Error al aprobar postulante: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void rechazarPostulante(Integer idPostulante, Integer idJefe, String motivo) {
        logger.info("======= INICIANDO RECHAZO DE POSTULANTE =======");
        logger.info("Postulante ID: {}, Jefe ID: {}, Motivo: {}", idPostulante, idJefe, motivo);
        
        try {
            // 1. Buscar postulante
            Postulante postulante = postulanteRepository.findById(idPostulante)
                    .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));
            logger.info("Postulante encontrado: {}", postulante.getNumeroDocumento());
            
            // 2. Guardar estado anterior
            EstadoPostulante estadoAnterior = postulante.getEstado();
            
            // 3. Cambiar estado a RECHAZADO
            postulante.setEstado(EstadoPostulante.RECHAZADO);
            postulanteRepository.save(postulante);
            logger.info("Estado postulante cambiado a RECHAZADO");
            
            // 4. Registrar en historial con motivo
            HistorialEstadoPostulante historial = HistorialEstadoPostulante.builder()
                    .idPostulante(postulante.getId())
                    .idJefe(idJefe)
                    .fechaCambio(LocalDate.now())
                    .estadoAnterior(estadoAnterior.toString())
                    .estadoNuevo("RECHAZADO")
                    .motivo(motivo)
                    .build();
            
            historialRepository.save(historial);
            logger.info("Historial registrado exitosamente");
            
            logger.info("======= RECHAZO COMPLETADO EXITOSAMENTE =======");
            
        } catch (Exception e) {
            logger.error("Error durante el rechazo del postulante: {}", e.getMessage(), e);
            throw new RuntimeException("Error al rechazar postulante: " + e.getMessage());
        }
    }

    // =====================================================
    // MÉTODOS PRIVADOS HELPER
    // =====================================================

    /**
     * Construye un DTO PostulanteConDeudas a partir de una entidad Postulante
     * Aplica SOLID: Reutilización de lógica
     */
    private PostulanteConDeudasDTO construirPostulanteConDeudas(Postulante postulante) {
        // Bug 2 fix: consultar deudas en BD local por idPostulante
        List<DeudaExternaDTO> deudas = deudaExternaService.obtenerDeudasPorPostulante(
                postulante.getId()
        );
        
        String clasificacion = deudaExternaService.clasificarPostulante(deudas);
        
        return PostulanteConDeudasDTO.builder()
                .idPostulante(postulante.getId())
                .tipoDocumento(postulante.getTipoDocumento().toString())
                .numeroDocumento(postulante.getNumeroDocumento())
                .nombres(postulante.getNombres())
                .apellidoPaterno(postulante.getApellidoPaterno())
                .apellidoMaterno(postulante.getApellidoMaterno())
                .razonSocial(postulante.getRazonSocial())
                .correoElectronico(postulante.getCorreoElectronico())
                .telefono(postulante.getTelefono())
                .direccion(postulante.getDireccion())
                .fechaNacimiento(postulante.getFechaNacimiento())
                .tipoInteres(postulante.getTipoInteres())
                .fechaRegistro(postulante.getFechaRegistro())
                .estadoPostulacion(postulante.getEstado().toString())
                .deudas(deudas)
                .clasificacion(clasificacion)
                .build();
    }
}
