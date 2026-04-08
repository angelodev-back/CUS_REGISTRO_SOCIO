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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private HistorialEstadoPostulanteRepository historialRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private InformeAdmisionRepository informeAdmisionRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PostulanteDTO registrarPostulante(RegistroPostulanteDTO dto) {
        // Validaciones Manuales de Regla de Negocio
        if (dto.getTipoDocumento() == null || dto.getNumeroDocumento() == null) {
            throw new RuntimeException("El tipo y número de documento son obligatorios.");
        }

        if (!dto.getNumeroDocumento().matches("\\d+")) {
            throw new RuntimeException("El número de documento solo debe contener dígitos.");
        }
        
        if ("DNI".equalsIgnoreCase(dto.getTipoDocumento()) && dto.getNumeroDocumento().length() != 8) {
            throw new RuntimeException("El DNI debe tener exactamente 8 dígitos.");
        }
        
        if ("RUC".equalsIgnoreCase(dto.getTipoDocumento()) && dto.getNumeroDocumento().length() != 11) {
            throw new RuntimeException("El RUC debe tener exactamente 11 dígitos.");
        }

        if ("RUC".equalsIgnoreCase(dto.getTipoDocumento()) && !dto.getNumeroDocumento().matches("^(10|20)\\d{9}$")) {
            throw new RuntimeException("El RUC debe iniciar con 10 o 20 y tener 11 dígitos.");
        }

        dto.setTelefono(validarYNormalizarTelefono(dto.getTipoTelefono(), dto.getTelefono()));

        if ("DNI".equalsIgnoreCase(dto.getTipoDocumento())) {
            if (dto.getNombre() == null || dto.getNombre().isBlank() || dto.getApellidoPaterno() == null || dto.getApellidoPaterno().isBlank()) {
                throw new RuntimeException("Los nombres y el apellido paterno son obligatorios para DNI.");
            }
        } else if ("RUC".equalsIgnoreCase(dto.getTipoDocumento())) {
            if (dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) {
                throw new RuntimeException("La razón social es obligatoria para RUC.");
            }
        }

        Optional<Postulante> existingDoc = postulanteRepository.findByNumeroDocumento(dto.getNumeroDocumento());
        if (existingDoc.isPresent()) {
            if (existingDoc.get().getEstado() == EstadoPostulante.APROBADO) {
                throw new RuntimeException("Usted ya se encuentra registrado en el sistema.");
            } else {
                throw new RuntimeException("Usted se encuentra en proceso de validación para su cuenta.");
            }
        }

        Optional<Postulante> existingEmail = postulanteRepository.findByCorreoElectronico(dto.getCorreo());
        if (existingEmail.isPresent()) {
            if (existingEmail.get().getEstado() == EstadoPostulante.APROBADO) {
                throw new RuntimeException("Usted ya se encuentra registrado en el sistema con este correo.");
            } else {
                throw new RuntimeException("Usted se encuentra en proceso de validación para su cuenta con este correo.");
            }
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

        String telefonoNormalizado = validarYNormalizarTelefono(dto.getTipoTelefono(), dto.getTelefono());

        postulante.setTipoDocumento(TipoDocumento.valueOf(dto.getTipoDocumento()));
        postulante.setNumeroDocumento(dto.getNumeroDocumento());
        postulante.setCorreoElectronico(dto.getCorreo());
        postulante.setTelefono(telefonoNormalizado);
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
        registrarHistorial(actualizado, estadoAnterior, "Cambio de estado manual", null, null);
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
        ExternalDebtResponseDTO ext = null;
        try {
            ext = deudaExternaService.obtenerDatosExternos(
                    postulante.getTipoDocumento().toString(),
                    postulante.getNumeroDocumento()
            );

            // Siempre tomar deudas desde el servicio API-only para conservar el estado temporal de verificación.
            deudas = deudaExternaService.obtenerDeudasPorPostulante(postulante.getId());
            clasificacion = (ext != null && ext.getClasificacionSugerida() != null)
                    ? ext.getClasificacionSugerida()
                    : deudaExternaService.clasificarPostulante(deudas);
        } catch (Exception e) {
            logger.warn("No se pudieron obtener deudas para postulante {}: {}", idPostulante, e.getMessage());
            deudas = deudaExternaService.obtenerDeudasPorPostulante(postulante.getId());
            clasificacion = deudaExternaService.clasificarPostulante(deudas);
        }
        
        PostulanteConDeudasDTO dto = mapearPostulanteBasico(postulante);
        if ((dto.getCiudad() == null || dto.getCiudad().isBlank()) && ext != null) {
            dto.setCiudad(obtenerCiudadExterna(ext));
        }
        dto.setDeudas(deudas);
        dto.setClasificacion(clasificacion);
        return dto;
    }

    @Override
    @Transactional
    public void aprobarPostulante(Integer idPostulante, Integer idEmpleado) {
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
        
        EstadoPostulante estadoAnterior = postulante.getEstado();
        
        try {
            Socio socio = socioRepository.findByPostulante(postulante)
                .orElse(Socio.builder()
                    .postulante(postulante)
                    .build());

            socio.setTipoSocio("NAUTICO".equalsIgnoreCase(postulante.getTipoInteres()) ? "Nautico" : "Social");
            socio.setEstadoSocio("aprobado");
            socio.setFechaActivacion(socio.getFechaActivacion() == null ? LocalDate.now() : socio.getFechaActivacion());
            socioRepository.save(socio);
            
            postulante.setEstado(EstadoPostulante.APROBADO);
            postulanteRepository.save(postulante);
            
            // Generar cuenta de usuario al aprobar
            Rol rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new RuntimeException("Rol SOCIO no encontrado"));

            String doc = postulante.getNumeroDocumento();
            String passwordLast4 = doc.length() >= 4 ? doc.substring(doc.length() - 4) : doc;
            String passwordTemporal = passwordLast4 + "*!";

            // Buscar si ya existe el usuario para actualizarlo o crear uno nuevo
            Usuario usuario = usuarioRepository.findByUsername(postulante.getCorreoElectronico())
                    .orElse(new Usuario());
            
            usuario.setUsername(postulante.getCorreoElectronico());
            usuario.setPassword(passwordEncoder.encode(passwordTemporal));
            usuario.setCorreoElectronico(postulante.getCorreoElectronico());
            usuario.setRol(rolSocio);
            usuario.setEstadoUsuario(true);
            
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            
            // Vincular Usuario con Socio
            socio.setUsuario(usuarioGuardado);
            socio.setEstadoSocio("activo");
            socioRepository.save(socio);
            
            // Email de notificación sín código (opcional, pero silenciado por requerimiento)
            /* 
            try {
                emailService.enviarCredenciales(
                    postulante.getCorreoElectronico(), 
                    postulante.getCorreoElectronico(), 
                    passwordTemporal
                );
            } catch (Exception e) {
                logger.error("No se pudo enviar el correo de credenciales: {}", e.getMessage());
            }
            */

            // Generar Historial con Informe
            InformeAdmision informe = informeAdmisionRepository.findByPostulante_Id(postulante.getId()).orElse(null);
            registrarHistorial(postulante, estadoAnterior, "Cuenta socio activada (Regla: DNI-4+*!)", idEmpleado, informe);

        } catch (Exception e) {
            throw new RuntimeException("Error al aprobar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void rechazarPostulante(Integer idPostulante, Integer idEmpleado, String motivo) {
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
        
        EstadoPostulante estadoAnterior = postulante.getEstado();
        postulante.setEstado(EstadoPostulante.RECHAZADO);
        postulanteRepository.save(postulante);
        
        // Generar Historial con Informe
        InformeAdmision informe = informeAdmisionRepository.findByPostulante_Id(postulante.getId()).orElse(null);
        registrarHistorial(postulante, estadoAnterior, motivo, idEmpleado, informe);
        
        emailService.enviarNotificacionSubsanacion(postulante.getCorreoElectronico(), motivo);
    }

    @Override
    @Transactional
    public void subsanarPostulante(Integer idPostulante, RegistroPostulanteDTO dto) {
        Postulante postulante = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
        
        EstadoPostulante estadoAnterior = postulante.getEstado();
        String telefonoNormalizado = validarYNormalizarTelefono(dto.getTipoTelefono(), dto.getTelefono());
        
        postulante.setCorreoElectronico(dto.getCorreo());
        postulante.setTelefono(telefonoNormalizado);
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
        
        registrarHistorial(postulante, estadoAnterior, "Subsanación enviada por el postulante", null, null);
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

    @Override
    @Transactional(readOnly = true)
    public String obtenerUltimoMotivoRechazo(Integer idPostulante) {
        return historialRepository.findByPostulante_IdOrderByFechaCambioDesc(idPostulante).stream()
                .filter(h -> "rechazado".equalsIgnoreCase(h.getEstadoNuevo()))
                .map(HistorialEstadoPostulante::getMotivo)
                .findFirst()
                .orElse("No se especificó un motivo.");
    }

    private PostulanteConDeudasDTO mapearPostulanteBasicoSync(Postulante p, List<ExternalDebtResponseDTO> extData) {
        ExternalDebtResponseDTO ext = extData.stream()
            .filter(e -> e.getTipoDocumento() != null && e.getTipoDocumento().equalsIgnoreCase(p.getTipoDocumento().name()) 
                      && e.getNumeroDocumento() != null && e.getNumeroDocumento().equals(p.getNumeroDocumento()))
            .findFirst()
            .orElse(null);
        
        String clasificacion = (ext != null) ? ext.getClasificacionSugerida() : "Sin datos";
        
        PostulanteConDeudasDTO dto = mapearPostulanteBasico(p);
        if ((dto.getCiudad() == null || dto.getCiudad().isBlank()) && ext != null) {
            dto.setCiudad(obtenerCiudadExterna(ext));
        }
        dto.setClasificacion(clasificacion);
        return dto;
    }

    private void registrarHistorial(Postulante p, EstadoPostulante anterior, String motivo, Integer idEmpleado, InformeAdmision informe) {
        Empleado empleado = null;
        if (idEmpleado != null && idEmpleado > 0) {
            empleado = empleadoRepository.findById(idEmpleado).orElse(null);
        }

        HistorialEstadoPostulante h = HistorialEstadoPostulante.builder()
                .postulante(p)
                .empleado(empleado)
                .informe(informe)
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
                .ciudad(p.getCiudad())
                .direccion(p.getDireccion())
                .fechaNacimiento(p.getFechaNacimiento() != null ? p.getFechaNacimiento().toString() : null)
                .tipoInteres(p.getTipoInteres())
                .fechaRegistro(p.getFechaRegistro() != null ? p.getFechaRegistro().toString() : null)
                .estadoPostulacion(p.getEstado().name().toLowerCase())
                .build();
    }

    private String obtenerCiudadExterna(ExternalDebtResponseDTO ext) {
        if (ext == null) {
            return null;
        }
        if (ext.getCiudad() != null && !ext.getCiudad().isBlank()) {
            return ext.getCiudad().trim();
        }
        if (ext.getDistrito() != null && !ext.getDistrito().isBlank()) {
            return ext.getDistrito().trim();
        }
        if (ext.getProvincia() != null && !ext.getProvincia().isBlank()) {
            return ext.getProvincia().trim();
        }
        if (ext.getDepartamento() != null && !ext.getDepartamento().isBlank()) {
            return ext.getDepartamento().trim();
        }
        return null;
    }

    private Integer obtenerIdEmpleadoActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        
        // Obtener usuario actual
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(auth.getName());
        if (usuarioOpt.isEmpty()) {
            return null;
        }
        
        // Obtener empleado asociado
        Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuario(usuarioOpt.get());
        return empleadoOpt.map(Empleado::getId).orElse(null);
    }

    private String validarYNormalizarTelefono(String tipoTelefono, String telefono) {
        String telefonoNormalizado = telefono != null ? telefono.trim().replaceAll("\\s+", "") : "";
        String tipoNormalizado = tipoTelefono != null ? tipoTelefono.trim().toUpperCase() : "";

        if (tipoNormalizado.isBlank()) {
            if (telefonoNormalizado.matches("^0\\d{8}$")) {
                tipoNormalizado = "FIJO";
            } else if (telefonoNormalizado.matches("^\\+519\\d{8}$")) {
                tipoNormalizado = "CELULAR";
            }
        }

        if (tipoNormalizado.isBlank()) {
            throw new RuntimeException("Debe seleccionar si el teléfono es FIJO o CELULAR.");
        }

        if (!"FIJO".equals(tipoNormalizado) && !"CELULAR".equals(tipoNormalizado)) {
            throw new RuntimeException("El tipo de teléfono debe ser FIJO o CELULAR.");
        }

        if (telefonoNormalizado.isBlank()) {
            throw new RuntimeException("El teléfono es obligatorio.");
        }

        if (telefonoNormalizado.length() > 15) {
            throw new RuntimeException("El teléfono no debe exceder 15 caracteres.");
        }

        if ("FIJO".equals(tipoNormalizado)) {
            if (!telefonoNormalizado.matches("^0\\d{8}$")) {
                throw new RuntimeException("Para teléfono fijo, ingrese 9 dígitos iniciando con 0.");
            }
            return telefonoNormalizado;
        }

        if (!telefonoNormalizado.matches("^\\+519\\d{8}$")) {
            throw new RuntimeException("Para celular, ingrese el formato +51 seguido de un número que inicie en 9 (ejemplo: +51987654321).");
        }
        return telefonoNormalizado;
    }
}
