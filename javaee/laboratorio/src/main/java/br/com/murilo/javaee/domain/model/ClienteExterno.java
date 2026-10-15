package br.com.murilo.javaee.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes_externos")
@Access(AccessType.PROPERTY)
public class ClienteExterno {
    private String codigo;
    private String nome;

    protected ClienteExterno() {}

    public ClienteExterno(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    @Id
    @Column(name = "codigo_externo", length = 50)
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    @Column(name = "nome_externo", length = 120)
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
