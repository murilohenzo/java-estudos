package lambdas;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

/**
 * Casos de uso práticos combinando interfaces funcionais.
 */
public class UseCasesDemo {

    record Pedido(String id, String cliente, double valor, boolean pago) {}

    record UserEntity(long id, String name, String email) {}

    record UserDto(String displayName, String email) {}

    public static void main(String[] args) {
        System.out.println("=== Casos de uso — interfaces funcionais ===\n");

        demoValidationPipeline();
        demoRetryWithSupplier();
        demoMapMerge();
        demoDomainPipeline();
        demoEventBiConsumer();
    }

    // 1. Validação composável com Predicate
    static void demoValidationPipeline() {
        System.out.println("--- 1. Validação com Predicate ---");
        List<Predicate<Pedido>> rules = List.of(
                p -> p.id() != null && !p.id().isBlank(),
                p -> p.valor() > 0,
                p -> p.pago(),
                p -> p.cliente() != null && p.cliente().length() >= 2
        );

        Pedido ok = new Pedido("P1", "Ana", 99.9, true);
        Pedido bad = new Pedido("P2", "A", -1, false);

        System.out.println("P1 válido? " + validate(ok, rules));
        System.out.println("P2 válido? " + validate(bad, rules));
        System.out.println("falhas P2 = " + failures(bad, rules));
        System.out.println();
    }

    static boolean validate(Pedido pedido, List<Predicate<Pedido>> rules) {
        return rules.stream().allMatch(rule -> rule.test(pedido));
    }

    static List<Integer> failures(Pedido pedido, List<Predicate<Pedido>> rules) {
        List<Integer> failed = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            if (!rules.get(i).test(pedido)) {
                failed.add(i);
            }
        }
        return failed;
    }

    // 2. Retry com Supplier
    static void demoRetryWithSupplier() {
        System.out.println("--- 2. Retry com Supplier ---");
        AtomicInteger attempts = new AtomicInteger();
        Supplier<String> flaky = () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("falha temporária #" + attempts.get());
            }
            return "sucesso na tentativa " + attempts.get();
        };

        System.out.println(retry(flaky, 5));
        System.out.println();
    }

    static <T> T retry(Supplier<T> action, int maxAttempts) {
        RuntimeException last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                return action.get();
            } catch (RuntimeException ex) {
                last = ex;
                System.out.println("  tentativa " + i + " falhou: " + ex.getMessage());
            }
        }
        throw last;
    }

    // 3. Map.merge + BinaryOperator
    static void demoMapMerge() {
        System.out.println("--- 3. Map.merge com BinaryOperator ---");
        List<String> words = List.of("java", "stream", "java", "lambda", "stream", "java");
        Map<String, Integer> freq = new LinkedHashMap<>();
        BinaryOperator<Integer> sum = Integer::sum;

        for (String w : words) {
            freq.merge(w, 1, sum);
        }
        System.out.println(freq);
        System.out.println();
    }

    // 4. Pipeline Function Entity → DTO → texto
    static void demoDomainPipeline() {
        System.out.println("--- 4. Pipeline Function de domínio ---");
        Function<UserEntity, UserDto> toDto =
                u -> new UserDto(u.name().toUpperCase(), u.email().toLowerCase());
        Function<UserDto, String> toLine =
                d -> d.displayName() + " <" + d.email() + ">";

        Function<UserEntity, String> pipeline = toDto.andThen(toLine);

        UserEntity user = new UserEntity(1L, "Murilo", "Murilo@Example.COM");
        System.out.println(pipeline.apply(user));
        System.out.println();
    }

    // 5. BiConsumer como handler de evento
    static void demoEventBiConsumer() {
        System.out.println("--- 5. BiConsumer de eventos ---");
        record Event(String type, String payload) {}
        record Ctx(String correlationId) {}

        BiConsumer<Event, Ctx> handler = (event, ctx) ->
                System.out.println("  [" + ctx.correlationId() + "] "
                        + event.type() + " → " + event.payload());

        handler.accept(new Event("ORDER_CREATED", "P1"), new Ctx("corr-99"));
        handler.accept(new Event("ORDER_PAID", "P1"), new Ctx("corr-99"));
        System.out.println();
    }
}
