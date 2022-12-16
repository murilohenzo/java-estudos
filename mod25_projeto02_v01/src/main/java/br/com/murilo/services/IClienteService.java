/**
 * 
 */
package main.java.br.com.murilo.services;

import main.java.br.com.murilo.domain.Cliente;
import main.java.br.com.murilo.exceptions.TipoChaveNaoEncontradaException;

public interface IClienteService {

	public Boolean cadastrar(Cliente cliente) throws TipoChaveNaoEncontradaException;

	public Cliente buscarPorCPF(Long cpf);

	public void excluir(Long cpf);

	public void alterar(Cliente cliente) throws TipoChaveNaoEncontradaException;

}
