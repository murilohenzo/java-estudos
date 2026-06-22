package br.com.murilo.usecases;

import br.com.murilo.domain.Customer;
import br.com.murilo.repository.ICustomerRepository;

import java.util.Collection;

public class FindAllCustomersUseCase {

    private final ICustomerRepository customerRepository;

    public FindAllCustomersUseCase(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Collection<Customer> handle() {
        return this.customerRepository.findAll();
    }
}
