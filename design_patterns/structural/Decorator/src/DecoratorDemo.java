import java.util.function.Function;

/**
 * <h1>Decorator — Padrão Estrutural</h1>
 *
 * <h2>Intenção</h2>
 * <p>Anexar responsabilidades adicionais a um objeto dinamicamente.
 * Decoradores fornecem uma alternativa flexível à criação de subclasses para extensão de funcionalidade.
 *
 * <h2>Problema que resolve</h2>
 * <p>Herança para extensão tem dois problemas:
 * <ol>
 *   <li>É estática — não pode ser trocada em runtime</li>
 *   <li>Cria explosão combinatória: LoggingCache + AuthCache + CompressionCache = 8 subclasses para 3 features</li>
 * </ol>
 *
 * <h2>Componentes</h2>
 * <ul>
 *   <li><b>Component</b> — interface base</li>
 *   <li><b>ConcreteComponent</b> — implementação real do trabalho</li>
 *   <li><b>BaseDecorator</b> — wrapping + delegação; mantém referência ao Component</li>
 *   <li><b>ConcreteDecorator</b> — adiciona comportamento antes/depois da delegação</li>
 * </ul>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Cross-cutting concerns: logging, caching, auth, compressão, métricas</li>
 *   <li>Quando não quer modificar a classe existente (Open/Closed Principle)</li>
 *   <li>Java I/O: {@code new BufferedReader(new InputStreamReader(socket.getInputStream()))}</li>
 * </ul>
 *
 * <h2>Java IO como exemplo canônico</h2>
 * <pre>
 *   InputStream base   = new FileInputStream("data.bin");
 *   InputStream buffered = new BufferedInputStream(base);        // Decorator
 *   InputStream gzipped  = new GZIPInputStream(buffered);       // Decorator
 *   // cada camada adiciona comportamento ao anterior
 * </pre>
 */
public class DecoratorDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // COMPONENT — interface de DataSource
    // ─────────────────────────────────────────────────────────────────────────

    interface DataSource {
        void write(String data);
        String read();
        String name();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCRETE COMPONENT — armazenamento real
    // ─────────────────────────────────────────────────────────────────────────

    static class InMemoryDataSource implements DataSource {
        private String data = "";

        @Override public void write(String d) { this.data = d; }
        @Override public String read()        { return data; }
        @Override public String name()        { return "InMemory"; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BASE DECORATOR — delega tudo para o wrappee
    // ─────────────────────────────────────────────────────────────────────────

    abstract static class DataSourceDecorator implements DataSource {
        private final DataSource wrappee;

        DataSourceDecorator(DataSource wrappee) { this.wrappee = wrappee; }

        @Override public void write(String data)  { wrappee.write(data); }
        @Override public String read()            { return wrappee.read(); }
        @Override public String name()            { return wrappee.name(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCRETE DECORATORS
    // ─────────────────────────────────────────────────────────────────────────

    /** Decorator 1 — transforma dados (simula Base64) */
    static class EncodingDecorator extends DataSourceDecorator {
        EncodingDecorator(DataSource wrappee) { super(wrappee); }

        @Override
        public void write(String data) {
            super.write(encode(data));
        }

        @Override
        public String read() {
            return decode(super.read());
        }

        @Override
        public String name() { return "Encoding(" + super.name() + ")"; }

        private String encode(String s) { return "[ENC:" + s + "]"; }
        private String decode(String s) {
            return s.startsWith("[ENC:") && s.endsWith("]")
                    ? s.substring(5, s.length() - 1)
                    : s;
        }
    }

    /** Decorator 2 — comprime dados (simula compressão) */
    static class CompressionDecorator extends DataSourceDecorator {
        CompressionDecorator(DataSource wrappee) { super(wrappee); }

        @Override
        public void write(String data) {
            super.write("COMPRESSED{" + data.length() + ":" + data + "}");
        }

        @Override
        public String read() {
            var raw = super.read();
            if (raw.startsWith("COMPRESSED{")) {
                int colon = raw.indexOf(':');
                return raw.substring(colon + 1, raw.length() - 1);
            }
            return raw;
        }

        @Override
        public String name() { return "Compression(" + super.name() + ")"; }
    }

    /** Decorator 3 — logging de operações */
    static class LoggingDecorator extends DataSourceDecorator {
        LoggingDecorator(DataSource wrappee) { super(wrappee); }

        @Override
        public void write(String data) {
            System.out.println("  [LOG] write → " + wrappee().name());
            super.write(data);
        }

        @Override
        public String read() {
            System.out.println("  [LOG] read ← " + wrappee().name());
            return super.read();
        }

        @Override
        public String name() { return "Logging(" + super.name() + ")"; }

        private DataSource wrappee() {
            // acessa name() do próximo nível
            return this;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Decorator funcional — alternativa com Function<T,T> em Java 8+
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Decorator funcional: composição de transformações com {@code Function::andThen}.
     * Menos boilerplate para casos simples onde não precisa de wrapping stateful.
     */
    static Function<String, String> buildPipeline() {
        Function<String, String> trim       = String::trim;
        Function<String, String> uppercase  = String::toUpperCase;
        Function<String, String> addBracket = s -> "[" + s + "]";

        return trim.andThen(uppercase).andThen(addBracket);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Decorator Pattern ===\n");

        var payload = "dados sensíveis do usuário";

        // Empilhamento de decorators — cada camada adiciona comportamento
        DataSource source = new InMemoryDataSource();
        DataSource encoded = new EncodingDecorator(source);
        DataSource compressed = new CompressionDecorator(encoded);
        DataSource logged = new LoggingDecorator(compressed);

        System.out.println("Stack: " + logged.name());
        System.out.println("\n--- Write ---");
        logged.write(payload);

        System.out.println("\n--- Read ---");
        var result = logged.read();
        System.out.println("  Resultado: '" + result + "'");
        System.out.println("  Igual ao original: " + result.equals(payload));

        // Comparar: sem decorators
        System.out.println("\n--- Sem decorators (InMemory direto) ---");
        var plain = new InMemoryDataSource();
        plain.write(payload);
        System.out.println("  Raw armazenado: '" + plain.read() + "'");

        // Decorator funcional
        System.out.println("\n--- Decorator funcional (Function::andThen) ---");
        var pipeline = buildPipeline();
        System.out.println("  Input:  '  hello world  '");
        System.out.println("  Output: '" + pipeline.apply("  hello world  ") + "'");
    }
}
