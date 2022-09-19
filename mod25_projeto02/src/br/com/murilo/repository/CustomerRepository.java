package br.com.murilo.repository;

import br.com.murilo.domain.Customer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CustomerRepository implements ICustomerRepository{
    private final Map<Integer, Customer> customers = new HashMap<>();

    @Override
    public Customer save(Customer customer) {
        int id = this.customers.size() + 1;

        this.customers.put(id, customer);
        return customer;
    }

    @Override
    public Optional<Customer> find(long cpf) {
        return Optional.ofNullable(this.customers.values()
                .stream().filter(customer -> Objects.equals(cpf, customer.getCpf()))
                .findAny().orElse(null));
    }

    @Override
    public Customer delete(long cpf) {
        return null;
    }
}
