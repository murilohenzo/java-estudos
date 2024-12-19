package reflections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação personalizada para marcar métodos que precisam ser auditados.
 *
 * <p>Essa anotação pode ser aplicada apenas em métodos, e sua retenção é em tempo de execução.</p>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * @Auditable(action = "user-login")
 * public void login() {
 *     // Método sujeito a auditoria
 * }
 * }</pre>
 *
 * @author
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME) // Disponível em tempo de execução
@Target(ElementType.METHOD) // Aplicável apenas a métodos
public @interface Auditable {
    String action() default "default-action"; // Define um parâmetro com valor padrão
}
