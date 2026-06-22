package generics_project.useCases;


import generics_project.domain.Client;
import generics_project.repository.client.IClientsRepository;

import java.util.Objects;

public class DeleteClientUseCase {
    private final IClientsRepository clientsRepository;

    public DeleteClientUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public void execute(Long cpf){
        Client existsClient = this.clientsRepository.search(cpf);
        if (Objects.nonNull(existsClient)) {
            this.clientsRepository.delete(existsClient.getCpf());
        }
    }
}
