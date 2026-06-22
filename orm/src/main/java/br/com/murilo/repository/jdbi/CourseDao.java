package br.com.murilo.repository.jdbi;

import br.com.murilo.domain.CourseRecord;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.List;
import java.util.Optional;

/**
 * <b>JDBI SQL Object</b> — interface declarativa de acesso ao banco.
 *
 * <h2>O que é SQL Object?</h2>
 * <p>JDBI SQL Object Plugin gera automaticamente uma implementação desta interface
 * em runtime. Cada anotação {@code @Sql*} mapeia para uma operação JDBC:
 *
 * <table border="1" cellpadding="4">
 *   <caption>Anotações SQL Object</caption>
 *   <tr><th>Anotação</th><th>Operação</th><th>Statement SQL</th></tr>
 *   <tr><td>{@code @SqlQuery}</td><td>SELECT</td><td>Retorna resultados mapeados</td></tr>
 *   <tr><td>{@code @SqlUpdate}</td><td>INSERT/UPDATE/DELETE</td><td>Retorna int (linhas afetadas)</td></tr>
 *   <tr><td>{@code @SqlBatch}</td><td>Batch INSERT/UPDATE</td><td>Aceita Iterable de parâmetros</td></tr>
 *   <tr><td>{@code @GetGeneratedKeys}</td><td>—</td><td>Retorna chave gerada após INSERT</td></tr>
 * </table>
 *
 * <h2>Mapeamento de ResultSet → Record</h2>
 * <p>{@code @RegisterConstructorMapper(CourseRecord.class)} instrui o JDBI a mapear
 * cada linha do ResultSet para um {@link CourseRecord} usando o construtor canônico.
 * Os nomes das colunas SQL devem corresponder aos nomes dos componentes do record
 * (case-insensitive por padrão).
 *
 * <h2>Parâmetros de Bind</h2>
 * <ul>
 *   <li>{@code @Bind("name")} — bind por nome de parâmetro SQL {@code :name}</li>
 *   <li>{@code @BindBean} — bind automático de todos os getters/campos do objeto</li>
 * </ul>
 *
 * <h2>SQL Text Blocks (JDK 15+)</h2>
 * <p>As queries usam text blocks para legibilidade. O JDBI processa a string
 * literalmente, portanto indentação e quebras de linha são preservadas mas
 * não afetam a execução SQL.
 */
@RegisterConstructorMapper(CourseRecord.class)
public interface CourseDao {

    /**
     * Insere um curso e retorna o ID gerado pelo banco.
     *
     * <p>{@code @GetGeneratedKeys} instrui o JDBI a executar o statement com
     * {@code Statement.RETURN_GENERATED_KEYS} e mapear a primeira coluna retornada
     * para o tipo de retorno ({@code Long}).
     */
    @SqlUpdate("""
            INSERT INTO TB_COURSE (code, name, description)
            VALUES (:code, :name, :description)
            """)
    @GetGeneratedKeys("id")
    long insert(@Bind("code") String code,
                @Bind("name") String name,
                @Bind("description") String description);

    /**
     * Busca curso por ID — retorna {@code Optional} vazio se não encontrado.
     * JDBI 3 suporta {@code Optional<T>} diretamente como tipo de retorno.
     */
    @SqlQuery("""
            SELECT id, code, name, description
            FROM TB_COURSE
            WHERE id = :id
            """)
    Optional<CourseRecord> findById(@Bind("id") long id);

    /**
     * Busca curso por código (case-insensitive via UPPER()).
     * Caso o índice seja definido em UPPER(code), esta query usará o index scan.
     */
    @SqlQuery("""
            SELECT id, code, name, description
            FROM TB_COURSE
            WHERE UPPER(code) = UPPER(:code)
            """)
    Optional<CourseRecord> findByCode(@Bind("code") String code);

    /** Retorna todos os cursos ordenados alfabeticamente pelo nome. */
    @SqlQuery("""
            SELECT id, code, name, description
            FROM TB_COURSE
            ORDER BY name ASC
            """)
    List<CourseRecord> findAll();

    /**
     * Atualiza nome e descrição de um curso existente.
     *
     * <p>{@code @BindBean} mapeia automaticamente os campos/getters do
     * {@link CourseRecord} para os parâmetros SQL nomeados com o mesmo identificador.
     * {@code courseRecord.code()} → {@code :code}, {@code courseRecord.name()} → {@code :name}, etc.
     *
     * @return número de linhas afetadas (0 se ID não encontrado)
     */
    @SqlUpdate("""
            UPDATE TB_COURSE
            SET name = :name, description = :description
            WHERE id = :id
            """)
    int update(@BindBean CourseRecord courseRecord);

    /**
     * Remove um curso pelo ID.
     *
     * @return número de linhas afetadas (0 se ID não encontrado)
     */
    @SqlUpdate("DELETE FROM TB_COURSE WHERE id = :id")
    int deleteById(@Bind("id") long id);

    /**
     * <b>Batch INSERT</b> — insere múltiplos cursos em uma única roundtrip JDBC.
     *
     * <p>{@code @SqlBatch} instrui o JDBI a usar {@code PreparedStatement.addBatch()}
     * e {@code executeBatch()}, agrupando todos os INSERTs em uma única chamada ao driver.
     * Para grandes volumes, isto é 10–100x mais eficiente que INSERTs individuais.
     *
     * @param codes        lista de códigos
     * @param names        lista de nomes (posição corresponde ao código)
     * @param descriptions lista de descrições
     * @return array com o número de linhas afetadas por cada statement no batch
     */
    @SqlBatch("""
            INSERT INTO TB_COURSE (code, name, description)
            VALUES (:code, :name, :description)
            """)
    int[] batchInsert(@Bind("code") List<String> codes,
                      @Bind("name") List<String> names,
                      @Bind("description") List<String> descriptions);
}
