package repository;

import domain.Client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClientMapRepositoryInMemory implements IClientsRepository{
    private final Map<Long, Client> clientMap;

    public ClientMapRepositoryInMemory() {
        this.clientMap = new HashMap<>();
    }

    @Override
    public void create(Client client) {
        this.clientMap.put(client.getCpf(), client);
    }

    @Override
    public void delete(Long cpf) {
        this.clientMap.remove(cpf);
    }

    @Override
    public void update(Client existsClient , Client client) {
            if (client.getName() != null) {
                existsClient.setName(client.getName());
            } else if (client.getCpf() != null) {
                existsClient.setCpf(client.getCpf());
            } else if (client.getPhoneNumber() != null) {
                existsClient.setPhoneNumber(client.getPhoneNumber());
            } else if (client.getAddress() != null) {
                existsClient.setAddress(client.getAddress());
            } else if (client.getNumber() != null) {
                existsClient.setNumber(client.getNumber());
            } else if (client.getCity() != null) {
                existsClient.setCity(client.getCity());
            } else if (client.getState() != null) {
                existsClient.setState(client.getState());
            }
    }

    @Override
    public Client search(Long cpf) {
        return this.clientMap.get(cpf);
    }

    @Override
    public Collection<Client> findAll() {
        return this.clientMap.values();
    }
}
