package br.com.murilo.domain;

import br.com.murilo.enums.Status;

import java.time.Instant;
import java.util.List;

/**
 * Read model imutável para Curso — JDK 16+ {@code record}.
 *
 * <p><b>Por que usar {@code record} aqui?</b>
 * <ul>
 *   <li>Records são imutáveis por padrão: todos os campos são {@code final}.</li>
 *   <li>O compilador gera automaticamente construtor canônico, {@code equals()},
 *       {@code hashCode()} e {@code toString()} baseados em todos os componentes.</li>
 *   <li>Ideal para <em>read models</em>, DTOs de API, respostas JDBI e projeções JPA
 *       — situações onde não há necessidade de ciclo de vida gerenciado pelo ORM.</li>
 *   <li><b>NÃO usar como entidade JPA</b>: JPA exige construtor sem args e mutabilidade.</li>
 * </ul>
 *
 * <p><b>JDBI:</b> JDBI 3 mapeia ResultSet → record via
 * {@code @RegisterConstructorMapper(CourseRecord.class)}, usando o construtor
 * canônico com os nomes dos componentes mapeados para colunas SQL.
 *
 * <p><b>JPA Projection:</b> Pode ser usado como target de JPQL constructor expression:
 * <pre>{@code
 *   SELECT new br.com.murilo.domain.CourseRecord(c.id, c.code, c.name, c.description)
 *   FROM Course c
 * }</pre>
 *
 * @param id          Identificador gerado pelo banco de dados
 * @param code        Código único do curso (ex: "JAVA-001")
 * @param name        Nome completo do curso
 * @param description Descrição detalhada do conteúdo
 */
public record CourseRecord(Long id, String code, String name, String description) {

    /**
     * Compact constructor — validações executadas antes de atribuição dos campos.
     * Em records, {@code this.field = value} é feito automaticamente após o compact constructor.
     */
    public CourseRecord {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Course code must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Course name must not be blank");
        }
        code = code.toUpperCase().strip();
        name = name.strip();
    }

    /**
     * Factory method para criar um CourseRecord sem ID (antes do INSERT).
     * ID será {@code null} até que o banco retorne a chave gerada.
     */
    public static CourseRecord of(String code, String name, String description) {
        return new CourseRecord(null, code, name, description);
    }

    /**
     * Retorna um novo record com o ID preenchido após persistência.
     * Records são imutáveis — atualização gera nova instância (copy-with pattern).
     */
    public CourseRecord withId(Long id) {
        return new CourseRecord(id, this.code, this.name, this.description);
    }

    /** Converte para a entidade JPA gerenciável pelo ORM. */
    public Course toEntity() {
        return new Course(code, name, description);
    }
}

/**
 * Read model imutável para Matrícula com dados agregados do Curso.
 *
 * <p>Usado em queries JDBI que fazem JOIN entre TB_REGISTRATION e TB_COURSE,
 * retornando uma visão desnormalizada sem necessidade de lazy loading.
 */
record RegistrationRecord(
        Long id,
        String code,
        Double value,
        Status status,
        Instant createdAt,
        Long courseId,
        String courseName
) {
    public RegistrationRecord {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Registration code must not be blank");
        }
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Registration value must be non-negative");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
    }
}
