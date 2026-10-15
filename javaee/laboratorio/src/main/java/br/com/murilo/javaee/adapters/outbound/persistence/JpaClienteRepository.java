package br.com.murilo.javaee.adapters.outbound.persistence;

import br.com.murilo.javaee.application.port.outbound.ClienteRepository;
import br.com.murilo.javaee.application.dto.ClienteResumo;
import br.com.murilo.javaee.domain.model.Cliente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaClienteRepository implements ClienteRepository {
    private final EntityManager manager;

    protected JpaClienteRepository() { this.manager = null; }

    @Inject
    public JpaClienteRepository(EntityManager manager) { this.manager = manager; }

    @Override
    public Cliente salvar(Cliente cliente) {
        manager.persist(cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> buscar(Long id) {
        return Optional.ofNullable(manager.find(Cliente.class, id));
    }

    @Override
    public List<ClienteResumo> listar() {
        return manager.createQuery("""
                select new br.com.murilo.javaee.application.dto.ClienteResumo(c.id, c.nome, c.email)
                from Cliente c order by c.nome
                """, ClienteResumo.class).getResultList();
    }
}
