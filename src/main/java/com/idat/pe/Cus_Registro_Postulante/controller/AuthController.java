package com.idat.pe.Cus_Registro_Postulante.controller;

import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.genericResponse.GenericResponse;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import com.idat.pe.Cus_Registro_Postulante.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<String, LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponse.<String, LoginResponse>builder()
                            .message("Usuario o contraseña incorrectos")
                            .statusCode("401")
                            .body(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponse.<String, LoginResponse>builder()
                            .message("Usuario inactivo o no validado")
                            .statusCode("401")
                            .body(null)
                            .build());
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        // Obtener datos adicionales del usuario
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElse(null);

        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .username(userDetails.getUsername())
                .rol(usuario != null ? usuario.getRol().getNombre() : "")
                .build();

        return ResponseEntity.ok(GenericResponse.<String, LoginResponse>builder()
                .message("Login exitoso")
                .body(response)
                .statusCode("200")
                .build());
    }

    @GetMapping("/validate")
    public ResponseEntity<GenericResponse<String, ValidateResponse>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(GenericResponse.<String, ValidateResponse>builder()
                                .message("Token no proporcionado")
                                .statusCode("401")
                                .body(null)
                                .build());
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);

            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                ValidateResponse response = ValidateResponse.builder()
                        .valid(true)
                        .username(username)
                        .role(role)
                        .build();

                return ResponseEntity.ok(GenericResponse.<String, ValidateResponse>builder()
                        .message("Token válido")
                        .body(response)
                        .statusCode("200")
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(GenericResponse.<String, ValidateResponse>builder()
                                .message("Token expirado o inválido")
                                .statusCode("401")
                                .body(ValidateResponse.builder().valid(false).build())
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponse.<String, ValidateResponse>builder()
                            .message("Error validando token: " + e.getMessage())
                            .statusCode("401")
                            .body(ValidateResponse.builder().valid(false).build())
                            .build());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<GenericResponse<String, ProfileResponse>> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(GenericResponse.<String, ProfileResponse>builder()
                                .message("Token no proporcionado")
                                .statusCode("401")
                                .body(null)
                                .build());
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(GenericResponse.<String, ProfileResponse>builder()
                                .message("Token inválido o expirado")
                                .statusCode("401")
                                .body(null)
                                .build());
            }

            String username = jwtUtil.extractUsername(token);
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            ProfileResponse response = ProfileResponse.builder()
                    .id(usuario.getId())
                    .username(usuario.getUsername())
                    .nombres(usuario.getNombres())
                    .apellidoPaterno(usuario.getApellidoPaterno())
                    .apellidoMaterno(usuario.getApellidoMaterno())
                    .rol(usuario.getRol().getNombre())
                    .correo(usuario.getCorreoElectronico())
                    .estado(usuario.getEstado())
                    .build();

            return ResponseEntity.ok(GenericResponse.<String, ProfileResponse>builder()
                    .message("Perfil obtenido exitosamente")
                    .body(response)
                    .statusCode("200")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponse.<String, ProfileResponse>builder()
                            .message("Error obteniendo perfil: " + e.getMessage())
                            .statusCode("401")
                            .body(null)
                            .build());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class LoginResponse {
        private String token;
        private String username;
        private String rol;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class ValidateResponse {
        private boolean valid;
        private String username;
        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class ProfileResponse {
        private Integer id;
        private String username;
        private String nombres;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private String rol;
        private String correo;
        private String estado;
    }
}
