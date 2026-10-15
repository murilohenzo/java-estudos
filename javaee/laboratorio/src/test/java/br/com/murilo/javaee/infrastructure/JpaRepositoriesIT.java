package br.com.murilo.javaee.infrastructure;

import br.com.murilo.javaee.domain.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class JpaRepositoriesIT {
    private static EntityManagerFactory factory;
    private EntityManager manager;

    @BeforeAll
    static void iniciar() {
        factory = Persistence.createEntityManagerFactory("testePU");
    }

    @AfterAll
    static void encerrar() { if (factory != null) factory.close(); }

    @BeforeEach
    void abrir() { manager = factory.createEntityManager(); }

    @AfterEach
    void fechar() { if (manager != null) manager.close(); }

    @Test
    void dadoClienteSemPedido_quandoLeftJoin_entaoPreservaCliente() {
        manager.getTransaction().begin();
        Cliente cliente = new Cliente("Bruno", "bruno@teste.com");
        manager.persist(cliente);
        manager.getTransaction().commit();
        Long id = cliente.getId();

        var resultado = manager.createQuery("""
                select c from Cliente c left join c.pedidos p where c.id = :id
                """, Cliente.class).setParameter("id", id).getResultList();
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getPedidos().isEmpty());
    }

    @Test
    void dadoIdExterno_quandoPersistir_entaoNaoExigeGeneratedValue() {
        manager.getTransaction().begin();
        manager.persist(new ClienteExterno("ERP-01", "Integração"));
        manager.getTransaction().commit();
        assertNotNull(manager.find(ClienteExterno.class, "ERP-01"));
    }
}
