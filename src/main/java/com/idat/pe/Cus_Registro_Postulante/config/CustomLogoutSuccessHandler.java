package com.idat.pe.Cus_Registro_Postulante.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isJefe = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_JEFE"));
            boolean isSocio = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SOCIO"));
            
            if (isJefe) {
                response.sendRedirect("/jefe/login?logout=true");
                return;
            }
        }
        
        // Default redirect
        response.sendRedirect("/login?logout=true");
    }
}
