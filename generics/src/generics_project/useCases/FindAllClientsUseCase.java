package generics_project.useCases;


import generics_project.domain.Client;
import generics_project.repository.client.IClientsRepository;

import java.util.Collection;

public class FindAllClientsUseCase {
    private final IClientsRepository clientsRepository;

    public FindAllClientsUseCase(IClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
    }

    public Collection<Client> execute() {
        return this.clientsRepository.findAll();
    }
}
