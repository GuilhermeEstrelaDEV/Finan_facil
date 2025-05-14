package com.finanfacil.finan_facil.repository;

import com.finanfacil.finan_facil.model.Transacao;
import com.finanfacil.finan_facil.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsuario(Usuario usuario);
}
