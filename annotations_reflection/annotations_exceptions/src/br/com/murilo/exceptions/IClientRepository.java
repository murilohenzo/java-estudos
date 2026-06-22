package br.com.murilo.exceptions;

public interface IClientRepository {
    void create(Client client);

    boolean findById(int id);
}
