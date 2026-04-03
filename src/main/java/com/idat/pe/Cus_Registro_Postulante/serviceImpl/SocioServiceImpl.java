package com.idat.pe.Cus_Registro_Postulante.serviceImpl;

import com.idat.pe.Cus_Registro_Postulante.dto.ConsultaEstadoDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.SocioAprobadoDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.EstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.entity.Rol;
import com.idat.pe.Cus_Registro_Postulante.entity.Socio;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.RolRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.SocioRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import com.idat.pe.Cus_Registro_Postulante.service.EmailService;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SocioServiceImpl implements SocioService {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private PostulanteRepository postulanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Lazy
    private PostulanteService postulanteService;

    @Override
    @Transactional(readOnly = true)
    public List<SocioAprobadoDTO> listarSociosAprobados() {
        return socioRepository.findByEstadoSocioConPostulante("aprobado")
                .stream()
                .map(this::mapearSocioAprobado)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> generarCuentaSocio(Integer socioId) {
        Socio socio = socioRepository.findById(socioId)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        if (socio.getUsuario() != null) {
            throw new RuntimeException("El socio ya tiene cuenta generada");
        }

        Postulante postulante = socio.getPostulante();
        if (postulante == null) {
            throw new RuntimeException("No se encontró el postulante asociado al socio");
        }

        Rol rolSocio = rolRepository.findByNombre("SOCIO")
                .orElseThrow(() -> new RuntimeException("Rol SOCIO no encontrado"));

        String correo = postulante.getCorreoElectronico();
        String passwordTemporal = postulante.getTelefono(); // Usar teléfono como password

        Usuario usuario = usuarioRepository.findByUsernameOrCorreoElectronico(correo, correo)
                .orElseGet(() -> {
                    Usuario nuevo = Usuario.builder()
                            .username(correo)
                            .password(passwordEncoder.encode(passwordTemporal))
                            .correoElectronico(correo)
                            .rol(rolSocio)
                            .estadoUsuario(true)
                            .build();
                    return usuarioRepository.save(nuevo);
                });

        socio.setUsuario(usuario);
        socio.setEstadoSocio("activo");
        socioRepository.save(socio);

        emailService.enviarCredenciales(correo, correo, passwordTemporal);

        return Map.of(
                "mensaje", "Cuenta generada correctamente",
                "usuario", correo,
                "socioId", socio.getId(),
                "estado", "activo"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaEstadoDTO consultarEstadoPublico(String numeroDocumento) {
        Postulante postulante = postulanteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("No se encontró información para el documento ingresado"));

        String estadoPostulacion = postulante.getEstado().name().toLowerCase();
        Socio socio = socioRepository.findByPostulante(postulante).orElse(null);
        boolean tieneAcceso = socio != null && socio.getUsuario() != null;

        String estado = (socio != null && socio.getEstadoSocio() != null)
                ? socio.getEstadoSocio().toLowerCase()
                : estadoPostulacion;

        String mensajeEstado = construirMensajeEstado(postulante.getEstado(), tieneAcceso);
        String nombre = construirNombreCompleto(postulante);
        String motivoRechazo = null;
        if (postulante.getEstado() == EstadoPostulante.RECHAZADO) {
            motivoRechazo = postulanteService.obtenerUltimoMotivoRechazo(postulante.getId());
        }

        return ConsultaEstadoDTO.builder()
                .nombre(nombre)
                .numeroDocumento(postulante.getNumeroDocumento())
                .email(postulante.getCorreoElectronico())
                .estado(estado)
                .tieneAcceso(tieneAcceso)
                .mensajeEstado(mensajeEstado)
                .motivoRechazo(motivoRechazo)
                .build();
    }

    private SocioAprobadoDTO mapearSocioAprobado(Socio socio) {
        Postulante postulante = socio.getPostulante();
        return SocioAprobadoDTO.builder()
                .socioId(socio.getId())
                .nombre(construirNombreCompleto(postulante))
                .numeroDocumento(postulante.getNumeroDocumento())
                .email(postulante.getCorreoElectronico())
                .estado(socio.getEstadoSocio())
                .tieneAcceso(socio.getUsuario() != null)
                .build();
    }

    private String construirNombreCompleto(Postulante postulante) {
        if (postulante.getNombres() != null && !postulante.getNombres().isBlank()) {
            String apellidoPaterno = postulante.getApellidoPaterno() == null ? "" : postulante.getApellidoPaterno();
            String apellidoMaterno = postulante.getApellidoMaterno() == null ? "" : postulante.getApellidoMaterno();
            return (postulante.getNombres() + " " + apellidoPaterno + " " + apellidoMaterno).trim();
        }
        return postulante.getRazonSocial() != null ? postulante.getRazonSocial() : "Postulante";
    }

    private String construirMensajeEstado(EstadoPostulante estadoPostulante, boolean tieneAcceso) {
        if (tieneAcceso) {
            return "Tu cuenta ya fue generada. Puedes iniciar sesión en el sistema.";
        }

        if (estadoPostulante == EstadoPostulante.APROBADO) {
            return "Tu solicitud fue aprobada. Acércate a oficina para finalizar la habilitación de cuenta.";
        }
        if (estadoPostulante == EstadoPostulante.PENDIENTE) {
            return "Tu solicitud está en revisión. Te contactaremos cuando finalice la evaluación.";
        }
        if (estadoPostulante == EstadoPostulante.RECHAZADO) {
            return "Tu solicitud fue observada o rechazada. Revisa el motivo e inicia subsanación.";
        }
        if (estadoPostulante == EstadoPostulante.SUBSANADO) {
            return "Recibimos tu subsanación. Tu solicitud volvió a evaluación.";
        }

        return "Estado no disponible por el momento.";
    }
}
