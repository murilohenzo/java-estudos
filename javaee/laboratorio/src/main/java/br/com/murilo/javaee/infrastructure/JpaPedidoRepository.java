package br.com.murilo.javaee.infrastructure;

import br.com.murilo.javaee.application.*;
import br.com.murilo.javaee.domain.Pedido;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class JpaPedidoRepository implements PedidoRepository {
    private final EntityManager manager;

    protected JpaPedidoRepository() { this.manager = null; }

    @Inject
    public JpaPedidoRepository(EntityManager manager) { this.manager = manager; }

    @Override
    public Pedido salvar(Pedido pedido) {
        manager.persist(pedido);
        return pedido;
    }

    @Override
    public List<PedidoResumo> listarPorCliente(Long clienteId) {
        return manager.createQuery("""
                select new br.com.murilo.javaee.application.PedidoResumo(p.id, p.descricao)
                from Pedido p where p.cliente.id = :clienteId order by p.id
                """, PedidoResumo.class)
                .setParameter("clienteId", clienteId).getResultList();
    }
}
