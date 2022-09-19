package br.com.murilo.usecases;
import br.com.murilo.exceptions.CustomerNotFoundException;
import br.com.murilo.repository.ICustomerRepository;

public class DeleteCustomerUseCase {
    private final ICustomerRepository customerRepository;

    public DeleteCustomerUseCase(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void handle(Long cpf) throws CustomerNotFoundException {
        FindCustomerUseCase findCustomerUseCase = new FindCustomerUseCase(customerRepository);
        customerRepository.delete(findCustomerUseCase.handle(cpf).getCpf());
    }
}
