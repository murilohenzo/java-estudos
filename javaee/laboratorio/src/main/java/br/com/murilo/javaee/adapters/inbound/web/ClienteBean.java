package br.com.murilo.javaee.adapters.inbound.web;

import br.com.murilo.javaee.application.dto.*;
import br.com.murilo.javaee.application.port.inbound.ClienteBoundary;
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

    protected ClienteBean() {}

    @Inject
    public ClienteBean(ClienteBoundary facade) { this.facade = facade; }

    public void antesDeSalvar(ActionEvent event) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("ActionListener: clique recebido"));
    }

    public String salvar() {
        try {
            facade.cadastrar(new NovoCliente(nome, email));
            nome = null;
            email = null;
            aviso(FacesMessage.SEVERITY_INFO, "Cliente cadastrado");
        } catch (IllegalArgumentException e) {
            aviso(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
        return null;
    }

    public void simularFalha() {
        try {
            facade.cadastrarComFalha(new NovoCliente(nome, email));
        } catch (RuntimeException e) {
            aviso(FacesMessage.SEVERITY_WARN, "Rollback simulado: confira a auditoria");
        }
    }

    private void aviso(FacesMessage.Severity nivel, String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(nivel, mensagem, null));
    }

    public List<ClienteResumo> getClientes() { return facade.listar(); }
    public String getNome() { return nome; }
    public void setNome(String valor) { nome = valor; }
    public String getEmail() { return email; }
    public void setEmail(String valor) { email = valor; }
}
