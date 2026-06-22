package br.com.murilo.modules.course;

import br.com.murilo.domain.Course;
import br.com.murilo.repository.ICourseRepository;
import br.com.murilo.repository.hibernate.CourseHibernateRepository;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para {@link CourseHibernateRepository} (Hibernate Session API).
 *
 * <p><b>O que esta suite testa além do CRUD básico:</b>
 * <ul>
 *   <li>Uso de HQL com {@code FROM Course} (sem SELECT explícito)</li>
 *   <li>HQL bulk DELETE sem carregar entidade</li>
 *   <li>Bulk insert via {@code StatelessSession}</li>
 * </ul>
 *
 * <p><b>JUnit 5 features usadas:</b>
 * <ul>
 *   <li>{@code @TestMethodOrder} + {@code @Order} — controle de ordem de execução</li>
 *   <li>{@code @DisplayName} — nomes legíveis nos relatórios de teste</li>
 *   <li>{@code @Nested} — agrupa testes por funcionalidade</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CourseHibernateRepository — Hibernate Native Session Tests")
class CourseHibernateTest {

    private static final CourseHibernateRepository repository = new CourseHibernateRepository();

    private static Long createdId;

    @Test
    @Order(1)
    @DisplayName("deve persistir Course via Session.persist()")
    void shouldPersistCourse() {
        var course = new Course("HIB-001", "Hibernate Session API", "Acesso direto à Session nativa do Hibernate");

        var saved = repository.create(course);

        assertNotNull(saved.getId());
        createdId = saved.getId();
    }

    @Test
    @Order(2)
    @DisplayName("deve buscar Course via Session.get() — usa L1 cache se disponível")
    void shouldGetById() {
        Optional<Course> result = repository.findById(createdId);

        assertTrue(result.isPresent());
        assertEquals("HIB-001", result.get().getCode());
    }

    @Test
    @Order(3)
    @DisplayName("deve buscar Course por código via HQL")
    void shouldFindByCodeViaHql() {
        Optional<Course> result = repository.findByCode("HIB-001");

        assertTrue(result.isPresent());
        assertEquals("Hibernate Session API", result.get().getName());
    }

    @Nested
    @DisplayName("Bulk Operations — StatelessSession e HQL Mutation")
    class BulkOperations {

        @Test
        @DisplayName("deve inserir múltiplos cursos via StatelessSession sem overhead de L1 cache")
        void shouldBulkInsertViaStatelessSession() {
            var courses = List.of(
                    new Course("HIB-BULK-01", "Batch Processing I",  "Insert em lote sem cache"),
                    new Course("HIB-BULK-02", "Batch Processing II", "Update em lote sem dirty check"),
                    new Course("HIB-BULK-03", "Batch Processing III","Delete em lote via HQL")
            );

            assertDoesNotThrow(() -> repository.bulkInsert(courses));

            // Verifica que os cursos foram inseridos
            var all = repository.findAll();
            var codes = all.stream().map(Course::getCode).toList();
            assertTrue(codes.containsAll(List.of("HIB-BULK-01", "HIB-BULK-02", "HIB-BULK-03")));
        }

        @Test
        @DisplayName("deve executar HQL DELETE sem carregar entidades em memória")
        void shouldDeleteViaHqlWithoutLoadingEntity() {
            // HQL DELETE — sem SELECT anterior
            assertDoesNotThrow(() -> {
                var allBefore = repository.findAll().size();
                repository.deleteById(createdId);
                var allAfter = repository.findAll().size();

                assertEquals(allBefore - 1, allAfter);
            });
        }
    }

    @Test
    @Order(99)
    @DisplayName("deve confirmar remoção do Course criado")
    void shouldConfirmDeletion() {
        Optional<Course> deleted = repository.findById(createdId);
        assertTrue(deleted.isEmpty());
    }
}
