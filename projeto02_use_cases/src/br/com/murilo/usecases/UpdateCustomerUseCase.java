package br.com.murilo.usecases;

import br.com.murilo.domain.Customer;
import br.com.murilo.exceptions.CustomerNotFoundException;
import br.com.murilo.repository.ICustomerRepository;

public class UpdateCustomerUseCase {

    private final ICustomerRepository customerRepository;

    public UpdateCustomerUseCase(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer handle(Customer updatedCustomer) throws CustomerNotFoundException {
        Customer existing = new FindCustomerUseCase(customerRepository)
                .handle(updatedCustomer.getCpf());
        customerRepository.update(existing, updatedCustomer);
        return existing;
    }
}
