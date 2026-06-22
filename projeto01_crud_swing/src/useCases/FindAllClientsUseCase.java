package useCases;

import domain.Client;
import repository.IClientsRepository;

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
