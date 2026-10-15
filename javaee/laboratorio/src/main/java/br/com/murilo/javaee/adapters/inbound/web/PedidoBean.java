package br.com.murilo.javaee.adapters.inbound.web;

import br.com.murilo.javaee.application.dto.PedidoResumo;
import br.com.murilo.javaee.application.port.inbound.ClienteBoundary;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("pedidoBean")
@ViewScoped
public class PedidoBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private ClienteBoundary facade;
    private Long clienteId;
    private String descricao;

    protected PedidoBean() {}

    @Inject
    public PedidoBean(ClienteBoundary facade) { this.facade = facade; }

    public void criarPedido() {
        try {
            facade.criarPedido(clienteId, descricao);
            descricao = null;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Pedido cadastrado"));
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public List<PedidoResumo> getPedidos() { return facade.listarPedidos(clienteId); }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long valor) { clienteId = valor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String valor) { descricao = valor; }
}
