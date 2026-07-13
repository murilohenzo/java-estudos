package br.com.murilo.ioc25.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca ponto de injeção de dependência (análogo a {@code @Autowired} / {@code @Inject}).
 * Preferir construtor; campo é suportado para demonstração.
 */
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
