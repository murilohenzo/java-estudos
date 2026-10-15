package br.com.murilo.javaee.architecture;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ArchitectureTest {
    @Test
    void dadoCodigoJava_quandoVerificarTamanho_entaoTemNoMaximo80Linhas() throws Exception {
        for (Path pasta : new Path[] {Path.of("src/main/java"), Path.of("src/test/java")}) {
            try (var arquivos = Files.walk(pasta)) {
                for (Path arquivo : arquivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                    int linhas = Files.readAllLines(arquivo).size();
                    assertTrue(linhas <= 80, arquivo + " contém " + linhas + " linhas");
                }
            }
        }
    }

    @Test
    void dadoService_quandoVerificarDependencias_entaoNaoImportaAdapters() throws Exception {
        Path pasta = Path.of("src/main/java/br/com/murilo/javaee/application/service");
        try (var arquivos = Files.walk(pasta)) {
            for (Path arquivo : arquivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                String codigo = Files.readString(arquivo);
                assertFalse(codigo.contains("import br.com.murilo.javaee.adapters."),
                        "Caso de uso acoplado a adaptador: " + arquivo);
                assertFalse(codigo.contains("import jakarta.persistence."),
                        "Caso de uso acoplado diretamente ao JPA: " + arquivo);
                assertFalse(codigo.contains("import jakarta.faces."),
                        "Caso de uso acoplado à View: " + arquivo);
            }
        }
    }
}
