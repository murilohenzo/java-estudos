/**
 * 
 */
package test.java.br.com.murilo;

import main.java.br.com.murilo.dao.IClienteDAO;
import main.java.br.com.murilo.dao.generic.IGenericDAO;
import main.java.br.com.murilo.domain.Cliente;
import main.java.br.com.murilo.exceptions.TipoChaveNaoEncontradaException;
import main.java.br.com.murilo.services.ClienteService;
import main.java.br.com.murilo.services.IClienteService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.java.br.com.murilo.dao.ClienteDaoMock;


public class ClienteServiceTest {
	
	private IClienteService clienteService;
	
	private Cliente cliente;
	
	public ClienteServiceTest() {
		IClienteDAO dao = new ClienteDaoMock();
		clienteService = new ClienteService((IGenericDAO<Cliente, Long>) dao);
	}
	
	@Before
	public void init() {
		cliente = new Cliente();
		cliente.setCpf(12312312312L);
		cliente.setNome("Rodrigo");
		cliente.setCidade("São Paulo");
		cliente.setEnd("End");
		cliente.setEstado("SP");
		cliente.setNumero(10);
		cliente.setTel(1199999999L);
		
	}
	
	@Test
	public void pesquisarCliente() {
		Cliente clienteConsultado = clienteService.buscarPorCPF(cliente.getCpf());
		Assert.assertNotNull(clienteConsultado);
	}
	
	@Test
	public void salvarCliente() throws TipoChaveNaoEncontradaException {
		Boolean retorno = clienteService.cadastrar(cliente);
		
		Assert.assertTrue(retorno);
	}
	
	@Test
	public void excluirCliente() {
		clienteService.excluir(cliente.getCpf());
	}
	
	@Test
	public void alterarCliente() throws TipoChaveNaoEncontradaException {
		cliente.setNome("Rodrigo Pires");
		clienteService.alterar(cliente);
		
		Assert.assertEquals("Rodrigo Pires", cliente.getNome());
	}
}
