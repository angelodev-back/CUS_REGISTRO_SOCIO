package com.idat.pe.Cus_Registro_Postulante.config;

import com.idat.pe.Cus_Registro_Postulante.entity.EstadoPostulante;
import com.idat.pe.Cus_Registro_Postulante.entity.Postulante;
import com.idat.pe.Cus_Registro_Postulante.repository.PostulanteRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private PostulanteRepository postulanteRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
            throws IOException, ServletException {
        
        String portalSource = request.getParameter("portal_source");
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String username = authentication.getName();
        
        boolean isJefe = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_JEFE"));
        boolean isSocio = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SOCIO"));

        // 1. Validación de Mismatch entre Portal y Rol (Seguridad HU-10)
        if (isJefe && !"jefe".equals(portalSource)) {
            SecurityContextHolder.clearContext();
            response.sendRedirect("/login?error=true&portal_mismatch=true");
            return;
        }
        
        if (isSocio && "jefe".equals(portalSource)) {
            SecurityContextHolder.clearContext();
            response.sendRedirect("/jefe/login?error=true&portal_mismatch=true");
            return;
        }

        // 2. Redirección según Rol
        if (isJefe) {
            response.sendRedirect("/jefe/dashboard");
        } else if (isSocio) {
            Optional<Postulante> postulanteOpt = postulanteRepository.findByCorreoElectronico(username);
            if (postulanteOpt.isPresent()) {
                EstadoPostulante estado = postulanteOpt.get().getEstado();
                if (estado == EstadoPostulante.APROBADO) {
                    response.sendRedirect("/socio/dashboard");
                } else if (estado == EstadoPostulante.RECHAZADO) {
                    response.sendRedirect("/socio/deuda-pendiente");
                } else {
                    response.sendRedirect("/socio/estado-revision");
                }
            } else {
                response.sendRedirect("/socio/estado-revision");
            }
        } else {
            response.sendRedirect("/");
        }
    }
}
