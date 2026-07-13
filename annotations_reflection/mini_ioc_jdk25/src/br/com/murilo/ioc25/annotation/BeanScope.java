package br.com.murilo.ioc25.annotation;

/**
 * Escopo de ciclo de vida do bean — espelha a ideia de singleton/prototype do Spring.
 */
public enum BeanScope {

    /** Uma única instância compartilhada no container (thread-safe via registry). */
    SINGLETON,

    /** Nova instância a cada {@code getBean}. */
    PROTOTYPE
}
