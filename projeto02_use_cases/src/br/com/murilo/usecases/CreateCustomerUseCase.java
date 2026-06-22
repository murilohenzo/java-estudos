package br.com.murilo.usecases;

import br.com.murilo.domain.Customer;
import br.com.murilo.exceptions.CustomerAlreadyExistsException;
import br.com.murilo.repository.ICustomerRepository;

public class CreateCustomerUseCase {
    private final ICustomerRepository iCustomerRepository;

    public CreateCustomerUseCase(ICustomerRepository iCustomerRepository) {
        this.iCustomerRepository = iCustomerRepository;
    }

    public Customer handle(Customer customer) throws CustomerAlreadyExistsException {
        if (this.iCustomerRepository.find(customer.getCpf()).isEmpty()) {
            this.iCustomerRepository.save(customer);
            return customer;
        } else {
            throw new CustomerAlreadyExistsException("Customer already exists with cpf " + customer.getCpf());
        }
    }
}
