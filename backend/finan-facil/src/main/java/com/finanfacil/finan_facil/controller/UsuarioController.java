package com.finanfacil.finan_facil.controller;

import com.finanfacil.finan_facil.dto.LoginRequest;
import com.finanfacil.finan_facil.model.Usuario;
import com.finanfacil.finan_facil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
    System.out.println("Senha recebida: " + usuario.getSenha()); // debug

    if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
        return ResponseEntity.badRequest().body(null);
    }

    if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
        return ResponseEntity.badRequest().build();
    }

    usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
    Usuario novoUsuario = usuarioRepository.save(usuario);
    return ResponseEntity.ok(novoUsuario);
}


    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
                return ResponseEntity.ok(usuario);
            }
        }

        return ResponseEntity.status(401).build();
    }
}
