import domain.Client;
import lib.LoggerCtx;
import repository.ClientMapRepositoryInMemory;
import repository.ClientSetRepositoryInMemory;
import repository.IClientsRepository;
import useCases.*;

import java.util.logging.Level;

public class Main {

    private static IClientsRepository iClientsRepository;
    public static void main(String[] args) {
        Client client = new Client("Murilo", "123456",
                "5510312312312", "Sao Paulo",
                "1333", "Sao Paulo", "Sao Paulo");

//        iClientsRepository = new ClientMapRepositoryInMemory();
        iClientsRepository = new ClientSetRepositoryInMemory();
        CreateClientUseCase createClientUseCase = new CreateClientUseCase(iClientsRepository);
        SearchClientUseCase searchClientUseCase = new SearchClientUseCase(iClientsRepository);
        UpdateClientUseCase updateClientUseCase = new UpdateClientUseCase(iClientsRepository);


        createClientUseCase.execute(client);
        System.out.println(searchClientUseCase.execute(Long.parseLong("123456")));
        Client client1 = new Client("Henzo");
        updateClientUseCase.execute(Long.parseLong("123456"), client1);

        System.out.println(searchClientUseCase.execute(Long.parseLong("123456")));
    }
}