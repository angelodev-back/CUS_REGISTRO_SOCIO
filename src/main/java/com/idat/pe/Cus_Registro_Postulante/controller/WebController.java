package com.idat.pe.Cus_Registro_Postulante.controller;

import org.springframework.ui.Model;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.PostulanteConDeudasDTO;
import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import com.idat.pe.Cus_Registro_Postulante.entity.Empleado;
import com.idat.pe.Cus_Registro_Postulante.entity.EstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.Socio;
import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.repository.EmpleadoRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.SocioRepository;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.service.PostulanteService;
import com.idat.pe.Cus_Registro_Postulante.service.SocioService;
import com.idat.pe.Cus_Registro_Postulante.dto.ConsultaEstadoDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import com.idat.pe.Cus_Registro_Postulante.service.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Controller
public class WebController {

    private static final String RECOVERY_ATTEMPTS_KEY = "passwordRecoveryAttempts";
    private static final String RECOVERY_LOCK_UNTIL_KEY = "passwordRecoveryLockUntil";
    private static final int MAX_RECOVERY_ATTEMPTS = 5;
    private static final long RECOVERY_LOCK_MINUTES = 15;
    private static final String RECOVERY_GENERIC_ERROR = "No se pudo validar tu identidad con los datos proporcionados.";

    @Autowired
    private PostulanteService postulanteService;

    @Autowired
    private SocioService socioService;

    @Autowired
    private PostulanteRepository postulanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private com.idat.pe.Cus_Registro_Postulante.repository.HistorialEstadoPostulanteRepository historialRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private com.idat.pe.Cus_Registro_Postulante.repository.InformeAdmisionRepository informeRepository;

    @Autowired
    private com.idat.pe.Cus_Registro_Postulante.service.ExcelReportService excelReportService;

    @Autowired
    private com.idat.pe.Cus_Registro_Postulante.service.EstadoCuentaService estadoCuentaService;

    @Autowired
    private com.idat.pe.Cus_Registro_Postulante.service.InformeAdmisionService informeService;

    @GetMapping({"/", "/inicio"})
    public String inicio() {
        return "index"; // Portal de bienvenida (página de inicio)
    }

@GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/jefe/login")
    public String jefeLogin() {
        return "jefe/login";
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/recuperar-password")
    public String recuperarPasswordView() {
        return "publico/recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String recuperarPasswordSubmit(@RequestParam("numDocumento") String numDocumento,
                                          @RequestParam("correo") String correo,
                                          @RequestParam("fechaNacimiento") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
                                          @RequestParam("nuevaPassword") String nuevaPassword,
                                          @RequestParam("confirmarPassword") String confirmarPassword,
                                          HttpSession session,
                                          RedirectAttributes ra) {
        if (isPasswordRecoveryLocked(session)) {
            LocalDateTime lockedUntil = (LocalDateTime) session.getAttribute(RECOVERY_LOCK_UNTIL_KEY);
            String horaDesbloqueo = lockedUntil != null
                    ? lockedUntil.toLocalTime().withSecond(0).withNano(0).toString()
                    : "más tarde";
            ra.addFlashAttribute("error", "Demasiados intentos fallidos. Intenta nuevamente después de las " + horaDesbloqueo + ".");
            return "redirect:/recuperar-password";
        }

        String documento = numDocumento != null ? numDocumento.trim() : "";
        String correoNormalizado = correo != null ? correo.trim().toLowerCase() : "";
        String nueva = nuevaPassword != null ? nuevaPassword.trim() : "";
        String confirmacion = confirmarPassword != null ? confirmarPassword.trim() : "";

        if (!documento.matches("\\d{8}|\\d{11}")) {
            ra.addFlashAttribute("error", "Ingresa un documento válido: DNI (8 dígitos) o RUC (11 dígitos).");
            return "redirect:/recuperar-password";
        }
        if (correoNormalizado.isBlank() || correoNormalizado.length() > 100) {
            ra.addFlashAttribute("error", "Ingresa un correo válido.");
            return "redirect:/recuperar-password";
        }
        if (fechaNacimiento == null) {
            ra.addFlashAttribute("error", "Ingresa la fecha de nacimiento o constitución registrada.");
            return "redirect:/recuperar-password";
        }
        if (!nueva.equals(confirmacion)) {
            ra.addFlashAttribute("error", "La nueva contraseña y su confirmación no coinciden.");
            return "redirect:/recuperar-password";
        }
        if (!esPasswordSegura(nueva)) {
            ra.addFlashAttribute("error", "La contraseña debe tener entre 8 y 72 caracteres, con mayúscula, minúscula, número y símbolo.");
            return "redirect:/recuperar-password";
        }

        Optional<Postulante> postulanteOpt = postulanteRepository.findByNumeroDocumento(documento);
        if (postulanteOpt.isEmpty()) {
            return rejectPasswordRecoveryAttempt(ra, session, RECOVERY_GENERIC_ERROR);
        }

        Postulante postulante = postulanteOpt.get();
        boolean identidadValida = postulante.getCorreoElectronico() != null
                && postulante.getCorreoElectronico().equalsIgnoreCase(correoNormalizado)
                && postulante.getFechaNacimiento() != null
                && postulante.getFechaNacimiento().equals(fechaNacimiento);

        if (!identidadValida || postulante.getEstado() != EstadoPostulante.APROBADO) {
            return rejectPasswordRecoveryAttempt(ra, session, RECOVERY_GENERIC_ERROR);
        }

        Optional<Socio> socioOpt = socioRepository.findByPostulante(postulante);
        if (socioOpt.isEmpty() || socioOpt.get().getUsuario() == null) {
            return rejectPasswordRecoveryAttempt(ra, session, RECOVERY_GENERIC_ERROR);
        }

        Usuario usuario = socioOpt.get().getUsuario();
        if (!Boolean.TRUE.equals(usuario.getEstadoUsuario())) {
            return rejectPasswordRecoveryAttempt(ra, session, RECOVERY_GENERIC_ERROR);
        }
        if (usuario.getCorreoElectronico() == null || !usuario.getCorreoElectronico().equalsIgnoreCase(correoNormalizado)) {
            return rejectPasswordRecoveryAttempt(ra, session, RECOVERY_GENERIC_ERROR);
        }
        if (passwordEncoder.matches(nueva, usuario.getPassword())) {
            ra.addFlashAttribute("error", "La nueva contraseña debe ser diferente a la anterior.");
            return "redirect:/recuperar-password";
        }

        usuario.setPassword(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
        clearPasswordRecoveryAttempts(session);

        ra.addFlashAttribute("mensaje", "Contraseña actualizada correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    // --- FLUJO REGISTRO ---
    @GetMapping("/registro")
    public String registro(Model model) {
        if (!model.containsAttribute("registroDTO")) {
            model.addAttribute("registroDTO", RegistroPostulanteDTO.builder().tipoTelefono("CELULAR").build());
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
            
            // Buscar postulante para obtener el nombre completo
            com.idat.pe.Cus_Registro_Postulante.entity.Postulante p = 
                postulanteRepository.findById(id).orElse(null);
            
            String nombre = (p != null) 
                ? (p.getNombres() + " " + p.getApellidoPaterno())
                : "el socio";

            ra.addFlashAttribute("mensaje", "Cuenta generada para " + nombre);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al aprobar: " + e.getMessage());
        }
        return "redirect:/jefe/dashboard";
    }

    @PostMapping("/jefe/rechazar")
    public String rechazarPostulante(@RequestParam Integer idPostulante, @RequestParam String motivo, RedirectAttributes ra) {
        try {
            String motivoNormalizado = motivo != null ? motivo.trim() : "";
            if (motivoNormalizado.isBlank()) {
                throw new RuntimeException("Debe ingresar un motivo de rechazo.");
            }
            if (motivoNormalizado.length() > 100) {
                throw new RuntimeException("El motivo no debe exceder 100 caracteres.");
            }

            Integer idEmpleado = obtenerIdEmpleadoActual();
            if (idEmpleado == null) {
                throw new RuntimeException("No se pudo identificar al empleado autenticado");
            }
            postulanteService.rechazarPostulante(idPostulante, idEmpleado, motivoNormalizado);
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

    /* 
    @GetMapping("/jefe/socios-aprobados")
    public String sociosAprobadosPanel(Model model) {
        agregarPerfilJefe(model);
        return "jefe/socios-aprobados-panel";
    }
    */

    @GetMapping("/jefe/informe-registro/{id}")
    public String verInformeRegistro(@PathVariable Integer id, Model model) {
        agregarPerfilJefe(model);
        try {
            com.idat.pe.Cus_Registro_Postulante.entity.Socio socio = socioService.obtenerEntidadSocio(id);
            model.addAttribute("socio", socio);
            model.addAttribute("postulante", socio.getPostulante());

            // Buscar quién fue el que aprobó al postulante
            List<com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante> historial =
                historialRepository.findByPostulante_IdOrderByFechaCambioDesc(socio.getPostulante().getId());

            com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante validacion = historial.stream()
                .filter(h -> "aprobado".equalsIgnoreCase(h.getEstadoNuevo()))
                .findFirst()
                .orElse(null);

            model.addAttribute("validacion", validacion);
            
            return "jefe/informe-registro";
        } catch (Exception e) {
            return "redirect:/jefe/socios-aprobados";
        }
    }

    @GetMapping("/jefe/informe-registro/{id}/pdf")
    public ResponseEntity<InputStreamResource> descargarPdf(@PathVariable Integer id) {
        try {
            com.idat.pe.Cus_Registro_Postulante.entity.Socio socio = socioService.obtenerEntidadSocio(id);
            return generarPdfResponse(socio.getPostulante().getId(), "socio");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/jefe/pre-informe/{id}/pdf")
    public ResponseEntity<InputStreamResource> descargarPreInformePdf(@PathVariable Integer id) {
        return generarPdfResponse(id, "pre");
    }


    private ResponseEntity<InputStreamResource> generarPdfResponse(Integer idPostulante, String tipo) {
        try {
            PostulanteConDeudasDTO datosCompletos = postulanteService.obtenerPostulanteConDeudasDetalle(idPostulante);
            
            // Buscar validación si existe (para el pre-informe puede no haber aprobación aún)
            List<com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante> historial =
                historialRepository.findByPostulante_IdOrderByFechaCambioDesc(idPostulante);

            com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante validacion = historial.stream()
                .filter(h -> "aprobado".equalsIgnoreCase(h.getEstadoNuevo()) || "rechazado".equalsIgnoreCase(h.getEstadoNuevo()))
                .findFirst()
                .orElse(null);

            com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision informe = 
                informeService.obtenerPorPostulante(idPostulante).orElse(null);

            ByteArrayInputStream bis = pdfReportService.generarInformeRegistro(datosCompletos, validacion, informe);
            String prefix = "socio".equals(tipo) ? "Informe_Final_" : "Pre_Informe_Admision_";
            String fileName = prefix + datosCompletos.getNumeroDocumento() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/jefe/reporte/{id}/excel")
    public ResponseEntity<InputStreamResource> descargarExcelDetallado(@PathVariable Integer id) {
        try {
            ByteArrayInputStream bis = excelReportService.generarReporteDetallado(id);
            String fileName = "Reporte_Socio_" + id + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(bis));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/jefe/historial/excel")
    public ResponseEntity<InputStreamResource> descargarHistorialGeneral() {
        try {
            List<com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante> historial = historialRepository.findAll();
            ByteArrayInputStream bis = excelReportService.generarReporteHistorialGeneral(historial);
            String fileName = "Historial_General.xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(bis));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- API de Admisión Unificada (HU-Portal) ---

    @GetMapping("/jefe/api/historial/{idPostulante}")
    @ResponseBody
    public List<com.idat.pe.Cus_Registro_Postulante.entity.HistorialEstadoPostulante> obtenerApiHistorial(@PathVariable Integer idPostulante) {
        return historialRepository.findByPostulante_IdOrderByFechaCambioDesc(idPostulante);
    }

    @GetMapping("/jefe/api/informe/{idPostulante}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerApiInforme(@PathVariable Integer idPostulante) {
        return informeService.obtenerPorPostulante(idPostulante)
                .map(inf -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", inf.getId());
                    map.put("observaciones", inf.getObservaciones());
                    map.put("recomendacion", inf.getRecomendacionManual());
                    map.put("estado", inf.getEstado());
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/jefe/informe-guardar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarInformeDesdePortal(
            @RequestParam Integer idPostulante,
            @RequestParam String observaciones,
            @RequestParam String recomendacion,
            @RequestParam(required = false) String estado) {
        
        Postulante p = postulanteRepository.findById(idPostulante)
                .orElseThrow(() -> new RuntimeException("Postulante no encontrado"));
                
        com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision informe = 
                informeService.obtenerPorPostulante(idPostulante).orElse(new com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision());
                
        informe.setPostulante(p);
        informe.setObservaciones(observaciones);
        informe.setRecomendacionManual(recomendacion);
        informe.setEstado(estado != null ? estado : "ACTUALIZADO");
        
        com.idat.pe.Cus_Registro_Postulante.entity.InformeAdmision guardado = informeRepository.save(informe);
        
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("id", guardado.getId());
        map.put("estado", guardado.getEstado());
        
        return ResponseEntity.ok(map);
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



    @GetMapping("/verificar-estado-cuenta")
    public String verificarEstadoCuenta(HttpSession session, Model model) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) return "redirect:/login";


        // Buscar el Socio por el usuario actual
        Optional<Socio> socioOpt = socioRepository.findByUsuario(usuario);
        if (socioOpt.isPresent()) {
            Socio socio = socioOpt.get();
            List<com.idat.pe.Cus_Registro_Postulante.dto.EstadoCuentaDTO> estados = 
                estadoCuentaService.obtenerEstadoCuentaPorSocio(socio.getId());
            Double saldoTotal = estadoCuentaService.calcularSaldoTotalPendiente(socio.getId());
            
            model.addAttribute("socio", socio.getPostulante());
            model.addAttribute("estadosCuenta", estados);
            model.addAttribute("saldoTotal", saldoTotal);
            
            // Perfil
            String nombre = socio.getPostulante().getNombres() + " " + socio.getPostulante().getApellidoPaterno();
            String iniciales = generarIniciales(socio.getPostulante().getNombres(), socio.getPostulante().getApellidoPaterno());
            model.addAttribute("socioNombre", nombre);
            model.addAttribute("socioIniciales", iniciales);
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
    public String guardarRegistro(@Valid @ModelAttribute("registroDTO") RegistroPostulanteDTO dto, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            String firstError = result.getAllErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("error", firstError);
            ra.addFlashAttribute("registroDTO", dto);
            return "redirect:/registro";
        }
        try {
            PostulanteDTO guardado = postulanteService.registrarPostulante(dto);
            ra.addFlashAttribute("postulante", guardado);
            ra.addFlashAttribute("mensaje", "Solicitud de registro enviada exitosamente. Por favor, espere la validación.");
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
            ra.addFlashAttribute("mensaje", "Subsanación completada exitosamente.");
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
                .tipoTelefono(inferirTipoTelefono(p.getTelefono()))
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
    public String guardarSubsanacion(@Valid @ModelAttribute("registroDTO") RegistroPostulanteDTO dto, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            String firstError = result.getAllErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("error", firstError);
            ra.addFlashAttribute("registroDTO", dto);
            return "redirect:/registro/subsanar";
        }
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

    private boolean esPasswordSegura(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            return false;
        }

        boolean tieneMayuscula = password.chars().anyMatch(Character::isUpperCase);
        boolean tieneMinuscula = password.chars().anyMatch(Character::isLowerCase);
        boolean tieneNumero = password.chars().anyMatch(Character::isDigit);
        boolean tieneSimbolo = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c));

        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneSimbolo;
    }

    private boolean isPasswordRecoveryLocked(HttpSession session) {
        Object lockObj = session.getAttribute(RECOVERY_LOCK_UNTIL_KEY);
        if (lockObj instanceof LocalDateTime lockUntil) {
            if (LocalDateTime.now().isBefore(lockUntil)) {
                return true;
            }
            clearPasswordRecoveryAttempts(session);
        }
        return false;
    }

    private void registerFailedPasswordRecoveryAttempt(HttpSession session) {
        int attempts = 0;
        Object attemptsObj = session.getAttribute(RECOVERY_ATTEMPTS_KEY);
        if (attemptsObj instanceof Integer) {
            attempts = (Integer) attemptsObj;
        }

        attempts++;
        session.setAttribute(RECOVERY_ATTEMPTS_KEY, attempts);

        if (attempts >= MAX_RECOVERY_ATTEMPTS) {
            session.setAttribute(RECOVERY_LOCK_UNTIL_KEY, LocalDateTime.now().plusMinutes(RECOVERY_LOCK_MINUTES));
        }
    }

    private void clearPasswordRecoveryAttempts(HttpSession session) {
        session.removeAttribute(RECOVERY_ATTEMPTS_KEY);
        session.removeAttribute(RECOVERY_LOCK_UNTIL_KEY);
    }

    private String rejectPasswordRecoveryAttempt(RedirectAttributes ra, HttpSession session, String message) {
        registerFailedPasswordRecoveryAttempt(session);
        ra.addFlashAttribute("error", message);
        return "redirect:/recuperar-password";
    }

    private String inferirTipoTelefono(String telefono) {
        String valor = telefono != null ? telefono.trim() : "";
        if (valor.startsWith("0")) {
            return "FIJO";
        }
        return "CELULAR";
    }
}
