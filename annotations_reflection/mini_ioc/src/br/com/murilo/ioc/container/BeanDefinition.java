package br.com.murilo.ioc.container;

import br.com.murilo.ioc.annotation.BeanScope;

/**
 * Metadados imutáveis de um bean registrado — equivalente simplificado a {@code BeanDefinition}.
 */
public record BeanDefinition(
        String name,
        Class<?> type,
        BeanScope scope
) {
    public BeanDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("bean name obrigatório");
        }
        if (type == null) {
            throw new IllegalArgumentException("bean type obrigatório");
        }
        if (scope == null) {
            throw new IllegalArgumentException("bean scope obrigatório");
        }
    }
}
