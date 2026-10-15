package br.com.murilo.javaee.infrastructure;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Dependent
public class PersistenceResources {
    // A injeção de recurso JPA é realizada pelo contêiner, não por construtor.
    @Produces
    @PersistenceContext(unitName = "estudosPU")
    private EntityManager entityManager;
}
