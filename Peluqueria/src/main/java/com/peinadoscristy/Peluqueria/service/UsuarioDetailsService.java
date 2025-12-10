package com.peinadoscristy.Peluqueria.service;

import com.peinadoscristy.Peluqueria.model.Usuario;
import com.peinadoscristy.Peluqueria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscar en la base de datos por username
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado: " + username)
                );

        // Construimos el User de Spring con su rol
        String rolConPrefijo = "ROLE_" + usuario.getRol();  // ej: ADMIN → ROLE_ADMIN

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority(rolConPrefijo))
        );
    }
}
