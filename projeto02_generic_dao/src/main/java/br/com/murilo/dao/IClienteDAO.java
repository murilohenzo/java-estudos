/**
 * 
 */
package br.com.murilo.dao;

import br.com.murilo.dao.generic.IGenericDAO;
import br.com.murilo.domain.Cliente;
import br.com.murilo.exceptions.TipoChaveNaoEncontradaException;

import java.util.Collection;

public interface IClienteDAO extends IGenericDAO<Cliente, Long> {


    Boolean cadastrar(Cliente entity) throws TipoChaveNaoEncontradaException;

    void excluir(Long valor);

    void alterar(Cliente entity) throws TipoChaveNaoEncontradaException;

    Cliente consultar(Long valor);

    Collection<Cliente> buscarTodos();
}
