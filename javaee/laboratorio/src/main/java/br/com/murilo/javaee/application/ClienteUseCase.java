package br.com.murilo.javaee.application;

import java.util.List;

public interface ClienteUseCase {
    Long cadastrar(NovoCliente entrada);
    List<ClienteResumo> listar();
    Long criarPedido(Long clienteId, String descricao);
    List<PedidoResumo> listarPedidos(Long clienteId);
}
