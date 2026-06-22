package br.com.murilo.repository;

import br.com.murilo.domain.Course;
import br.com.murilo.domain.CourseRecord;

import java.util.List;
import java.util.Optional;

/**
 * Contrato do repositório de Cursos.
 *
 * <p>Esta interface é implementada por três estratégias distintas de acesso a dados,
 * cada uma com trade-offs específicos de complexidade, performance e portabilidade:
 *
 * <ul>
 *   <li>{@code jpa.CourseJpaRepository} — Pure JPA ({@code jakarta.persistence.*})</li>
 *   <li>{@code hibernate.CourseHibernateRepository} — Hibernate Session API nativa</li>
 *   <li>{@code jdbi.CourseJdbiRepository} — JDBI 3 SQL-first com HikariCP</li>
 * </ul>
 *
 * <p><b>Design note:</b> A interface expõe {@link Course} (entidade mutável) e
 * {@link CourseRecord} (read model imutável). O caller escolhe o tipo de retorno
 * adequado para seu contexto: entidade para write operations,
 * record para projections e read models.
 */
public interface ICourseRepository {

    /**
     * Persiste um novo Curso no banco de dados.
     *
     * @param course entidade a ser persistida (id deve ser null)
     * @return a mesma entidade com o id preenchido pelo banco
     */
    Course create(Course course);

    /**
     * Busca um Curso pelo seu identificador primário.
     *
     * @param id chave primária gerada
     * @return {@link Optional#empty()} se não encontrado
     */
    Optional<Course> findById(Long id);

    /**
     * Busca um Curso pelo código único.
     *
     * @param code código do curso (case-insensitive na implementação JPA)
     * @return {@link Optional#empty()} se não encontrado
     */
    Optional<Course> findByCode(String code);

    /**
     * Retorna todos os Cursos cadastrados.
     * <p><b>Cuidado:</b> em produção com volume alto, use paginação.
     */
    List<Course> findAll();

    /**
     * Retorna todos os Cursos como {@link CourseRecord} (read model imutável).
     * <p>Preferível para listagens em APIs REST onde não há necessidade de
     * gerenciamento de ciclo de vida pelo ORM.
     */
    List<CourseRecord> findAllAsRecords();

    /**
     * Atualiza os dados de um Curso existente.
     *
     * @param course entidade com id preenchido e novos valores
     * @return entidade após merge/update
     */
    Course update(Course course);

    /**
     * Remove um Curso pelo seu identificador.
     *
     * @param id chave primária do curso a remover
     */
    void deleteById(Long id);
}
