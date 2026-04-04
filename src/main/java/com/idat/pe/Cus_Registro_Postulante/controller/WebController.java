package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.Empleado;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.repository.EmpleadoRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.service.SocioService;
import com.idat.pe.Cus_Registro_Postulante.dto.ConsultaEstadoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private PostulanteService postulanteService;

    @Autowired
    private SocioService socioService;

    @Autowired
    private PostulanteRepository postulanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @GetMapping("/pre-registro")
    public String preRegistro() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // --- FLUJO REGISTRO ---
    @GetMapping("/registro")
    public String registro(Model model) {
        if (!model.containsAttribute("registroDTO")) {
            model.addAttribute("registroDTO", new RegistroPostulanteDTO());
        }
        return "registro/formulario-registro";
    }

    @GetMapping("/registro/exitoso")
    public String exitoso() {
        // Tip: El objeto 'postulante' llega aquí como FlashAttribute desde el POST.
        // Si el usuario refresca la página (F5), el atributo se pierde y será null.
        // El template registro-exitoso.html ya está preparado para manejar ese caso.
        return "registro/registro-exitoso";
    }

    // --- FLUJO JEFE ---
    @GetMapping("/jefe/dashboard")
    public String jefeDashboard(@RequestParam(required = false) String tipoDoc,
                               @RequestParam(required = false) String numDoc,
                               Model model) {
        agregarPerfilJefe(model);
        
        List<PostulanteConDeudasDTO> lista;
        if ((tipoDoc != null && !tipoDoc.isEmpty() && !"TODOS".equalsIgnoreCase(tipoDoc)) 
            || (numDoc != null && !numDoc.isEmpty())) {
            lista = postulanteService.buscarPostulantesParaJefe(tipoDoc, numDoc);
            model.addAttribute("searchTipo", tipoDoc);
            model.addAttribute("searchNum", numDoc);
        } else {
            lista = postulanteService.obtenerPostulantesPendientesConDeudas();
        }
        
        model.addAttribute("postulantes", lista);
        return "jefe/dashboard-deudas";
    }

    @PostMapping("/jefe/aprobar/{id}")
    public String aprobarPostulante(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Integer idEmpleado = obtenerIdEmpleadoActual();
            if (idEmpleado == null) {
                throw new RuntimeException("No se pudo identificar al empleado autenticado");
            }
            postulanteService.aprobarPostulante(id, idEmpleado);
            ra.addFlashAttribute("mensaje", "Postulante aprobado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jefe/dashboard";
    }

    @PostMapping("/jefe/rechazar")
    public String rechazarPostulante(@RequestParam Integer idPostulante, @RequestParam String motivo, RedirectAttributes ra) {
        try {
            Integer idEmpleado = obtenerIdEmpleadoActual();
            if (idEmpleado == null) {
                throw new RuntimeException("No se pudo identificar al empleado autenticado");
            }
            postulanteService.rechazarPostulante(idPostulante, idEmpleado, motivo);
            ra.addFlashAttribute("mensaje", "Postulante rechazado para subsanación.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jefe/dashboard";
    }

    @GetMapping("/jefe/historial")
    public String historialSolicitudes(Model model) {
        agregarPerfilJefe(model);
        
        List<com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO> todos = postulanteService.listarPostulantes();
        
        // Historial General: Incluimos todos los registros para seguimiento total
        List<com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO> historial = todos.stream()
                .peek(p -> {
                    if (p.getEstadoPostulacion() != null && p.getEstadoPostulacion().toLowerCase().contains("rechazado")) {
                        p.setMotivoRechazo(postulanteService.obtenerUltimoMotivoRechazo(p.getIdPostulante()));
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        // Calcular estadísticas reales para los cuadros del dashboard
        long countAprobados = todos.stream()
                .filter(p -> p.getEstadoPostulacion() != null && p.getEstadoPostulacion().toLowerCase().contains("aprobado"))
                .count();
        long countRechazados = todos.stream()
                .filter(p -> p.getEstadoPostulacion() != null && p.getEstadoPostulacion().toLowerCase().contains("rechazado"))
                .count();
        long countPendientes = todos.stream()
                .filter(p -> p.getEstadoPostulacion() != null && (p.getEstadoPostulacion().toLowerCase().contains("pendiente") || p.getEstadoPostulacion().toLowerCase().contains("subsanado")))
                .count();
        
        model.addAttribute("postulantes", historial);
        model.addAttribute("totalRegistros", historial.size());
        model.addAttribute("totalAprobados", countAprobados);
        model.addAttribute("totalRechazados", countRechazados);
        model.addAttribute("totalPendientes", countPendientes);
        
        return "jefe/historial-solicitudes";
    }

    @GetMapping("/jefe/socios-aprobados")
    public String sociosAprobadosPanel(Model model) {
        agregarPerfilJefe(model);
        return "jefe/socios-aprobados-panel";
    }

    // --- FLUJO SOCIO (Dashboard) ---
    @GetMapping("/socio/dashboard")
    public String socioDashboard(Model model) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) return "redirect:/login";

        // Obtener datos del socio (basado en postulante aprobado)
        PostulanteDTO p = postulanteService.buscarPorNumeroDocumento(usuario.getUsername());
        if (p == null) {
            p = postulanteRepository.findByCorreoElectronico(usuario.getUsername())
                    .map(ent -> postulanteService.buscarPorId(ent.getId()))
                    .orElse(null);
        }

        if (p != null) {
            model.addAttribute("socio", p);
            agregarPerfilSocio(model, p);
            
            // Datos estadísticos simulados o reales (HU-09/15/17)
            model.addAttribute("totalEmbarcaciones", 1); // Placeholder
            model.addAttribute("saldoPendiente", 150.00); // Placeholder o desde service
            model.addAttribute("ultimaSalida", "2024-03-28");
            model.addAttribute("proximaReserva", "2024-04-10");
        }

        return "socio/dashboard";
    }

    @GetMapping("/consultar-estado-socio")
    public String consultarEstadoPublico(@RequestParam(required = false) Boolean autoconsulta, Model model) {
        if (Boolean.TRUE.equals(autoconsulta)) {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario != null) {
                model.addAttribute("autoDni", usuario.getUsername()); // El username es el correo/DNI en este sistema
            }
        }
        return "publico/consultar-estado-socio";
    }

    @GetMapping("/verificar-estado-cuenta")
    public String verificarEstadoCuenta(Model model) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) return "redirect:/login";

        PostulanteDTO postulante = postulanteService.buscarPorNumeroDocumento(usuario.getUsername());
        if (postulante == null) {
            // Reintentar por correo si el username es el correo
            postulante = postulanteRepository.findByCorreoElectronico(usuario.getUsername())
                    .map(p -> {
                        // Mapear manualmente si es necesario o usar el service
                        return postulanteService.buscarPorId(p.getId());
                    }).orElse(null);
        }

        if (postulante != null) {
            model.addAttribute("socio", postulante);
            agregarPerfilSocio(model, postulante);
        }
        return "socio/verificar-estado-cuenta";
    }

    @GetMapping("/socio/deuda-pendiente")
    public String deudaPendiente(Model model) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) return "redirect:/login";

        PostulanteDTO postulante = postulanteService.buscarPorNumeroDocumento(usuario.getUsername());
        if (postulante == null) {
            postulante = postulanteRepository.findByCorreoElectronico(usuario.getUsername())
                    .map(p -> postulanteService.buscarPorId(p.getId()))
                    .orElse(null);
        }

        if (postulante != null) {
            PostulanteConDeudasDTO conDeudas = postulanteService.obtenerPostulanteConDeudasDetalle(postulante.getIdPostulante());
            model.addAttribute("socio", conDeudas);
            model.addAttribute("motivoRechazo", postulanteService.obtenerUltimoMotivoRechazo(postulante.getIdPostulante()));
        }

        return "socio/deuda-pendiente";
    }

    @GetMapping("/socio/estado-revision")
    public String estadoRevision(Model model) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) return "redirect:/login";

        // El username es el correo. Buscamos el postulante asociado.
        Postulante postulante = postulanteRepository.findByCorreoElectronico(usuario.getUsername())
                .orElse(null);

        if (postulante != null) {
            ConsultaEstadoDTO estado = socioService.consultarEstadoPublico(postulante.getNumeroDocumento());
            model.addAttribute("resultado", estado);
        }

        return "socio/estado-revision";
    }

    @PostMapping("/registro/guardar")
    public String guardarRegistro(@ModelAttribute("registroDTO") RegistroPostulanteDTO dto, RedirectAttributes ra) {
        try {
            PostulanteDTO guardado = postulanteService.registrarPostulante(dto);
            ra.addFlashAttribute("postulante", guardado);
            ra.addFlashAttribute("mensaje", "Solicitud de registro enviada exitosamente. Por favor, espere la validación.");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("registroDTO", dto);
            return "redirect:/registro";
        }
    }

    @PostMapping("/registro/subsanar/{id}")
    public String subsanarRegistro(@PathVariable Integer id, @ModelAttribute("registroDTO") RegistroPostulanteDTO dto, RedirectAttributes ra) {
        try {
            postulanteService.subsanarPostulante(id, dto);
            PostulanteDTO guardado = postulanteService.buscarPorId(id);
            ra.addFlashAttribute("postulante", guardado);
            ra.addFlashAttribute("mensaje", "Subsanación completada exitosamente.");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("registroDTO", dto);
            return "redirect:/registro/subsanar/formulario/" + id;
        }
    }

    // --- NUEVOS ENDPOINTS SUBSANACIÓN ---
    @GetMapping("/registro/subsanar")
    public String vistaSubsanarBuscar() {
        return "registro/subsanacion-buscar";
    }

    @PostMapping("/registro/subsanar/buscar")
    public String buscarSubsanacion(@RequestParam String dni, RedirectAttributes ra) {
        PostulanteDTO p = postulanteService.buscarPorNumeroDocumento(dni);
        
        if (p == null) {
            ra.addFlashAttribute("error", "No se encontró ninguna solicitud con el documento: " + dni);
            return "redirect:/registro/subsanar";
        }
        
        if (!"rechazado".equalsIgnoreCase(p.getEstadoPostulacion())) {
            ra.addFlashAttribute("error", "La solicitud no se encuentra en estado RECHAZADO (Estado actual: " + p.getEstadoPostulacion().toUpperCase() + ")");
            return "redirect:/registro/subsanar";
        }

        // Pre-llenar DTO para el formulario
        RegistroPostulanteDTO dto = RegistroPostulanteDTO.builder()
                .tipoInteres(p.getTipoInteres())
                .tipoDocumento(p.getTipoDocumento())
                .numeroDocumento(p.getNumeroDocumento())
                .nombre(p.getNombres())
                .apellidoPaterno(p.getApellidoPaterno())
                .apellidoMaterno(p.getApellidoMaterno())
                .razonSocial(p.getRazonSocial())
                .correo(p.getCorreoElectronico())
                .telefono(p.getTelefono())
                .direccion(p.getDireccion())
                .ciudad(p.getCiudad())
                .fechaNacimiento(p.getFechaNacimiento())
                .build();

        ra.addFlashAttribute("registroDTO", dto);
        ra.addFlashAttribute("idPostulante", p.getIdPostulante());
        return "redirect:/registro/subsanar/formulario/" + p.getIdPostulante();
    }

    @GetMapping("/registro/subsanar/formulario/{id}")
    public String vistaSubsanarFormulario(@PathVariable Integer id, Model model) {
        if (!model.containsAttribute("registroDTO")) {
            return "redirect:/registro/subsanar";
        }
        return "registro/subsanacion-formulario";
    }

    @PostMapping("/registro/subsanar/guardar")
    public String guardarSubsanacion(@ModelAttribute("registroDTO") RegistroPostulanteDTO dto, RedirectAttributes ra) {
        try {
            // Buscar por DNI ya que el ID no viene en el form mapping simple sin hidden
            PostulanteDTO p = postulanteService.buscarPorNumeroDocumento(dto.getNumeroDocumento());
            if (p == null) throw new RuntimeException("Postulante no encontrado");
            
            postulanteService.subsanarPostulante(p.getIdPostulante(), dto);
            ra.addFlashAttribute("postulante", postulanteService.buscarPorId(p.getIdPostulante()));
            ra.addFlashAttribute("mensaje", "Solicitud subsanada correctamente. Ha vuelto a estado PENDIENTE.");
            return "redirect:/registro/exitoso";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("registroDTO", dto);
            return "redirect:/registro/subsanar";
        }
    }

    // Helper para obtener usuario autenticado
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return usuarioRepository.findByUsername(auth.getName()).orElse(null);
    }

    private Integer obtenerIdEmpleadoActual() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            return null;
        }
        Optional<Empleado> empleado = empleadoRepository.findByUsuario(usuario);
        return empleado.map(Empleado::getId).orElse(null);
    }

    private void agregarPerfilJefe(Model model) {
        Usuario usuario = obtenerUsuarioActual();
        String nombre = "Administrador";
        String iniciales = "AD";

        if (usuario != null) {
            Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuario(usuario);
            if (empleadoOpt.isPresent()) {
                Empleado empleado = empleadoOpt.get();
                nombre = empleado.getNombres() + " " + empleado.getApellidoPaterno();
                iniciales = generarIniciales(empleado.getNombres(), empleado.getApellidoPaterno());
            } else {
                nombre = usuario.getUsername();
                iniciales = generarIniciales(usuario.getUsername(), "");
            }
        }

        model.addAttribute("jefeNombre", nombre);
        model.addAttribute("jefeIniciales", iniciales);
    }

    private void agregarPerfilSocio(Model model, PostulanteDTO p) {
        String nombre = p.getNombres() != null ? (p.getNombres() + " " + p.getApellidoPaterno()) : p.getRazonSocial();
        String iniciales = p.getNombres() != null ? generarIniciales(p.getNombres(), p.getApellidoPaterno()) : "SC";
        
        model.addAttribute("socioNombre", nombre);
        model.addAttribute("socioIniciales", iniciales);
    }

    private String generarIniciales(String primerTexto, String segundoTexto) {
        String primera = (primerTexto != null && !primerTexto.isBlank()) ? primerTexto.substring(0, 1).toUpperCase() : "A";
        String segunda = (segundoTexto != null && !segundoTexto.isBlank()) ? segundoTexto.substring(0, 1).toUpperCase() : "D";
        return primera + segunda;
    }
}
