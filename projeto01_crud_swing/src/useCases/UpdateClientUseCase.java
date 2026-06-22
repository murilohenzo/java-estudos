package useCases;

import domain.Client;
import repository.IClientsRepository;

public class UpdateClientUseCase {
    private final IClientsRepository clientsRepository;

    public UpdateClientUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public void execute(Long cpf, Client client){
        Client existsClient = this.clientsRepository.search(cpf);
        if (existsClient != null) {
            this.clientsRepository.update(existsClient, client);
        }
    }
}
