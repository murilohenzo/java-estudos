package br.com.murilo.javaee.application.service;

import br.com.murilo.javaee.domain.model.Cliente;
import br.com.murilo.javaee.application.dto.NovoCliente;
import br.com.murilo.javaee.application.port.outbound.ClienteRepository;
import br.com.murilo.javaee.application.port.outbound.PedidoRepository;
import br.com.murilo.javaee.adapters.outbound.notification.NotificadorSilencioso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClienteApplicationServiceTest {
    private ClienteRepository clientes;
    private PedidoRepository pedidos;
    private ClienteApplicationService service;

    @BeforeEach
    void preparar() {
        clientes = mock(ClienteRepository.class);
        pedidos = mock(PedidoRepository.class);
        service = new ClienteApplicationService(clientes, pedidos, new NotificadorSilencioso());
    }

    @Test
    void dadoClienteValido_quandoCadastrar_entaoPersiste() {
        when(clientes.salvar(any(Cliente.class))).thenAnswer(c -> c.getArgument(0));
        assertNull(service.cadastrar(new NovoCliente("Ana", "ana@teste.com")));
        verify(clientes).salvar(any(Cliente.class));
    }

    @Test
    void dadoEmailInvalido_quandoCadastrar_entaoRejeita() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(new NovoCliente("Ana", "invalido")));
        verifyNoInteractions(clientes);
    }

    @Test
    void dadoClienteInexistente_quandoCriarPedido_entaoRejeita() {
        when(clientes.buscar(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(99L, "Produto"));
        verifyNoInteractions(pedidos);
    }
}
