package br.com.murilo.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ClientRepository implements IClientRepository{
    private final List<Client> clients = new ArrayList<>();

    @Override
    public void create(Client client) {
        clients.add(client);
    }

    @Override
    public boolean findById(int id) {
        return this.clients.get(id) != null;
    }
}
