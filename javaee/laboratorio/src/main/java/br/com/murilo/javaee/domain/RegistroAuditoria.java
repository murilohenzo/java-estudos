package br.com.murilo.javaee.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 250)
    private String descricao;
    @Column(nullable = false)
    private LocalDateTime criadoEm;

    protected RegistroAuditoria() {}

    public RegistroAuditoria(String descricao) {
        this.descricao = descricao;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
