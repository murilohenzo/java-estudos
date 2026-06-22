package br.com.murilo.usecases;
import br.com.murilo.exceptions.CustomerNotFoundException;
import br.com.murilo.repository.ICustomerRepository;

public class DeleteCustomerUseCase {
    private final ICustomerRepository customerRepository;

    public DeleteCustomerUseCase(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void handle(String cpf) throws CustomerNotFoundException {
        new FindCustomerUseCase(customerRepository).handle(cpf);
        customerRepository.delete(cpf);
    }
}
