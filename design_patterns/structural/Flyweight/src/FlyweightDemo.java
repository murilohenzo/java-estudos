import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h1>Flyweight — Padrão Estrutural</h1>
 *
 * <h2>Intenção</h2>
 * <p>Usar compartilhamento para suportar eficientemente um grande número de objetos
 * de granularidade fina. Reduz o consumo de memória separando o estado <em>intrínseco</em>
 * (compartilhado, imutável) do estado <em>extrínseco</em> (único por instância, passado externamente).
 *
 * <h2>Estado intrínseco vs extrínseco</h2>
 * <pre>
 *   Intrínseco  — compartilhado entre muitas instâncias, IMUTÁVEL
 *                 Ex: tipo, cor, textura, fonte, sprite de personagem
 *
 *   Extrínseco  — único por instância, passado pelo contexto, MUTÁVEL
 *                 Ex: posição X,Y, velocidade, ID do objeto
 * </pre>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>O programa precisa criar um número enorme de objetos similares (partículas, caracteres)</li>
 *   <li>O consumo de memória é um problema (mobile, sistemas embarcados)</li>
 *   <li>Os objetos contêm estado duplicado que pode ser extraído e compartilhado</li>
 * </ul>
 *
 * <h2>Exemplos no JDK</h2>
 * <ul>
 *   <li>{@code Integer.valueOf()} — cache de -128 a 127</li>
 *   <li>{@code String.intern()} — String pool</li>
 *   <li>{@code Boolean.TRUE} / {@code Boolean.FALSE}</li>
 * </ul>
 *
 * <h2>Java 21</h2>
 * <p>Records são ideais para Flyweights: imutáveis por construção, {@code equals/hashCode}
 * gerados automaticamente (necessários para o cache), e {@code toString} informativo.
 */
public class FlyweightDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // FLYWEIGHT — estado intrínseco compartilhado (record = imutável por padrão)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * ParticleType é o Flyweight — compartilhado entre milhares de partículas.
     * Contém apenas dados que são iguais para todas as partículas do mesmo tipo.
     */
    record ParticleType(String name, String color, String sprite) {
        void render(int x, int y, double velocityX, double velocityY) {
            // Em um sistema real, aqui seria renderização de sprite
            // os parâmetros posicionais são EXTRÍNSECOS — não armazenados aqui
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FLYWEIGHT FACTORY — garante que apenas uma instância por tipo exista
    // ─────────────────────────────────────────────────────────────────────────

    static final class ParticleTypeFactory {
        private final Map<String, ParticleType> cache = new HashMap<>();
        private final AtomicInteger cacheHits   = new AtomicInteger();
        private final AtomicInteger cacheMisses = new AtomicInteger();

        /** Retorna flyweight existente ou cria um novo. */
        ParticleType get(String name, String color, String sprite) {
            var key = name + "-" + color;
            return cache.computeIfAbsent(key, k -> {
                cacheMisses.incrementAndGet();
                System.out.printf("  [Factory] criando ParticleType '%s'%n", key);
                return new ParticleType(name, color, sprite);
            });
        }

        int size()       { return cache.size(); }
        int hits()       { return cacheHits.get(); }
        int misses()     { return cacheMisses.get(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTEXT — combina flyweight (intrínseco) + estado extrínseco
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Particle é o contexto — cada partícula tem sua posição/velocidade (extrínseco),
     * mas compartilha o ParticleType (intrínseco) com milhares de outras.
     *
     * <p>Sem Flyweight: 1M partículas × ~200 bytes = ~200 MB
     * <p>Com Flyweight: 1M × ~32 bytes + 5 tipos × ~200 bytes = ~32 MB + 1 KB
     */
    record Particle(
            int x, int y,
            double velocityX, double velocityY,
            ParticleType type             // ← referência ao flyweight compartilhado
    ) {
        void update(int dx, int dy) {
            // Em produção, retornaria nova partícula atualizada (imutável)
        }

        void render() {
            type.render(x, y, velocityX, velocityY);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Exemplo JDK — Integer.valueOf() como Flyweight embutido
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateJdkFlyweight() {
        System.out.println("--- Flyweight no JDK: Integer cache [-128, 127] ---");
        Integer a = 100;   // Integer.valueOf(100) — usa cache
        Integer b = 100;
        Integer c = 1000;  // fora do cache — nova instância
        Integer d = 1000;

        System.out.println("  valueOf(100) == valueOf(100): " + (a == b));    // true  — mesma ref
        System.out.println("  valueOf(1000) == valueOf(1000): " + (c == d));  // false — refs diferentes
        System.out.println("  valueOf(1000).equals(1000): " + c.equals(d));   // true  — compare por valor
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Flyweight Pattern — Particle System ===\n");

        var factory = new ParticleTypeFactory();
        var rng = new Random(42);

        // Tipos de partículas (apenas 5 tipos diferentes)
        var types = new String[]{"fire", "smoke", "water", "spark", "debris"};
        var colors = new String[]{"#FF4500", "#888888", "#4FC3F7", "#FFD700", "#795548"};
        var sprites = new String[]{"fire.png", "smoke.png", "water.png", "spark.png", "debris.png"};

        // Criar 1.000 partículas — cada uma com posição única mas tipo compartilhado
        int PARTICLES = 1_000;
        var particles = new Particle[PARTICLES];

        System.out.println("Criando " + PARTICLES + " partículas...");
        for (int i = 0; i < PARTICLES; i++) {
            var typeIdx = rng.nextInt(types.length);
            var flyweight = factory.get(types[typeIdx], colors[typeIdx], sprites[typeIdx]);
            particles[i] = new Particle(
                    rng.nextInt(1920), rng.nextInt(1080),   // posição extrínseca
                    rng.nextDouble() * 2 - 1,               // velocidade extrínseca
                    rng.nextDouble() * 2 - 1,
                    flyweight                               // tipo compartilhado
            );
        }

        System.out.println("\n--- Resultado ---");
        System.out.printf("  Partículas criadas:       %,d%n", PARTICLES);
        System.out.printf("  ParticleTypes no cache:   %d (de %d tipos únicos)%n",
                factory.size(), types.length);
        System.out.printf("  Cache misses (criações):  %d%n", factory.misses());

        // Distribuição por tipo
        System.out.println("\n--- Distribuição por tipo ---");
        for (var type : types) {
            var count = java.util.Arrays.stream(particles)
                    .filter(p -> p.type().name().equals(type))
                    .count();
            System.out.printf("  %-8s: %3d partículas (1 ParticleType compartilhado)%n", type, count);
        }

        System.out.println("\n--- Memória ---");
        long flyweightBytes = (long) factory.size() * 200;
        long contextBytes   = (long) PARTICLES * 40;
        long naiveBytes     = (long) PARTICLES * 240;
        System.out.printf("  Com Flyweight: %d bytes (%d tipos × 200B) + %d bytes (%d × 40B)%n",
                flyweightBytes + contextBytes, factory.size(), contextBytes, PARTICLES);
        System.out.printf("  Sem Flyweight: %d bytes (%d × 240B)%n", naiveBytes, PARTICLES);
        System.out.printf("  Economia:      %.0f%%%n",
                (1.0 - (double)(flyweightBytes + contextBytes) / naiveBytes) * 100);

        System.out.println();
        demonstrateJdkFlyweight();
    }
}
