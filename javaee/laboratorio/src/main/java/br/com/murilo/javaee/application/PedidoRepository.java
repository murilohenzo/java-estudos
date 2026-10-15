package br.com.murilo.javaee.application;

import br.com.murilo.javaee.domain.Pedido;
import java.util.List;

public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
    List<PedidoResumo> listarPorCliente(Long clienteId);
}
