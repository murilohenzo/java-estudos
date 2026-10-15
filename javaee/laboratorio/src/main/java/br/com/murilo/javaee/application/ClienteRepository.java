package br.com.murilo.javaee.application;

import br.com.murilo.javaee.domain.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscar(Long id);
    List<ClienteResumo> listar();
}
