package br.com.murilo.repository;

import br.com.murilo.domain.Customer;

import java.util.*;

public class CustomerRepository implements ICustomerRepository {

    private final Map<String, Customer> customers = new HashMap<>();

    @Override
    public Collection<Customer> findAll() {
        return Collections.unmodifiableCollection(this.customers.values());
    }

    @Override
    public Customer save(Customer customer) {
        this.customers.put(customer.getCpf(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> find(String cpf) {
        return Optional.ofNullable(this.customers.get(cpf));
    }

    @Override
    public void delete(String cpf) {
        this.customers.remove(cpf);
    }

    @Override
    public void update(Customer existingCustomer, Customer updatedCustomer) {
        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setCpf(updatedCustomer.getCpf());
        existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
        existingCustomer.setAddress(updatedCustomer.getAddress());
        existingCustomer.setNumber(updatedCustomer.getNumber());
        existingCustomer.setCity(updatedCustomer.getCity());
        existingCustomer.setState(updatedCustomer.getState());
    }
}
