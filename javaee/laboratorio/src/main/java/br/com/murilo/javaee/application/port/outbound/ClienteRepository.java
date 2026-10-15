package br.com.murilo.javaee.application.port.outbound;
import br.com.murilo.javaee.application.dto.ClienteResumo;

import br.com.murilo.javaee.domain.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscar(Long id);
    List<ClienteResumo> listar();
}
