package br.com.murilo.javaee.application.port.outbound;
import br.com.murilo.javaee.application.dto.PedidoResumo;

import br.com.murilo.javaee.domain.model.Pedido;
import java.util.List;

public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
    List<PedidoResumo> listarPorCliente(Long clienteId);
}
