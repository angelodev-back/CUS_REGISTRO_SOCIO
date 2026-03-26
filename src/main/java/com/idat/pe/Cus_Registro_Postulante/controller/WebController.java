package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.service.EmbarcacionService;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.SocioRepository;
import com.idat.pe.Cus_Registro_Postulante.entity.Socio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private PostulanteService postulanteService;

    @Autowired
    private EmbarcacionService embarcacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SocioRepository socioRepository;

    @GetMapping("/")
    public String index() {
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

    @PostMapping("/socio/registrar-embarcacion")
    public String registrarEmbarcacion(@RequestParam String nombre,
                                     @RequestParam String tipo,
                                     @RequestParam String matricula,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam(required = false) Integer socioId, // Opcional para Staff
                                     RedirectAttributes ra) {
        try {
            Usuario user = obtenerUsuarioActual();
            Integer idParaRegistro;

            if (socioId != null && !user.getRol().getNombre().equals("SOCIO")) {
                // Registro asistido por personal
                idParaRegistro = socioId;
            } else {
                // Auto-registro por el socio
                Socio socio = socioRepository.findByUsuario(user)
                        .orElseThrow(() -> new RuntimeException("Socio no encontrado para el usuario actual"));
                idParaRegistro = socio.getId();
            }

            com.idat.pe.Cus_Registro_Postulante.dto.EmbarcacionDTO dto = new com.idat.pe.Cus_Registro_Postulante.dto.EmbarcacionDTO();
            dto.setNombre(nombre);
            dto.setTipo(tipo);
            dto.setMatricula(matricula);
            dto.setDescripcion(descripcion);
            dto.setEstado("Activa");

            embarcacionService.registrarEmbarcacion(dto, idParaRegistro);
            ra.addFlashAttribute("mensaje", "Embarcación registrada correctamente.");
            
            if (user.getRol().getNombre().equals("SOCIO")) {
                return "redirect:/socio/dashboard";
            } else {
                return "redirect:/jefe/dashboard"; // O a una vista de gestión de embarcaciones si existiera
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/socio/registrar-embarcacion";
        }
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
    public String jefeDashboard(Model model) {
        Usuario jefe = obtenerUsuarioActual();
        model.addAttribute("jefe", jefe);
        List<PostulanteConDeudasDTO> pendientes = postulanteService.obtenerPostulantesPendientesConDeudas();
        model.addAttribute("postulantes", pendientes);
        return "jefe/dashboard-deudas";
    }

    @PostMapping("/jefe/aprobar/{id}")
    public String aprobarPostulante(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Integer idJefe = obtenerUsuarioActual().getId();
            postulanteService.aprobarPostulante(id, idJefe);
            ra.addFlashAttribute("mensaje", "Postulante aprobado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jefe/dashboard";
    }

    @PostMapping("/jefe/rechazar")
    public String rechazarPostulante(@RequestParam Integer idPostulante, @RequestParam String motivo, RedirectAttributes ra) {
        try {
            Integer idJefe = obtenerUsuarioActual().getId();
            postulanteService.rechazarPostulante(idPostulante, idJefe, motivo);
            ra.addFlashAttribute("mensaje", "Postulante rechazado para subsanación.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jefe/dashboard";
    }

    // --- FLUJO SOCIO ---
    @GetMapping("/socio/dashboard")
    public String socioDashboard(Model model) {
        Usuario user = obtenerUsuarioActual();
        Socio socio = socioRepository.findByUsuario(user)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        
        model.addAttribute("socio", socio);
        model.addAttribute("embarcaciones", embarcacionService.listarPorSocio(socio.getId()));
        return "socio/socio-dashboard";
    }

    @GetMapping("/socio/registrar-embarcacion")
    public String vistaRegistrarEmbarcacion(Model model) {
        return "socio/embarcacion-registro";
    }

    @PostMapping("/registro/guardar")
    public String guardarRegistro(@ModelAttribute("registroDTO") RegistroPostulanteDTO dto, RedirectAttributes ra) {
        try {
            PostulanteDTO guardado = postulanteService.registrarPostulante(dto);
            ra.addFlashAttribute("postulante", guardado);
            return "redirect:/registro/exitoso";
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
            return "redirect:/registro/exitoso";
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
    public String buscarSubsanacion(@RequestParam String dni, RedirectAttributes ra, Model model) {
        PostulanteDTO p = postulanteService.buscarPorNumeroDocumento(dni);
        
        if (p == null) {
            ra.addFlashAttribute("error", "No se encontró ninguna solicitud con el documento: " + dni);
            return "redirect:/registro/subsanar";
        }
        
        if (!"rechazado".equalsIgnoreCase(p.getEstado())) {
            ra.addFlashAttribute("error", "La solicitud no se encuentra en estado RECHAZADO (Estado actual: " + p.getEstado().toUpperCase() + ")");
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
                .correo(p.getCorreo())
                .telefono(p.getTelefono())
                .direccion(p.getDireccion())
                .ciudad(p.getCiudad())
                .fechaNacimiento(p.getFechaNacimiento())
                .build();

        ra.addFlashAttribute("registroDTO", dto);
        ra.addFlashAttribute("idPostulante", p.getId());
        return "redirect:/registro/subsanar/formulario/" + p.getId();
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
            
            postulanteService.subsanarPostulante(p.getId(), dto);
            ra.addFlashAttribute("postulante", postulanteService.buscarPorId(p.getId()));
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
}
