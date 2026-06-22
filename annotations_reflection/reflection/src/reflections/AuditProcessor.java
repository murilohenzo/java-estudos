package reflections;

import java.lang.reflect.Method;

/**
 * Processador de auditoria que analisa métodos anotados com @Auditable.
 *
 * <p>Essa classe utiliza Reflection para verificar quais métodos de uma determinada classe estão
 * anotados com a annotation {@link Auditable}, e executa ações baseadas nos atributos configurados.</p>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * AuditProcessor processor = new AuditProcessor();
 * processor.processAuditableMethods(UserService.class);
 * }</pre>
 *
 * @author
 * @since 1.0
 */
public class AuditProcessor {

    /**
     * Processa uma classe e executa auditoria em métodos anotados com {@link Auditable}.
     *
     * @param clazz A classe a ser processada.
     * @throws NullPointerException Se a classe fornecida for nula.
     * @since 1.0
     */
    public void processAuditableMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            // Verifica se o método tem a anotação Auditable
            if (method.isAnnotationPresent(Auditable.class)) {
                Auditable annotation = method.getAnnotation(Auditable.class);
                System.out.printf("Método: %s | Ação: %s%n", method.getName(), annotation.action());
            }
        }
    }

    public static void main(String[] args) {
        AuditProcessor processor = new AuditProcessor();
        processor.processAuditableMethods(UserService.class);
    }
}
