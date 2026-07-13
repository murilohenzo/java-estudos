/**
 * Demonstração de <b>Compact Source Files & Instance Main</b> — JEP 512 (GA no Java 25).
 *
 * <p>Arquivo compacto: classe implícita, {@code main} de instância e acesso
 * automático aos tipos de {@code java.base} (como se houvesse
 * {@code import module java.base;}).
 *
 * <p>Execute com: {@code java CompactSourceDemo.java}
 */
void main(String[] args) {
    IO.println("=== Compact Source Files — Java 25 ===");
    IO.println("Hello from instance main(String[] args)!");
    IO.println("args length: " + args.length);

    var numbers = List.of(1, 2, 3, 4, 5);
    var sum = numbers.stream().mapToInt(Integer::intValue).sum();
    IO.println("sum of " + numbers + " = " + sum);
}
