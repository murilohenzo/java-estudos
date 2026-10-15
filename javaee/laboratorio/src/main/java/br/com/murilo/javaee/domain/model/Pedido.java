package br.com.murilo.javaee.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 250)
    private String descricao;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    protected Pedido() {}

    public Pedido(Cliente cliente, String descricao) {
        this.cliente = cliente;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public String getDescricao() { return descricao; }
}
