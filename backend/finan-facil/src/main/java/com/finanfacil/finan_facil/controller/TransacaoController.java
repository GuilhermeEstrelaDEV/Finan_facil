package com.finanfacil.finan_facil.controller;

import com.finanfacil.finan_facil.model.Transacao;
import com.finanfacil.finan_facil.model.Usuario;
import com.finanfacil.finan_facil.repository.TransacaoRepository;
import com.finanfacil.finan_facil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<Transacao> criarTransacao(@RequestBody Transacao transacao) {
        if (transacao.getUsuario() == null || transacao.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(transacao.getUsuario().getId());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        transacao.setUsuario(usuarioOpt.get());
        Transacao nova = transacaoRepository.save(transacao);
        return ResponseEntity.ok(nova);
    }

    @GetMapping
    public ResponseEntity<List<Transacao>> listarPorUsuario(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId != null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            List<Transacao> transacoes = transacaoRepository.findByUsuario(usuarioOpt.get());
            return ResponseEntity.ok(transacoes);
        } else {
            return ResponseEntity.ok(transacaoRepository.findAll());
        }
    }

    @GetMapping("/saldo")
    public ResponseEntity<BigDecimal> calcularSaldo(@RequestParam Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Usuario usuario = usuarioOpt.get();
        List<Transacao> transacoes = transacaoRepository.findByUsuario(usuario);

        BigDecimal saldo = BigDecimal.ZERO;

        for (Transacao t : transacoes) {
            if (t.getTipo().name().equals("SALARIO")) {
                saldo = saldo.add(t.getValor());
            } else if (t.getTipo().name().equals("DESPESA")) {
                saldo = saldo.subtract(t.getValor());
            }
        }

        return ResponseEntity.ok(saldo);
    }
}
