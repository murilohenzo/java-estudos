package br.com.murilo.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface Anotacao {
    String value();

    String[] bairros();

    long numeroDaCasa();

    double valores() default 10d;
}
