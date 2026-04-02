package com.idat.pe.Cus_Registro_Postulante.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/login-success")
    public String loginSuccess(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_JEFE")) {
                return "redirect:/jefe/dashboard";
            } else if (authority.getAuthority().equals("ROLE_SOCIO")) {
                return "redirect:/consultar-estado-socio";
            }
        }
        
        return "redirect:/";
    }
}
