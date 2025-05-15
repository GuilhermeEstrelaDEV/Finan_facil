package com.finanfacil.finan_facil.controller;

import com.finanfacil.finan_facil.dto.TransacaoRequest;
import com.finanfacil.finan_facil.model.Categoria;
import com.finanfacil.finan_facil.model.TipoTransacao;
import com.finanfacil.finan_facil.model.Transacao;
import com.finanfacil.finan_facil.model.Usuario;
import com.finanfacil.finan_facil.repository.CategoriaRepository;
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

    @Autowired
    private CategoriaRepository categoriaRepository;

    @PostMapping
    public ResponseEntity<?> criarTransacao(@RequestBody TransacaoRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(request.getUsuarioId());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        TipoTransacao tipo;
        try {
            tipo = TipoTransacao.valueOf(request.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de transação inválido. Use SALARIO ou DESPESA.");
        }

        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(request.getCategoriaId());
            if (categoriaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Categoria não encontrada.");
            }
            categoria = categoriaOpt.get();
        }

        Transacao transacao = new Transacao();
        transacao.setDescricao(request.getDescricao());
        transacao.setValor(request.getValor());
        transacao.setData(request.getData());
        transacao.setTipo(tipo);
        transacao.setUsuario(usuarioOpt.get());
        transacao.setCategoria(categoria);

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
            if (t.getTipo().equals(TipoTransacao.SALARIO)) {
                saldo = saldo.add(t.getValor());
            } else if (t.getTipo().equals(TipoTransacao.DESPESA)) {
                saldo = saldo.subtract(t.getValor());
            }
        }

        return ResponseEntity.ok(saldo);
    }
}
