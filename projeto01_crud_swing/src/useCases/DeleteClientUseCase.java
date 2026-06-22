package useCases;

import domain.Client;
import repository.IClientsRepository;

public class DeleteClientUseCase {
    private final IClientsRepository clientsRepository;

    public DeleteClientUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public void execute(Long cpf){
        Client existsClient = this.clientsRepository.search(cpf);
        if (existsClient != null) {
            this.clientsRepository.delete(existsClient.getCpf());
        }
    }
}
