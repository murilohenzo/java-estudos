package br.com.murilo.javaee.adapters.inbound.ejb;

import br.com.murilo.javaee.domain.model.RegistroAuditoria;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class AuditoriaEJB {
    @PersistenceContext(unitName = "estudosPU")
    private EntityManager manager;

    public AuditoriaEJB() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void registrar(String descricao) {
        manager.persist(new RegistroAuditoria(descricao));
    }
}
