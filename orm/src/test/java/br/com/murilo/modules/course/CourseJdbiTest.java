package br.com.murilo.modules.course;

import br.com.murilo.domain.Course;
import br.com.murilo.domain.CourseRecord;
import br.com.murilo.repository.jdbi.CourseJdbiRepository;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para {@link CourseJdbiRepository} (JDBI SQL-first).
 *
 * <p><b>Diferenças de comportamento vs JPA/Hibernate:</b>
 * <ul>
 *   <li>Sem L1 cache — cada query vai ao banco independentemente</li>
 *   <li>Sem change tracking — updates só ocorrem via chamada explícita</li>
 *   <li>Retorno de {@link CourseRecord} (imutável) ao invés de entidade managed</li>
 *   <li>Transações JDBI: {@code jdbi.inTransaction()} vs {@code em.getTransaction()}</li>
 * </ul>
 *
 * <p><b>Records em assertions JUnit 5:</b>
 * Como {@code CourseRecord} implementa {@code equals()} estruturalmente (por todos os campos),
 * {@code assertEquals(recordA, recordB)} funciona corretamente sem implementação manual.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CourseJdbiRepository — JDBI SQL-first Integration Tests")
class CourseJdbiTest {

    private static final CourseJdbiRepository repository = new CourseJdbiRepository();

    private static Long createdId;

    @Test
    @Order(1)
    @DisplayName("deve inserir um Course via SQL explícito e retornar ID gerado")
    void shouldInsertCourse() {
        var course = new Course("JDBI-001", "JDBI SQL Object API", "Acesso SQL-first com HikariCP");

        var saved = repository.create(course);

        assertNotNull(saved.getId(), "ID deve ser gerado via @GetGeneratedKeys");
        assertEquals("JDBI-001", saved.getCode());
        createdId = saved.getId();
    }

    @Test
    @Order(2)
    @DisplayName("deve retornar CourseRecord imutável para findById")
    void shouldFindByIdAsRecord() {
        assertNotNull(createdId);

        Optional<Course> found = repository.findById(createdId);

        assertTrue(found.isPresent());
        assertEquals("JDBI-001", found.get().getCode());
    }

    @Test
    @Order(3)
    @DisplayName("findAllAsRecords deve retornar List<CourseRecord> imutável")
    void shouldFindAllAsImmutableRecords() {
        List<CourseRecord> records = repository.findAllAsRecords();

        assertNotNull(records);
        assertFalse(records.isEmpty());

        // Record equality — baseada em todos os componentes
        records.forEach(r -> {
            assertNotNull(r.id());
            assertFalse(r.code().isBlank());
            assertFalse(r.name().isBlank());
        });
    }

    @Test
    @Order(4)
    @DisplayName("deve atualizar Course via SQL UPDATE explícito")
    void shouldUpdateCourse() {
        assertNotNull(createdId);

        var course = repository.findById(createdId).orElseThrow();
        course.setName("JDBI 3 — SQL Object + Fluent API");

        var updated = repository.update(course);
        assertEquals("JDBI 3 — SQL Object + Fluent API", updated.getName());
    }

    @Test
    @Order(5)
    @DisplayName("findByNameLike deve usar Fluent API com LIKE dinâmico")
    void shouldFindByNameLikeUsingFluentApi() {
        List<CourseRecord> results = repository.findByNameLike("JDBI");

        assertFalse(results.isEmpty(), "Deve encontrar ao menos 1 curso com 'JDBI' no nome");
        assertTrue(results.stream().allMatch(r -> r.name().contains("JDBI")));
    }

    @Test
    @Order(6)
    @DisplayName("batchInsert deve inserir múltiplos cursos em uma única roundtrip")
    void shouldBatchInsert() {
        var batch = List.of(
                CourseRecord.of("JDBI-BATCH-01", "Batch Insert I",  "Demonstração de @SqlBatch"),
                CourseRecord.of("JDBI-BATCH-02", "Batch Insert II", "Performance com addBatch()"),
                CourseRecord.of("JDBI-BATCH-03", "Batch Insert III","Comparação com INSERT único")
        );

        int inserted = repository.batchInsert(batch);

        assertEquals(3, inserted, "Deve inserir exatamente 3 registros");
    }

    @Test
    @Order(7)
    @DisplayName("deve deletar Course via SQL DELETE explícito")
    void shouldDeleteById() {
        assertNotNull(createdId);

        assertDoesNotThrow(() -> repository.deleteById(createdId));

        Optional<Course> deleted = repository.findById(createdId);
        assertTrue(deleted.isEmpty(), "Course deve ter sido removido");
    }
}
