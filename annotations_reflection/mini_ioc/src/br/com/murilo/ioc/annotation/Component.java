package br.com.murilo.ioc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca uma classe como bean gerenciado pelo container (análogo a {@code @Component} do Spring).
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    /** Nome explícito do bean; vazio = simple name da classe com 1ª letra minúscula. */
    String value() default "";

    BeanScope scope() default BeanScope.SINGLETON;
}
