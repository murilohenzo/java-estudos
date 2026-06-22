package br.com.murilo.repository;

import br.com.murilo.domain.Customer;

import java.util.Collection;
import java.util.Optional;

public interface ICustomerRepository {
    Collection<Customer> findAll();
    Customer save(Customer customer);
    Optional<Customer> find(String cpf);
    void delete(String cpf);
    void update(Customer existingCustomer, Customer updatedCustomer);
}
