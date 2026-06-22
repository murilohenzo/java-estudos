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

    private static final String VALID_CPF = "12345678910";
    private static final String ANOTHER_CPF = "98765432100";

    private Customer customer;
    private ICustomerRepository repository;

    @Before
    public void setUp() throws CustomerAlreadyExistsException {
        repository = new CustomerRepository();
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository);
        customer = useCase.handle(new Customer(
                "Murilo",
                VALID_CPF,
                "11987654321",
                "Rua das Flores",
                100,
                "Fortaleza",
                "CE"
        ));
    }

    @Test
    public void whenNewCustomerInformedThenShouldBeCreated() {
        assertEquals("Murilo", customer.getName());
        assertEquals("Fortaleza", customer.getCity());
        assertEquals(VALID_CPF, customer.getCpf());
    }

    @Test
    public void whenExistingCustomerIsInformedThenAnExceptionShouldBeThrown() {
        CreateCustomerUseCase useCase = new CreateCustomerUseCase(repository);
        assertThrows(CustomerAlreadyExistsException.class, () -> useCase.handle(
                new Customer("Murilo", VALID_CPF, "11987654321", "Rua das Flores", 100, "Fortaleza", "CE")
        ));
    }

    @Test
    public void whenValidCpfIsGivenThenCustomerShouldBeReturned() throws CustomerNotFoundException {
        FindCustomerUseCase findCustomerUseCase = new FindCustomerUseCase(repository);
        assertEquals(customer, findCustomerUseCase.handle(VALID_CPF));
    }

    @Test
    public void whenInvalidCpfIsGivenThenAnExceptionShouldBeThrown() {
        FindCustomerUseCase findCustomerUseCase = new FindCustomerUseCase(repository);
        assertThrows(CustomerNotFoundException.class, () -> findCustomerUseCase.handle(ANOTHER_CPF));
    }

    @Test
    public void whenValidCpfIsGivenThenCustomerShouldBeDeleted() throws CustomerNotFoundException {
        DeleteCustomerUseCase deleteCustomerUseCase = new DeleteCustomerUseCase(repository);
        deleteCustomerUseCase.handle(VALID_CPF);

        FindAllCustomersUseCase findAllCustomersUseCase = new FindAllCustomersUseCase(repository);
        assertEquals(0, findAllCustomersUseCase.handle().size());
    }

    @Test
    public void whenExistingCustomerIsUpdatedThenNameShouldChange() throws CustomerNotFoundException {
        UpdateCustomerUseCase updateCustomerUseCase = new UpdateCustomerUseCase(repository);
        Customer updatedData = new Customer(
                "Henzo",
                VALID_CPF,
                "11987654321",
                "Rua das Flores",
                100,
                "Fortaleza",
                "CE"
        );

        updateCustomerUseCase.handle(updatedData);
        assertEquals("Henzo", customer.getName());
    }
}
