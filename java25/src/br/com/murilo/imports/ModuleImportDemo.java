package br.com.murilo.imports;

import module java.base;

/**
 * Demonstração de <b>Module Import Declarations</b> — JEP 511 (GA no Java 25).
 *
 * <p>{@code import module java.base;} traz para o simple-name todos os tipos
 * públicos dos packages exportados por {@code java.base} (List, Map, Stream,
 * Path, Files, Collectors, Function, …) sem dezenas de imports manuais.
 */
public class ModuleImportDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Module Import Declarations — Java 25 ===\n");

        String[] fruits = {"apple", "berry", "citrus", "avocado"};

        Map<String, String> byInitial = Stream.of(fruits)
                .collect(Collectors.toMap(
                        s -> s.substring(0, 1).toUpperCase(),
                        Function.identity(),
                        (a, b) -> a + "|" + b,
                        LinkedHashMap::new));

        System.out.println("byInitial = " + byInitial);

        Path tmp = Files.createTempFile("java25-", ".txt");
        Files.writeString(tmp, String.join("\n", fruits));
        System.out.println("wrote " + Files.size(tmp) + " bytes to " + tmp.getFileName());
        Files.deleteIfExists(tmp);

        List<String> upper = Stream.of(fruits)
                .map(String::toUpperCase)
                .sorted()
                .toList();
        System.out.println("upper sorted = " + upper);
    }
}
