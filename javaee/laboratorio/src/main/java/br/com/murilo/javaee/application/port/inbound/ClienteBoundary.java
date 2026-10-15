package br.com.murilo.javaee.application.port.inbound;
import br.com.murilo.javaee.application.dto.NovoCliente;

public interface ClienteBoundary extends ClienteUseCase {
    void cadastrarComFalha(NovoCliente entrada);
}
