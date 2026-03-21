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
import java.util.ArrayList;
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
    private UbicacionGeograficaRepository ubicacionRepo;

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

        Integer idUbicacion = getOrCreateUbicacion(dto);
        if (idUbicacion != null) {
            dto.setIdCiudad(idUbicacion);
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
        
        Integer idUbicacion = getOrCreateUbicacion(dto);
        if (idUbicacion != null) {
            postulante.setIdCiudad(idUbicacion);
        } else {
            postulante.setIdCiudad(dto.getIdCiudad());
        }
        
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
            postulante.setFechaNacimiento(dto.getFechaNacimiento());
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

    private Integer getOrCreateUbicacion(RegistroPostulanteDTO dto) {
        if (dto.getPais() == null || dto.getPais().trim().isEmpty()) return null;

        UbicacionGeografica pais = getOrCreate(dto.getPais().trim(), "PAIS", null);
        
        if (dto.getDepartamento() == null || dto.getDepartamento().trim().isEmpty()) return pais.getId();
        UbicacionGeografica dep = getOrCreate(dto.getDepartamento().trim(), "DEPARTAMENTO", pais);
        
        if (dto.getProvincia() == null || dto.getProvincia().trim().isEmpty()) return dep.getId();
        UbicacionGeografica prov = getOrCreate(dto.getProvincia().trim(), "PROVINCIA", dep);
        
        if (dto.getDistrito() == null || dto.getDistrito().trim().isEmpty()) return prov.getId();
        UbicacionGeografica dist = getOrCreate(dto.getDistrito().trim(), "DISTRITO", prov);
        
        return dist.getId();
    }

    private UbicacionGeografica getOrCreate(String nombre, String tipo, UbicacionGeografica padre) {
        if (padre == null) {
            return ubicacionRepo.findByNombreAndTipoUbicacionAndPadreIsNull(nombre, tipo)
                .orElseGet(() -> ubicacionRepo.save(UbicacionGeografica.builder()
                    .nombre(nombre)
                    .tipoUbicacion(tipo)
                    .padre(null)
                    .estado("activo")
                    .build()));
        } else {
            return ubicacionRepo.findByNombreAndTipoUbicacionAndPadre(nombre, tipo, padre)
                .orElseGet(() -> ubicacionRepo.save(UbicacionGeografica.builder()
                    .nombre(nombre)
                    .tipoUbicacion(tipo)
                    .padre(padre)
                    .estado("activo")
                    .build()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulanteConDeudasDTO> obtenerPostulantesPendientesConDeudas() {
        logger.info("Obteniendo postulantes pendientes/subsanados");
        
        // ArrayList mutable para evitar UnsupportedOperationException
        List<Postulante> postulantes = new ArrayList<>(postulanteRepository.findByEstado(EstadoPostulante.PENDIENTE));
        postulantes.addAll(postulanteRepository.findByEstado(EstadoPostulante.SUBSANADO));
        
        logger.info("Se encontraron {} postulantes pendientes/subsanados", postulantes.size());
        
        // Para el listado NO consultamos deudas — eso se hace al seleccionar un postulante
        return postulantes.stream()
                .map(this::mapearPostulanteBasico)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostulanteConDeudasDTO obtenerPostulanteConDeudasDetalle(Integer idPostulante) {
        logger.info("Obteniendo detalles de postulante ID: {}", idPostulante);
        
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));
        
        // Para el detalle sí consultamos deudas en BD local (con protección)
        List<DeudaExternaDTO> deudas = List.of();
        String clasificacion = "Sin datos";
        try {
            deudas = deudaExternaService.obtenerDeudasPorPostulante(postulante.getId());
            clasificacion = deudaExternaService.clasificarPostulante(deudas);
        } catch (Exception e) {
            logger.warn("No se pudieron obtener deudas locales para postulante {}: {}", idPostulante, e.getMessage());
        }
        
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
                .fechaNacimiento(postulante.getFechaNacimiento() != null ? postulante.getFechaNacimiento().toString() : null)
                .tipoInteres(postulante.getTipoInteres())
                .fechaRegistro(postulante.getFechaRegistro() != null ? postulante.getFechaRegistro().toString() : null)
                .estadoPostulacion(postulante.getEstado() != null ? postulante.getEstado().toString() : "PENDIENTE")
                .deudas(deudas)
                .clasificacion(clasificacion)
                .build();
    }

    @Override
    @Transactional
    public void aprobarPostulante(Integer idPostulante, Integer idJefe) {
        logger.info("======= INICIANDO APROBACIÓN DE POSTULANTE =======");
        logger.info("Postulante ID: {}, Jefe ID: {}", idPostulante, idJefe);
        
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));
        logger.info("Postulante encontrado: {}", postulante.getNumeroDocumento());
        
        EstadoPostulante estadoAnterior = postulante.getEstado();
        
        try {
            Rol rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new RuntimeException("Rol SOCIO no encontrado"));
            
            String passwordHash = passwordEncoder.encode("123456");
            
            Usuario nuevoUsuario = Usuario.builder()
                    .rol(rolSocio)
                    .dni(postulante.getNumeroDocumento())
                    .nombres(postulante.getNombres() != null ? postulante.getNombres() : "")
                    .apellidoPaterno(postulante.getApellidoPaterno() != null ? postulante.getApellidoPaterno() : "")
                    .apellidoMaterno(postulante.getApellidoMaterno() != null ? postulante.getApellidoMaterno() : "")
                    .correoElectronico(postulante.getCorreoElectronico())
                    .username(postulante.getNumeroDocumento())
                    .password(passwordHash)
                    .estado("activo")
                    .build();
            
            Usuario usuarioCreado = usuarioRepository.save(nuevoUsuario);
            logger.info("Usuario creado - ID: {}", usuarioCreado.getId());
            
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
            
            socioRepository.save(nuevoSocio);
            
            postulante.setEstado(EstadoPostulante.APROBADO);
            postulanteRepository.save(postulante);
            
            HistorialEstadoPostulante historial = HistorialEstadoPostulante.builder()
                    .idPostulante(postulante.getId())
                    .idJefe(idJefe)
                    .fechaCambio(LocalDate.now())
                    .estadoAnterior(estadoAnterior.toString())
                    .estadoNuevo("APROBADO")
                    .motivo("Postulante aprobado - Creacion de Socio y Usuario")
                    .build();
            
            historialRepository.save(historial);
            logger.info("======= APROBACIÓN COMPLETADA =======");
            
        } catch (Exception e) {
            logger.error("Error al aprobar postulante: {}", e.getMessage(), e);
            throw new RuntimeException("Error al aprobar postulante: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void rechazarPostulante(Integer idPostulante, Integer idJefe, String motivo) {
        logger.info("======= INICIANDO RECHAZO DE POSTULANTE =======");
        
        try {
            Postulante postulante = postulanteRepository.findById(idPostulante)
                    .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));
            
            EstadoPostulante estadoAnterior = postulante.getEstado();
            
            postulante.setEstado(EstadoPostulante.RECHAZADO);
            postulanteRepository.save(postulante);
            
            HistorialEstadoPostulante historial = HistorialEstadoPostulante.builder()
                    .idPostulante(postulante.getId())
                    .idJefe(idJefe)
                    .fechaCambio(LocalDate.now())
                    .estadoAnterior(estadoAnterior.toString())
                    .estadoNuevo("RECHAZADO")
                    .motivo(motivo)
                    .build();
            
            historialRepository.save(historial);
            logger.info("======= RECHAZO COMPLETADO =======");
            
        } catch (Exception e) {
            logger.error("Error al rechazar postulante: {}", e.getMessage(), e);
            throw new RuntimeException("Error al rechazar postulante: " + e.getMessage());
        }
    }

    // =====================================================
    // MÉTODOS PRIVADOS HELPER
    // =====================================================

    /**
     * Mapea un Postulante a DTO básico SIN consultar deudas.
     * Usado en el listado para evitar queries innecesarias.
     */
    private PostulanteConDeudasDTO mapearPostulanteBasico(Postulante postulante) {
        return PostulanteConDeudasDTO.builder()
                .idPostulante(postulante.getId())
                .tipoDocumento(postulante.getTipoDocumento() != null ? postulante.getTipoDocumento().toString() : "DNI")
                .numeroDocumento(postulante.getNumeroDocumento())
                .nombres(postulante.getNombres())
                .apellidoPaterno(postulante.getApellidoPaterno())
                .apellidoMaterno(postulante.getApellidoMaterno())
                .razonSocial(postulante.getRazonSocial())
                .correoElectronico(postulante.getCorreoElectronico())
                .telefono(postulante.getTelefono())
                .direccion(postulante.getDireccion())
                .fechaNacimiento(postulante.getFechaNacimiento() != null ? postulante.getFechaNacimiento().toString() : null)
                .tipoInteres(postulante.getTipoInteres())
                .fechaRegistro(postulante.getFechaRegistro() != null ? postulante.getFechaRegistro().toString() : null)
                .estadoPostulacion(postulante.getEstado() != null ? postulante.getEstado().toString() : "PENDIENTE")
                .deudas(null)
                .clasificacion(null)
                .build();
    }
}
