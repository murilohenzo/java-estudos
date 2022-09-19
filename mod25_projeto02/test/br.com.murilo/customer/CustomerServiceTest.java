package br.com.murilo.customer;

import br.com.murilo.domain.Customer;
import br.com.murilo.exceptions.CustomerAlreadyExistsException;
import br.com.murilo.exceptions.CustomerNotFoundException;
import br.com.murilo.repository.CustomerRepository;
import br.com.murilo.repository.ICustomerRepository;
import br.com.murilo.usecases.CreateCustomerUseCase;
import br.com.murilo.usecases.FindCustomerUseCase;
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
    public void whenValidCpfIsGivenThenAnAuthorShouldBeReturned() {
        FindCustomerUseCase findCustomerUseCase = new FindCustomerUseCase(repository);
        assertEquals(customer, findCustomerUseCase.handle(12345678910L));
    }

    @Test
    public void whenInvalidCpfIsGivenThenAnExceptionShouldBeThrown() {

        ICustomerRepository repository = new CustomerRepository();
        FindCustomerUseCase findCustomerUseCase = new FindCustomerUseCase(repository);

        assertThrows(CustomerNotFoundException.class, () -> findCustomerUseCase.handle(12345678910L));
    }
}