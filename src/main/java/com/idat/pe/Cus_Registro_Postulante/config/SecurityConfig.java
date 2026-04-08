package com.idat.pe.Cus_Registro_Postulante.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitado para simplificar el proyecto estudiantil
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/jefe/login"),
                    new AntPathRequestMatcher("/recuperar-password"),
                    new AntPathRequestMatcher("/inicio"),
                    new AntPathRequestMatcher("/registro/**"),
                    new AntPathRequestMatcher("/api/postulantes/buscar-por-documento/**"),

                    new AntPathRequestMatcher("/api/socios/consultar-publico/**"),
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/images/**"),
                    new AntPathRequestMatcher("/error")
                ).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/jefe/**")).hasRole("JEFE")
                .requestMatchers(new AntPathRequestMatcher("/api/deudas/**")).hasRole("JEFE")
                .requestMatchers(new AntPathRequestMatcher("/api/socios/aprobados")).hasRole("JEFE")
                .requestMatchers(new AntPathRequestMatcher("/api/socios/generar-cuenta/**")).hasRole("JEFE")
                .requestMatchers(new AntPathRequestMatcher("/socio/**")).hasRole("SOCIO")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            );

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
