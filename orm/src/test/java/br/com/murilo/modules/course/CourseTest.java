package br.com.murilo.modules.course;

import br.com.murilo.domain.Course;
import br.com.murilo.repository.ICourseRepository;
import br.com.murilo.repository.jpa.CourseJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de smoke para garantir que o bootstrap JPA funciona corretamente.
 * Para testes completos de cada implementação, veja:
 *
 * @see CourseJpaTest
 * @see CourseHibernateTest
 * @see CourseJdbiTest
 */
@DisplayName("CourseTest — Smoke Test (JPA)")
class CourseTest {

    private final ICourseRepository courseRepository = new CourseJpaRepository();

    @Test
    @DisplayName("deve criar um Course via JPA e retornar ID gerado")
    void whenNewCourseInformedThenShouldBeCreated() {
        var course = new Course("SMOKE-001", "Curso de Java Backend", "Smoke test de persistência JPA");

        var saved = courseRepository.create(course);

        assertNotNull(saved.getId(), "ID deve ser gerado pelo banco após persist");
    }
}
