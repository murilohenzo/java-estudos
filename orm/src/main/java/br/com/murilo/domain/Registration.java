package br.com.murilo.domain;

import br.com.murilo.enums.Status;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidade JPA que representa uma Matrícula de um aluno em um Curso.
 *
 * <p><b>Design decisions:</b>
 * <ul>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} — Registration é o lado "dono"
 *       da FK {@code course_id}. Lazy carrega o Course apenas quando acessado
 *       explicitamente; evita JOIN automático desnecessário.</li>
 *   <li>{@code @Enumerated(EnumType.STRING)} — persiste o nome do enum ({@code "ACTIVE"})
 *       ao invés do ordinal ({@code 0}). Alterar a ordem do enum não corrompe dados
 *       existentes no banco. <b>Sempre prefira STRING em enums JPA.</b></li>
 *   <li>{@code @Column(updatable = false)} no {@code createdAt} — campo de auditoria
 *       imutável; o banco de dados nunca deve atualizar a data de criação.</li>
 *   <li>{@code @PrePersist} — hook de ciclo de vida JPA que preenche automaticamente
 *       {@code createdAt} antes do INSERT, sem necessidade de set manual.</li>
 * </ul>
 */
@Entity
@Table(
        name    = "TB_REGISTRATION",
        indexes = {
                @Index(name = "idx_registration_code", columnList = "code", unique = true),
                @Index(name = "idx_registration_course", columnList = "course_id")
        }
)
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_seq")
    @SequenceGenerator(
            name           = "registration_seq",
            sequenceName   = "sq_registration",
            initialValue   = 1,
            allocationSize = 50
    )
    private Long id;

    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "value", nullable = false)
    private Double value;

    /**
     * EnumType.STRING: armazena "ACTIVE", "INACTIVE", "PENDING"
     * Nunca use EnumType.ORDINAL — renomear ou reordenar o enum quebra o banco.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    /**
     * Campo de auditoria: preenchido automaticamente pelo hook @PrePersist.
     * updatable = false garante imutabilidade após INSERT.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Lado "dono" do relacionamento N:1 com Course.
     * A FK course_id fica na tabela TB_REGISTRATION.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Construtor sem args exigido pela especificação JPA. */
    protected Registration() {}

    public Registration(String code, Double value, Status status, Course course) {
        this.code   = code;
        this.value  = value;
        this.status = status;
        this.course = course;
    }

    /** Hook de ciclo de vida: executado antes de todo INSERT. */
    @PrePersist
    private void onPrePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId()         { return id; }
    public String getCode()     { return code; }
    public Double getValue()    { return value; }
    public Status getStatus()   { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Course getCourse()   { return course; }

    public void setCode(String code)       { this.code = code; }
    public void setValue(Double value)     { this.value = value; }
    public void setStatus(Status status)   { this.status = status; }
    void setCourse(Course course)          { this.course = course; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registration r)) return false;
        return Objects.equals(code, r.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return "Registration{id=%d, code='%s', status=%s, value=%.2f}".formatted(id, code, status, value);
    }
}
