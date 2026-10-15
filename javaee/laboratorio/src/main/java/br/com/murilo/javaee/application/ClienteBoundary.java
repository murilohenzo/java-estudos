package br.com.murilo.javaee.application;

public interface ClienteBoundary extends ClienteUseCase {
    void cadastrarComFalha(NovoCliente entrada);
}
