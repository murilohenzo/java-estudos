package useCases;

import domain.Client;
import repository.IClientsRepository;

public class CreateClientUseCase {
    private final IClientsRepository clientsRepository;

    public CreateClientUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public Boolean execute(Client client){
        Client existClient = this.clientsRepository.search(client.getCpf());
        if (existClient != null || client.getCpf() == null) {
            return false;
        }
        this.clientsRepository.create(client);
        return true;
    }
}
