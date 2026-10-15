package br.com.murilo.javaee.ejb;

import br.com.murilo.javaee.application.*;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
@Local(ClienteBoundary.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ClienteFacadeEJB implements ClienteBoundary {
    // O EJB exige construtor público sem argumentos: injeção do contêiner na borda.
    @Inject
    private ClienteUseCase service;
    @EJB
    private AuditoriaEJB auditoria;

    public ClienteFacadeEJB() {}

    @Override
    public Long cadastrar(NovoCliente entrada) { return service.cadastrar(entrada); }

    @Override
    public List<ClienteResumo> listar() { return service.listar(); }

    @Override
    public Long criarPedido(Long id, String descricao) {
        return service.criarPedido(id, descricao);
    }

    @Override
    public List<PedidoResumo> listarPedidos(Long id) {
        return service.listarPedidos(id);
    }

    @Override
    public void cadastrarComFalha(NovoCliente entrada) {
        Long id = service.cadastrar(entrada);
        auditoria.registrar("Tentativa simulada de cadastro: cliente " + id);
        throw new IllegalStateException("Falha simulada; cadastro deve sofrer rollback");
    }
}
