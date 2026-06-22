package br.com.murilo.exceptions;

public class ClientService {
    private final IClientRepository iClientRepository;

    public ClientService(IClientRepository iClientRepository) {
        this.iClientRepository = iClientRepository;
    }

    public void findClientById(String id) throws ClientNotFoundException {
        try {
            boolean existsClient = this.iClientRepository.findById(Integer.parseInt(id));
            if (!existsClient) {
                throw new ClientNotFoundException("Client not found in database");
            }
        } catch (IndexOutOfBoundsException exception) {
            throw new ClientNotFoundException("Client not found in database");
        }
    }
}
