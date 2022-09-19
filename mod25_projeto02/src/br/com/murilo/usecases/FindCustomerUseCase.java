package br.com.murilo.usecases;

import br.com.murilo.domain.Customer;
import br.com.murilo.exceptions.CustomerNotFoundException;
import br.com.murilo.repository.ICustomerRepository;

public class FindCustomerUseCase {
    private final ICustomerRepository iCustomerRepository;

    public FindCustomerUseCase(ICustomerRepository iCustomerRepository) {
        this.iCustomerRepository = iCustomerRepository;
    }

    public Customer handle(long cpf) throws CustomerNotFoundException {
            return this.iCustomerRepository.find(cpf).orElseThrow(() -> new CustomerNotFoundException("Customer not found in database with cpf: " + cpf));
    }
}
