package br.com.murilo.repository;

import br.com.murilo.domain.Customer;

import java.util.*;

public class CustomerRepository implements ICustomerRepository{
    private final Map<Long, Customer> customers = new HashMap<>();

    @Override
    public Collection<Customer> findAll() {
        return this.customers.values();
    }

    @Override
    public Customer save(Customer customer) {
        this.customers.put(customer.getCpf(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> find(long cpf) {
        return this.customers.values()
                .stream().filter(customer -> Objects.equals(cpf, customer.getCpf()))
                .findAny();
    }

    @Override
    public void delete(long cpf) {
        this.customers.remove(cpf);
    }

    @Override
    public void update(Customer existsCustomer, Customer customer) {
            existsCustomer.setName(customer.getName());
            existsCustomer.setCpf(customer.getCpf());
            existsCustomer.setPhoneNumber(customer.getPhoneNumber());
            existsCustomer.setAddress(customer.getAddress());
            existsCustomer.setNumber(customer.getNumber());
            existsCustomer.setCity(customer.getCity());
            existsCustomer.setState(customer.getState());
        }
}
