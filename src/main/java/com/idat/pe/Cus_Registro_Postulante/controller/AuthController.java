package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.entity.EstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.Optional;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PostulanteRepository postulanteRepository;

    @GetMapping("/login-success")
    public String loginSuccess(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String username = authentication.getName();
        
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_JEFE")) {
                return "redirect:/jefe/dashboard";
            } else if (role.equals("ROLE_SOCIO")) {
                // Buscar el postulante asociado al usuario por su email/username
                Optional<Postulante> postulanteOpt = postulanteRepository.findByCorreoElectronico(username);
                
                if (postulanteOpt.isPresent()) {
                    EstadoPostulante estado = postulanteOpt.get().getEstado();
                    if (estado == EstadoPostulante.APROBADO) {
                        return "redirect:/socio/dashboard";
                    } else if (estado == EstadoPostulante.RECHAZADO) {
                        return "redirect:/socio/deuda-pendiente";
                    } else if (estado == EstadoPostulante.PENDIENTE || estado == EstadoPostulante.SUBSANADO) {
                        return "redirect:/socio/estado-revision";
                    }
                }
                return "redirect:/socio/estado-revision";
            }
        }
        
        return "redirect:/";
    }
}
