package generics_project.useCases;

import generics_project.domain.Client;
import generics_project.repository.client.IClientsRepository;

public class UpdateClientUseCase {
    private final IClientsRepository clientsRepository;

    public UpdateClientUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public void execute(Long cpf, Client client){
        Client existsClient = this.clientsRepository.search(cpf);
        if (existsClient != null) {
            this.clientsRepository.update(client);
        }
    }
}
