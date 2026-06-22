package useCases;

import domain.Client;
import repository.IClientsRepository;

public class SearchClientUseCase {
    private final IClientsRepository clientsRepository;

    public SearchClientUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public Client execute(Long cpf){
        return this.clientsRepository.search(cpf);
    }
}
