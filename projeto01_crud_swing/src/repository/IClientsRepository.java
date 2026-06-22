package repository;

import domain.Client;

import java.util.Collection;

public interface IClientsRepository {
    void create(Client client);
    void delete (Long cpf);
    void update(Client existsClient , Client client);
    Client search(Long cpf);
    Collection<Client> findAll();
}
