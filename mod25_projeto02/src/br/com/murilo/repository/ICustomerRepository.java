package br.com.murilo.repository;

import br.com.murilo.domain.Customer;

import java.util.Optional;

public interface ICustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> find(long cpf);
    Customer delete(long cpf);
}
