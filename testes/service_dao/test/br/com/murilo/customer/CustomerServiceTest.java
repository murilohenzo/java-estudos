package br.com.murilo.customer;

import br.com.murilo.dao.ICustomerDAO;
import br.com.murilo.dao.implement.CustomerDAO;
import br.com.murilo.dao.mocks.CustomerDAOMock;
import br.com.murilo.service.CustomerService;
import org.junit.Assert;
import org.junit.Test;

public class CustomerServiceTest {

    @Test
    public void handleSaveCustomer() {
        ICustomerDAO mockDAO = new CustomerDAOMock();
        CustomerService customerService = new CustomerService(mockDAO);

        String returned = customerService.save();
        Assert.assertEquals("Successful", returned);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void handleExceptionSaveCustomer() {
        ICustomerDAO customerDAO = new CustomerDAO();
        CustomerService customerService = new CustomerService(customerDAO);
        String returned = customerService.save();
        Assert.assertEquals("Successful", returned);
    }
}