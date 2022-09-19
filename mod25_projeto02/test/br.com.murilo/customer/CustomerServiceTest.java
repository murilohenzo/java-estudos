package br.com.murilo.customer;

import br.com.murilo.domain.Customer;
import br.com.murilo.exceptions.CustomerAlreadyExistsException;
import br.com.murilo.exceptions.CustomerNotFoundException;
import br.com.murilo.repository.CustomerRepository;
import br.com.murilo.repository.ICustomerRepository;
import br.com.murilo.usecases.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CustomerServiceTest {
    Customer customer;
    ICustomerRepository repository = new CustomerRepository();

    @Before
    public void init() throws CustomerAlreadyExistsException {
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository);

        customer = useCase.handle(new Customer(
                "Murilo",
                12345678910L,
                102030L,
                "End",
                12345,
                "Fortaleza",
                "CE"
        ));
    }

    @Test
    public void whenNewCustomerInformedThenShouldBeCreated() {
        assertEquals("Murilo", customer.getName());
        assertEquals("Fortaleza", customer.getCity());
    }

    @Test
    public void whenExistingCustomerIsInformedThenAnExceptionShouldBeThrown() {
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository);

        assertThrows(CustomerAlreadyExistsException.class, () -> useCase.handle(
                new Customer(
                        "Murilo",
                        12345678910L,
                        102030L,
                        "End",
                        12345,
                        "Fortaleza",
                        "CE"
                ))
        );
    }

    @Test
    public void whenValidCpfIsGivenThenAnCustomerShouldBeReturned() throws CustomerNotFoundException {
        FindCustomerUseCase findCustomerUseCase = new FindCustomerUseCase(repository);
        assertEquals(customer, findCustomerUseCase.handle(12345678910L));
    }

    @Test
    public void whenInvalidCpfIsGivenThenAnExceptionShouldBeThrown() throws CustomerNotFoundException{
        FindCustomerUseCase findCustomerUseCase = new FindCustomerUseCase(repository);
        assertThrows(CustomerNotFoundException.class, () -> findCustomerUseCase.handle(1234567890L));
    }

    @Test
    public void whenValidCpfIsGivenThenAnCustomerShouldBeDeleted() throws CustomerNotFoundException {
        DeleteCustomerUseCase deleteCustomerUseCase = new DeleteCustomerUseCase(repository);
        deleteCustomerUseCase.handle(12345678910L);
        FindAllCustomersUseCase findAllCustomersUseCase = new FindAllCustomersUseCase(repository);
        assertEquals(0, findAllCustomersUseCase.handle().size());
    }

    @Test
    public void whenExistingCustomerIsShouldBeUpdatedCustomer() throws CustomerNotFoundException {
        UpdateCustomerUseCase updateCustomerUseCase = new UpdateCustomerUseCase(repository);
        Customer editCustomer = new Customer(
                "Henzo",
                12345678910L,
                102030L,
                "End",
                12345,
                "Fortaleza",
                "CE"
        );

        updateCustomerUseCase.handle(editCustomer);
        assertEquals("Henzo", customer.getName());
    }
}