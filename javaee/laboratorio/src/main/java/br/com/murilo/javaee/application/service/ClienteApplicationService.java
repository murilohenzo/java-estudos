package br.com.murilo.javaee.application.service;
import br.com.murilo.javaee.application.dto.NovoCliente;
import br.com.murilo.javaee.application.dto.ClienteResumo;
import br.com.murilo.javaee.application.dto.PedidoResumo;
import br.com.murilo.javaee.application.port.outbound.ClienteRepository;
import br.com.murilo.javaee.application.port.outbound.PedidoRepository;
import br.com.murilo.javaee.application.port.outbound.Notificador;
import br.com.murilo.javaee.application.port.inbound.ClienteUseCase;

import br.com.murilo.javaee.domain.model.Cliente;
import br.com.murilo.javaee.domain.model.Pedido;
import br.com.murilo.javaee.application.port.outbound.Operacional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ClienteApplicationService implements ClienteUseCase {
    private final ClienteRepository clientes;
    private final PedidoRepository pedidos;
    private final Notificador notificador;

    protected ClienteApplicationService() {
        this.clientes = null;
        this.pedidos = null;
        this.notificador = null;
    }

    @Inject
    public ClienteApplicationService(ClienteRepository clientes, PedidoRepository pedidos,
                                     @Operacional Notificador notificador) {
        this.clientes = clientes;
        this.pedidos = pedidos;
        this.notificador = notificador;
    }

    @Override
    public Long cadastrar(NovoCliente entrada) {
        if (entrada == null || entrada.nome() == null || entrada.nome().isBlank()
                || entrada.email() == null || !entrada.email().contains("@")) {
            throw new IllegalArgumentException("Nome e e-mail válido são obrigatórios");
        }
        Cliente salvo = clientes.salvar(new Cliente(entrada.nome().trim(), entrada.email().trim()));
        notificador.informar("Cliente cadastrado: " + salvo.getNome());
        return salvo.getId();
    }

    @Override
    public List<ClienteResumo> listar() { return clientes.listar(); }

    @Override
    public Long criarPedido(Long clienteId, String descricao) {
        if (clienteId == null || descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Cliente e descrição são obrigatórios");
        }
        Cliente cliente = clientes.buscar(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        return pedidos.salvar(new Pedido(cliente, descricao.trim())).getId();
    }

    @Override
    public List<PedidoResumo> listarPedidos(Long clienteId) {
        return clienteId == null ? List.of() : pedidos.listarPorCliente(clienteId);
    }
}
