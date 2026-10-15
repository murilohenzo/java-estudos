package br.com.murilo.javaee.infrastructure;

import br.com.murilo.javaee.application.*;
import br.com.murilo.javaee.domain.Cliente;
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
                select new br.com.murilo.javaee.application.ClienteResumo(c.id, c.nome, c.email)
                from Cliente c order by c.nome
                """, ClienteResumo.class).getResultList();
    }
}
