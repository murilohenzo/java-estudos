package br.com.murilo.javaee.web;

import br.com.murilo.javaee.application.*;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("clienteBean")
@ViewScoped
public class ClienteBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private ClienteBoundary facade;
    private String nome;
    private String email;
    private String descricao;
    private Long clienteId;

    protected ClienteBean() {} // Necessário para o proxy do escopo CDI.

    @Inject
    public ClienteBean(ClienteBoundary facade) { this.facade = facade; }

    public void antesDeSalvar(ActionEvent evento) {
        mensagem(FacesMessage.SEVERITY_INFO, "ActionListener recebido");
    }

    public String salvar() {
        try {
            facade.cadastrar(new NovoCliente(nome, email));
            nome = null;
            email = null;
            mensagem(FacesMessage.SEVERITY_INFO, "Cliente cadastrado");
        } catch (IllegalArgumentException e) {
            mensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
        return null;
    }

    public void criarPedido() {
        try {
            facade.criarPedido(clienteId, descricao);
            descricao = null;
            mensagem(FacesMessage.SEVERITY_INFO, "Pedido cadastrado");
        } catch (IllegalArgumentException e) {
            mensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public void simularFalha() {
        try {
            facade.cadastrarComFalha(new NovoCliente(nome, email));
        } catch (IllegalStateException e) {
            mensagem(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    private void mensagem(FacesMessage.Severity nivel, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(nivel, texto, null));
    }

    public List<ClienteResumo> getClientes() { return facade.listar(); }
    public List<PedidoResumo> getPedidos() { return facade.listarPedidos(clienteId); }
    public String getNome() { return nome; }
    public void setNome(String valor) { nome = valor; }
    public String getEmail() { return email; }
    public void setEmail(String valor) { email = valor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String valor) { descricao = valor; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long valor) { clienteId = valor; }
}
