package br.com.murilo.modules.course;

import br.com.murilo.domain.Course;
import br.com.murilo.domain.CourseRecord;
import br.com.murilo.repository.ICourseRepository;
import br.com.murilo.repository.jpa.CourseJpaRepository;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para {@link CourseJpaRepository} (Pure JPA).
 *
 * <p><b>Escopo:</b> testes que requerem banco de dados real — PostgreSQL rodando em localhost.
 * Para execução em CI sem banco, anote a classe com {@code @Tag("integration")}
 * e configure o Surefire para pular tags em builds padrão.
 *
 * <p><b>Ordem de execução:</b> {@code @TestMethodOrder(OrderAnnotation.class)} garante
 * que create() seja executado antes de find(), e find() antes de delete().
 * Evite dependência entre testes sempre que possível — use {@code @BeforeEach}
 * para setup de dados.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CourseJpaRepository — Pure JPA Integration Tests")
class CourseJpaTest {

    private static final ICourseRepository repository = new CourseJpaRepository();

    private static Long createdId;

    @Test
    @Order(1)
    @DisplayName("deve persistir um novo Course e retornar com ID gerado")
    void shouldCreateCourse() {
        var course = new Course("JPA-001", "Java Persistence API Fundamentos", "Curso completo de JPA com Hibernate");

        var persisted = repository.create(course);

        assertNotNull(persisted.getId(), "ID deve ser gerado pelo banco");
        assertEquals("JPA-001", persisted.getCode());
        createdId = persisted.getId();
    }

    @Test
    @Order(2)
    @DisplayName("deve encontrar um Course pelo ID")
    void shouldFindById() {
        assertNotNull(createdId, "Depende de shouldCreateCourse");

        Optional<Course> found = repository.findById(createdId);

        assertTrue(found.isPresent());
        assertEquals("JPA-001", found.get().getCode());
    }

    @Test
    @Order(3)
    @DisplayName("deve encontrar um Course pelo código (case-insensitive)")
    void shouldFindByCode() {
        Optional<Course> found = repository.findByCode("jpa-001");

        assertTrue(found.isPresent(), "Busca deve ser case-insensitive");
        assertEquals("JPA-001", found.get().getCode().toUpperCase());
    }

    @Test
    @Order(4)
    @DisplayName("deve retornar Optional vazio para código inexistente")
    void shouldReturnEmptyForUnknownCode() {
        Optional<Course> found = repository.findByCode("INEXISTENTE-999");
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("deve retornar todos os cursos como List<CourseRecord> (projection)")
    void shouldFindAllAsRecords() {
        List<CourseRecord> records = repository.findAllAsRecords();

        assertNotNull(records);
        // Verifica imutabilidade: o record é imutável por spec JDK 21
        assertFalse(records.isEmpty());
        records.forEach(r -> {
            assertNotNull(r.id());
            assertNotNull(r.code());
            assertFalse(r.name().isBlank());
        });
    }

    @Test
    @Order(6)
    @DisplayName("deve atualizar o nome de um Course existente")
    void shouldUpdateCourse() {
        assertNotNull(createdId, "Depende de shouldCreateCourse");

        var course = repository.findById(createdId).orElseThrow();
        course.setName("JPA Avançado — Hibernate 6");

        var updated = repository.update(course);
        assertEquals("JPA Avançado — Hibernate 6", updated.getName());
    }

    @Test
    @Order(7)
    @DisplayName("deve deletar um Course pelo ID")
    void shouldDeleteById() {
        assertNotNull(createdId, "Depende de shouldCreateCourse");

        assertDoesNotThrow(() -> repository.deleteById(createdId));

        Optional<Course> deleted = repository.findById(createdId);
        assertTrue(deleted.isEmpty(), "Course deve estar removido do banco");
    }
}
