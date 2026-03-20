package com.idat.pe.Cus_Registro_Postulante.security;

import com.idat.pe.Cus_Registro_Postulante.entity.Usuario;
import com.idat.pe.Cus_Registro_Postulante.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Validar que el usuario esté activo (case-insensitive)
        if (usuario.getEstado() == null || !usuario.getEstado().equalsIgnoreCase("activo")) {
            throw new UsernameNotFoundException("Usuario inactivo o no validado: " + username);
        }

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre().toUpperCase())
                )
        );
    }
}
