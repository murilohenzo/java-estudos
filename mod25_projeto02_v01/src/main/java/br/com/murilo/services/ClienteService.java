/**
 * 
 */
package main.java.br.com.murilo.services;
import main.java.br.com.murilo.dao.IClienteDAO;
import main.java.br.com.murilo.dao.generic.IGenericDAO;
import main.java.br.com.murilo.domain.Cliente;
import main.java.br.com.murilo.exceptions.TipoChaveNaoEncontradaException;
import main.java.br.com.murilo.services.generic.GenericService;

import java.util.Collection;

public class ClienteService extends GenericService<Cliente, Long> implements IClienteService {

	public ClienteService(IGenericDAO<Cliente, Long> dao) {
		super(dao);
	}

	@Override
	public Boolean cadastrar(Cliente entity) throws TipoChaveNaoEncontradaException {
		return super.cadastrar(entity);
	}

	@Override
	public void excluir(Long valor) {
		super.excluir(valor);
	}

	@Override
	public void alterar(Cliente entity) throws TipoChaveNaoEncontradaException {
		super.alterar(entity);
	}

	@Override
	public Collection<Cliente> buscarTodos() {
		return super.buscarTodos();
	}

	@Override
	public Cliente buscarPorCPF(Long cpf) {
		return this.dao.consultar(cpf);
	}
}
