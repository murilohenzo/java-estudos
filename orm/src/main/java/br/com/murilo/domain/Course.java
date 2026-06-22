package br.com.murilo.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade JPA que representa um Curso.
 *
 * <p><b>Design decisions:</b>
 * <ul>
 *   <li>{@code @Table(indexes)} — índice no campo {@code code} melhora performance em buscas
 *       por código sem full table scan.</li>
 *   <li>{@code FetchType.LAZY} no {@code @OneToMany} — evita N+1 no carregamento de cursos
 *       sem necessidade de matriculas. Sempre prefira LAZY em coleções e EAGER somente quando
 *       o dado é garantidamente necessário (raro).</li>
 *   <li>{@code CascadeType.ALL} — propaga operações persist/merge/remove para matriculas.
 *       Use com cautela: remover um Course remove todas as Registrations associadas.</li>
 *   <li>{@code orphanRemoval = true} — garante que um Registration sem Course pai
 *       seja deletado automaticamente do banco, prevenindo dados órfãos.</li>
 *   <li>{@code @SequenceGenerator(allocationSize = 50)} — o Hibernate pré-aloca 50 IDs
 *       em memória por request ao banco, reduzindo chamadas à sequence em 50x.
 *       Ajuste conforme o volume de inserts esperado.</li>
 * </ul>
 *
 * <p><b>JDK 21 note:</b> Esta classe mantém mutabilidade pois JPA exige construtor
 * sem argumentos e setters para merge de entidades gerenciadas. Para read models
 * imutáveis, use {@link CourseRecord}.
 */
@Entity
@Table(
        name = "TB_COURSE",
        indexes = {
                @Index(name = "idx_course_code", columnList = "code", unique = true)
        }
)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_seq")
    @SequenceGenerator(
            name           = "course_seq",
            sequenceName   = "sq_course",
            initialValue   = 1,
            allocationSize = 50
    )
    private Long id;

    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    /**
     * Mapeamento bidirecional Course → Registration.
     * {@code mappedBy = "course"} indica que Registration é o lado "dono" da FK.
     */
    @OneToMany(
            mappedBy      = "course",
            cascade       = CascadeType.ALL,
            fetch         = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<Registration> registrations = new ArrayList<>();

    /** Construtor sem args exigido pela especificação JPA. */
    protected Course() {}

    public Course(String code, String name, String description) {
        this.code        = code;
        this.name        = name;
        this.description = description;
    }

    public void addRegistration(Registration registration) {
        registrations.add(registration);
        registration.setCourse(this);
    }

    public void removeRegistration(Registration registration) {
        registrations.remove(registration);
        registration.setCourse(null);
    }

    public Long getId()          { return id; }
    public String getCode()      { return code; }
    public String getName()      { return name; }
    public String getDescription() { return description; }
    public List<Registration> getRegistrations() { return List.copyOf(registrations); }

    public void setCode(String code)             { this.code = code; }
    public void setName(String name)             { this.name = name; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course c)) return false;  // JDK 16+ pattern matching instanceof
        return Objects.equals(code, c.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return "Course{id=%d, code='%s', name='%s'}".formatted(id, code, name);
    }
}
