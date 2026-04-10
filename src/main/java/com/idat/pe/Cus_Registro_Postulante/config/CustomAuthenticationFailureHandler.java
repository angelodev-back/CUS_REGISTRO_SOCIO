package com.idat.pe.Cus_Registro_Postulante.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String portalSource = request.getParameter("portal_source");

        if ("jefe".equals(portalSource)) {
            response.sendRedirect("/jefe/login?error=true");
        } else {
            response.sendRedirect("/login?error=true");
        }
    }
}
