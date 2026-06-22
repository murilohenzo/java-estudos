package repository;

import domain.Client;

import java.util.*;
import java.util.stream.Collectors;

public class ClientSetRepositoryInMemory implements IClientsRepository{
    private final HashSet<Client> clientSet;

    public ClientSetRepositoryInMemory() {
        this.clientSet = new HashSet<>();
    }

    @Override
    public void create(Client client) {
        this.clientSet.add(client);
    }

    @Override
    public void delete(Long cpf) {
        Client existClient = this.clientSet.stream().filter(c -> Objects.equals(c.getCpf(), cpf)).findAny().orElse(null);
        this.clientSet.remove(existClient);
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
        return this.clientSet.stream().filter(c -> Objects.equals(c.getCpf(), cpf)).findAny().orElse(null);
    }

    @Override
    public Collection<Client> findAll() {
        return this.clientSet;
    }
}
