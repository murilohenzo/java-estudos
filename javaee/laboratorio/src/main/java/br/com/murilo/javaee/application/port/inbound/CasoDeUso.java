package br.com.murilo.javaee.application.port.inbound;

import jakarta.inject.Qualifier;
import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface CasoDeUso {}
