package generics_project.repository.client.implementation;


import generics_project.domain.Client;
import generics_project.repository.client.IClientsRepository;
import generics_project.repository.generic.implementation.GenericRepository;
import generics_project.singleton.SingletonMap;

import java.util.HashMap;
import java.util.Map;

public class ClientMapRepositoryInMemory extends GenericRepository<Client> implements IClientsRepository {

    public ClientMapRepositoryInMemory() {
        super();
        Map<Long, Client> tMap = (Map<Long, Client>) SingletonMap.getInstance().getMap().get(getClassType());
        if (tMap == null) {
            tMap = new HashMap<>();
            SingletonMap.getInstance().getMap().put(getClassType(), tMap);
        }
    }

    @Override
    public Class<Client> getClassType() {
        return Client.class;
    }

    @Override
    public void updateEntity(Client existsClient, Client client) {
            existsClient.setName(client.getName());
            existsClient.setCpf(client.getCpf());
            existsClient.setPhoneNumber(client.getPhoneNumber());
            existsClient.setAddress(client.getAddress());
            existsClient.setNumber(client.getNumber());
            existsClient.setCity(client.getCity());
            existsClient.setState(client.getState());
    }
}
