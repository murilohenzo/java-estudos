package br.com.murilo.javaee.architecture;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ArchitectureTest {
    @Test
    void dadoProjeto_quandoVerificarTamanho_entaoJavaTemNoMaximo80Linhas() throws Exception {
        Path principal = Path.of("src/main/java");
        Path testes = Path.of("src/test/java");
        for (Path pasta : new Path[] {principal, testes}) {
            try (var arquivos = Files.walk(pasta)) {
                for (Path arquivo : arquivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                    long linhas = Files.readAllLines(arquivo).size();
                    assertTrue(linhas <= 80, arquivo + " possui " + linhas + " linhas");
                }
            }
        }
    }
}
