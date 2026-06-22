package br.com.murilo.enums;

/**
 * Status do ciclo de vida de uma {@link br.com.murilo.domain.Registration}.
 *
 * <p><b>JPA note:</b> Persiste como String ({@code @Enumerated(EnumType.STRING)})
 * na entidade Registration. Nunca use {@code EnumType.ORDINAL} — a ordem dos
 * valores do enum não deve ser uma garantia de contrato com o banco de dados.
 */
public enum Status {

    /** Matrícula ativa: aluno com acesso ao conteúdo do curso. */
    ACTIVE,

    /** Matrícula suspensa temporariamente (ex: inadimplência). */
    INACTIVE,

    /** Matrícula aguardando confirmação de pagamento. */
    PENDING,

    /** Matrícula cancelada definitivamente. */
    CANCELLED;

    /**
     * Factory method seguro — evita {@link IllegalArgumentException} em strings inválidas.
     * JDK 21 switch expression com pattern matching.
     */
    public static java.util.Optional<Status> fromString(String value) {
        if (value == null) return java.util.Optional.empty();
        return switch (value.toUpperCase()) {
            case "ACTIVE"    -> java.util.Optional.of(ACTIVE);
            case "INACTIVE"  -> java.util.Optional.of(INACTIVE);
            case "PENDING"   -> java.util.Optional.of(PENDING);
            case "CANCELLED" -> java.util.Optional.of(CANCELLED);
            default          -> java.util.Optional.empty();
        };
    }
}

