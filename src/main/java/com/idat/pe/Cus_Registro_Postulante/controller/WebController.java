package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.dto.RegistroPostulanteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    @Autowired
    private com.idat.pe.Cus_Registro_Postulante.service.PostulanteService postulanteService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/debug/bcrypt")
    @ResponseBody
    public String generateBcrypt(@RequestParam(defaultValue = "123456") String password) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
                + "body { font-family: Arial; padding: 40px; background: #f5f5f5; }"
                + ".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }"
                + "h1 { color: #0094bc; }"
                + ".field { margin: 20px 0; padding: 10px; background: #f0f0f0; border-radius: 4px; }"
                + ".label { font-weight: bold; color: #333; margin-bottom: 5px; }"
                + ".value { font-family: monospace; font-size: 14px; word-break: break-all; color: #666; background: white; padding: 10px; border-radius: 4px; }"
                + ".copy-btn { background: #0094bc; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; margin-top: 10px; }"
                + ".copy-btn:hover { background: #007399; }"
                + "</style></head><body><div class='container'>"
                + "<h1>🔐 Generador de Hash BCrypt</h1>"
                + "<div class='field'><div class='label'>Contraseña original:</div><div class='value'>" + password + "</div></div>"
                + "<div class='field'><div class='label'>Hash BCrypt generado:</div><div class='value' id='hashValue'>" + hashedPassword + "</div>"
                + "<button class='copy-btn' onclick=\"document.getElementById('hashValue').select(); document.execCommand('copy')\">Copiar Hash</button></div>"
                + "<div class='field' style='background: #e8f5e9; border-left: 4px solid #4caf50;'>"
                + "<strong>✓ Usa este hash en tu script SQL</strong>"
                + "</div></div></body></html>";
    }

    @GetMapping("/jefe/dashboard")
    public String jefeDashboard() {
        // Redirige al dashboard de deudas del JEFE
        return "redirect:/jefe/dashboard-deudas";
    }
    
    @GetMapping("/jefe/dashboard-deudas")
    public String jefeDashboardDeudas() {
        // Dashboard del JEFE para revisar postulantes y deudas externas
        return "jefe/dashboard-deudas";
    }

    @GetMapping("/socio/dashboard")
    public String socioDashboard() {
        // TODO: Crear dashboard para SOCIO
        return "redirect:/";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        if (!model.containsAttribute("registroDTO")) {
            model.addAttribute("registroDTO", new RegistroPostulanteDTO());
        }
        return "registro/formulario-registro";
    }

    @PostMapping("/registro/guardar")
    public String guardar(@ModelAttribute("registroDTO") RegistroPostulanteDTO dto, 
                          BindingResult result, 
                          org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registro/formulario-registro";
        }
        try {
            com.idat.pe.Cus_Registro_Postulante.dto.PostulanteDTO saved = postulanteService.registrarPostulante(dto);
            redirectAttributes.addFlashAttribute("postulante", saved);
            return "redirect:/registro/exitoso";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("registroDTO", dto);
            return "redirect:/registro";
        }
    }

    @GetMapping("/registro/exitoso")
    public String exitoso(Model model) {
        if (!model.containsAttribute("postulante")) {
            return "redirect:/registro";
        }
        return "registro/registro-exitoso";
    }
}
