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
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        // Buscamos por nombre de usuario O por correo electrónico
        Usuario usuario = usuarioRepository.findByUsernameOrCorreoElectronico(input, input)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o correo no encontrado: " + input));

        // Validar que el usuario esté activo
        if (usuario.getEstado() == null || !usuario.getEstado().equalsIgnoreCase("activo")) {
            throw new UsernameNotFoundException("Usuario inactivo: " + input);
        }

        // Obtener el nombre del rol (asegurándonos de que tenga el prefijo ROLE_)
        String nombreRol = usuario.getRol().getNombre().toUpperCase();
        if (!nombreRol.startsWith("ROLE_")) {
            nombreRol = "ROLE_" + nombreRol;
        }

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(nombreRol))
        );
    }
}
