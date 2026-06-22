package generics_project.useCases;


import generics_project.domain.Client;
import generics_project.repository.client.IClientsRepository;

public class SearchClientUseCase {
    private final IClientsRepository clientsRepository;

    public SearchClientUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public Client execute(Long cpf){
        return this.clientsRepository.search(cpf);
    }
}
