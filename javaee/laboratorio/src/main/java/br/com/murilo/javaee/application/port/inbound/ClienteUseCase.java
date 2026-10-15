package br.com.murilo.javaee.application.port.inbound;
import br.com.murilo.javaee.application.dto.NovoCliente;
import br.com.murilo.javaee.application.dto.ClienteResumo;
import br.com.murilo.javaee.application.dto.PedidoResumo;

import java.util.List;

public interface ClienteUseCase {
    Long cadastrar(NovoCliente entrada);
    List<ClienteResumo> listar();
    Long criarPedido(Long clienteId, String descricao);
    List<PedidoResumo> listarPedidos(Long clienteId);
}
