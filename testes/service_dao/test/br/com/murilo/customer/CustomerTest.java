package br.com.murilo.customer;

import br.com.murilo.domain.Customer;
import org.junit.Assert;
import org.junit.Test;

public class CustomerTest {

    @Test
    public void createCustomer() {
        Customer customer = new Customer("John");
        Assert.assertEquals("John", customer.getName());

        customer.setName("John Doe");
        Assert.assertNotEquals("John", customer.getName());
    }
}
