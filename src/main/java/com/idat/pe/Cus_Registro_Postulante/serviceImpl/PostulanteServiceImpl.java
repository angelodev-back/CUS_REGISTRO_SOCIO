package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.DeudaExternaDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.ExternalDebtResponseDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.*;
import com.idat.pe.Cus_Registro_Postulante.mapper.PostulanteMapper;
import com.idat.pe.Cus_Registro_Postulante.repository.*;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.service.DeudaExternaService;
import com.idat.pe.Cus_Registro_Postulante.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de postulantes con sincronización externa y búsqueda
 */
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
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        postulante.setEstado(EstadoPostulante.PENDIENTE);
        postulante.setFechaRegistro(LocalDate.now());
        
        Postulante guardado = postulanteRepository.save(postulante);
        return postulanteMapper.toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulanteDTO> listarPostulantes() {
        return postulanteRepository.findAll().stream()
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
        postulante.setCiudad(dto.getCiudad());
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

        EstadoPostulante estadoAnterior = postulante.getEstado();
        try {
            postulante.setEstado(EstadoPostulante.valueOf(nuevoEstado.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado no válido: " + nuevoEstado);
        }

        Postulante actualizado = postulanteRepository.save(postulante);
        registrarHistorial(actualizado, estadoAnterior, "Cambio de estado manual", obtenerIdUsuarioActual());
        return postulanteMapper.toDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulanteConDeudasDTO> obtenerPostulantesPendientesConDeudas() {
        List<Postulante> postulantes = new ArrayList<>();
        postulantes.addAll(postulanteRepository.findByEstado(EstadoPostulante.PENDIENTE));
        postulantes.addAll(postulanteRepository.findByEstado(EstadoPostulante.SUBSANADO));
        
        List<ExternalDebtResponseDTO> extData = deudaExternaService.obtenerTodasLasDeudas();
        
        return postulantes.stream()
                .map(p -> mapearPostulanteBasicoSync(p, extData))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostulanteConDeudasDTO obtenerPostulanteConDeudasDetalle(Integer idPostulante) {
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado con id: " + idPostulante));
        
        List<DeudaExternaDTO> deudas = new ArrayList<>();
        String clasificacion = "Sin datos";
        try {
            // SYNC REAL-TIME
            ExternalDebtResponseDTO ext = deudaExternaService.obtenerDatosExternos(
                postulante.getTipoDocumento().toString(), 
                postulante.getNumeroDocumento()
            );
            
            if (ext != null) {
                deudas = ext.getDeudas();
                clasificacion = ext.getClasificacionSugerida();
            } else {
                deudas = deudaExternaService.obtenerDeudasPorPostulante(postulante.getId());
                clasificacion = deudaExternaService.clasificarPostulante(deudas);
            }
        } catch (Exception e) {
            logger.warn("No se pudieron obtener deudas para postulante {}: {}", idPostulante, e.getMessage());
            deudas = deudaExternaService.obtenerDeudasPorPostulante(postulante.getId());
            clasificacion = deudaExternaService.clasificarPostulante(deudas);
        }
        
        PostulanteConDeudasDTO dto = mapearPostulanteBasico(postulante);
        dto.setDeudas(deudas);
        dto.setClasificacion(clasificacion);
        return dto;
    }

    @Override
    @Transactional
    public void aprobarPostulante(Integer idPostulante, Integer idJefe) {
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
        
        EstadoPostulante estadoAnterior = postulante.getEstado();
        
        try {
            Rol rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new RuntimeException("Rol SOCIO no encontrado"));
            
            boolean isRuc = postulante.getTipoDocumento() == TipoDocumento.RUC;
            
            Usuario usuario = Usuario.builder()
                    .rol(rolSocio)
                    .dni(postulante.getNumeroDocumento())
                    .nombres(isRuc ? postulante.getRazonSocial() : postulante.getNombres())
                    .apellidoPaterno(isRuc ? " " : postulante.getApellidoPaterno())
                    .apellidoMaterno(isRuc ? " " : postulante.getApellidoMaterno())
                    .correoElectronico(postulante.getCorreoElectronico())
                    .username(postulante.getNumeroDocumento())
                    .password(passwordEncoder.encode("123456"))
                    .estado("activo")
                    .build();
            
            Usuario usuarioCreado = usuarioRepository.save(usuario);
            
            Socio socio = Socio.builder()
                    .postulante(postulante)
                    .usuario(usuarioCreado)
                    .tipoSocio("NAUTICO".equalsIgnoreCase(postulante.getTipoInteres()) ? "Nautico" : "Social")
                    .estadoSocio("activo")
                    .fechaActivacion(LocalDate.now())
                    .build();
            
            socioRepository.save(socio);
            
            postulante.setEstado(EstadoPostulante.APROBADO);
            postulanteRepository.save(postulante);
            
            registrarHistorial(postulante, estadoAnterior, "Postulante aprobado - Creacion de Socio", idJefe);
            
            emailService.enviarCredenciales(postulante.getCorreoElectronico(), postulante.getNumeroDocumento(), "123456");

        } catch (Exception e) {
            throw new RuntimeException("Error al aprobar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void rechazarPostulante(Integer idPostulante, Integer idJefe, String motivo) {
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
        
        EstadoPostulante estadoAnterior = postulante.getEstado();
        postulante.setEstado(EstadoPostulante.RECHAZADO);
        postulanteRepository.save(postulante);
        
        registrarHistorial(postulante, estadoAnterior, motivo, idJefe);
        
        emailService.enviarNotificacionSubsanacion(postulante.getCorreoElectronico(), motivo);
    }

    @Override
    @Transactional
    public void subsanarPostulante(Integer idPostulante, RegistroPostulanteDTO dto) {
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
        
        EstadoPostulante estadoAnterior = postulante.getEstado();
        
        postulante.setCorreoElectronico(dto.getCorreo());
        postulante.setTelefono(dto.getTelefono());
        postulante.setDireccion(dto.getDireccion());
        postulante.setCiudad(dto.getCiudad());
        postulante.setTipoInteres(dto.getTipoInteres());
        
        if (postulante.getTipoDocumento() == TipoDocumento.DNI) {
            postulante.setNombres(dto.getNombre());
            postulante.setApellidoPaterno(dto.getApellidoPaterno());
            postulante.setApellidoMaterno(dto.getApellidoMaterno());
        } else {
            postulante.setRazonSocial(dto.getRazonSocial());
        }
        
        postulante.setEstado(EstadoPostulante.SUBSANADO);
        postulanteRepository.save(postulante);
        
        registrarHistorial(postulante, estadoAnterior, "Subsanación enviada por el postulante", null);
    }

    @Override
    @Transactional(readOnly = true)
    public PostulanteDTO buscarPorNumeroDocumento(String numero) {
        return postulanteRepository.findByNumeroDocumento(numero)
                .map(postulanteMapper::toDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulanteConDeudasDTO> buscarPostulantesParaJefe(String tipoDoc, String numDoc) {
        List<Postulante> result = new ArrayList<>();
        if ((tipoDoc == null || tipoDoc.isEmpty() || "TODOS".equalsIgnoreCase(tipoDoc)) 
            && (numDoc == null || numDoc.isEmpty())) {
            return obtenerPostulantesPendientesConDeudas();
        }
        
        if (numDoc != null && !numDoc.isEmpty()) {
            if (tipoDoc != null && !tipoDoc.isEmpty() && !"TODOS".equalsIgnoreCase(tipoDoc)) {
                TipoDocumento td = "RUC".equalsIgnoreCase(tipoDoc) ? TipoDocumento.RUC : TipoDocumento.DNI;
                postulanteRepository.findByTipoDocumentoAndNumeroDocumento(td, numDoc).ifPresent(result::add);
            } else {
                postulanteRepository.findByNumeroDocumento(numDoc).ifPresent(result::add);
            }
        }
        
        List<ExternalDebtResponseDTO> extData = deudaExternaService.obtenerTodasLasDeudas();
        return result.stream()
                .map(p -> mapearPostulanteBasicoSync(p, extData))
                .collect(Collectors.toList());
    }

    private PostulanteConDeudasDTO mapearPostulanteBasicoSync(Postulante p, List<ExternalDebtResponseDTO> extData) {
        ExternalDebtResponseDTO ext = extData.stream()
            .filter(e -> e.getTipoDocumento() != null && e.getTipoDocumento().equalsIgnoreCase(p.getTipoDocumento().name()) 
                      && e.getNumeroDocumento() != null && e.getNumeroDocumento().equals(p.getNumeroDocumento()))
            .findFirst()
            .orElse(null);
        
        String clasificacion = (ext != null) ? ext.getClasificacionSugerida() : "Sin datos";
        
        PostulanteConDeudasDTO dto = mapearPostulanteBasico(p);
        dto.setClasificacion(clasificacion);
        return dto;
    }

    private void registrarHistorial(Postulante p, EstadoPostulante anterior, String motivo, Integer idJefe) {
        Usuario jefe = null;
        if (idJefe != null && idJefe > 0) {
            jefe = usuarioRepository.findById(idJefe).orElse(null);
        }

        HistorialEstadoPostulante h = HistorialEstadoPostulante.builder()
                .postulante(p)
                .jefe(jefe)
                .fechaCambio(LocalDate.now())
                .estadoAnterior(anterior != null ? anterior.name().toLowerCase() : "pendiente")
                .estadoNuevo(p.getEstado().name().toLowerCase())
                .motivo(motivo)
                .build();
        historialRepository.save(h);
    }

    private PostulanteConDeudasDTO mapearPostulanteBasico(Postulante p) {
        return PostulanteConDeudasDTO.builder()
                .idPostulante(p.getId())
                .tipoDocumento(p.getTipoDocumento().toString())
                .numeroDocumento(p.getNumeroDocumento())
                .nombres(p.getNombres())
                .apellidoPaterno(p.getApellidoPaterno())
                .apellidoMaterno(p.getApellidoMaterno())
                .razonSocial(p.getRazonSocial())
                .correoElectronico(p.getCorreoElectronico())
                .telefono(p.getTelefono())
                .direccion(p.getDireccion())
                .fechaNacimiento(p.getFechaNacimiento() != null ? p.getFechaNacimiento().toString() : null)
                .tipoInteres(p.getTipoInteres())
                .fechaRegistro(p.getFechaRegistro() != null ? p.getFechaRegistro().toString() : null)
                .estadoPostulacion(p.getEstado().name().toLowerCase())
                .build();
    }

    private Integer obtenerIdUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 0;
        }
        return usuarioRepository.findByUsername(auth.getName())
                .map(Usuario::getId)
                .orElse(0);
    }
}
