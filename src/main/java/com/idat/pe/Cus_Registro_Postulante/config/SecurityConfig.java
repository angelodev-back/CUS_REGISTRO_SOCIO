package com.idat.pe.Cus_Registro_Postulante.config;

import com.idat.pe.Cus_Registro_Postulante.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers(new AntPathRequestMatcher("/css/**"))
            .requestMatchers(new AntPathRequestMatcher("/js/**"))
            .requestMatchers(new AntPathRequestMatcher("/images/**"))
            .requestMatchers(new AntPathRequestMatcher("/favicon.ico"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/jefe/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/socio/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/registro/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/postulantes/registrar")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/postulantes/listar")).hasRole("JEFE")
                .requestMatchers(new AntPathRequestMatcher("/api/postulantes/**")).hasRole("JEFE")
                .requestMatchers(new AntPathRequestMatcher("/api/jefe/**")).hasRole("JEFE")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
